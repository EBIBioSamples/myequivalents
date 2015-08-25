package uk.ac.ebi.fg.myequivalents.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.model.EntityId;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.utils.memory.SimpleCache;

/**
 * <p>This is a DB-specific implementation of {@link EntityIdResolver}. Namely, it overrides 
 * {@link #resolveUri(String, String, String)}, to search a service by name, when it's received in its parameters.</p>
 * 
 * <p>Note that this class performs searches of {@link Service} over the DB backend, which is the reason it
 * needs an {@link EntityManager} in its constructor. Moreover, all found services are cached in {@link #serviceCache}, 
 * which is a static class member (so is life spans the VM/class loader in which this class is used). The cache
 * is synchronized, further synchronization is ensured by transaction management into the myEquivalent managers.</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>2 Jun 2015</dd>
 *
 */
public class DbEntityIdResolver extends EntityIdResolver
{
	private ServiceDAO serviceDao;

	private static Map<String, Object> serviceCache = new SimpleCache<> ( 100000 );
	
	public DbEntityIdResolver ( EntityManager entityManager )
	{
		serviceDao = new ServiceDAO ( entityManager );
	}
	
	
	@Override
	public EntityId resolveUri ( String serviceName, String acc, String uri )
	{
		Service service = null; 
		boolean isUriAccVerified = false; 
		
		if ( serviceName != null )
		{
			// If it was specified, just verify it exists
			service = findServiceByName ( serviceName );
			
			if ( service == null ) throw new RuntimeException ( String.format (  
				"Error: cannot find service '%s'", serviceName 
			)); 
		}
		else
		{
			// We aren't given any serviceName, first try with breakUri () and possibly get the URI pattern
			String uriPattern = EntityIdResolver.breakUri ( acc, uri );
			
			// Now search based on uriPattern
			service = findServiceByUriPattern ( uriPattern );

			if ( service == null )
			{
				// No service found, so let's see if we can match more than one by means of URI domain-based search
				String uriDom = getDomain ( uri );
				List<Service> services = findServicesByUriPatternLike ( uriDom + "%" );
				
				// For each of the retrieved services, see if there is any that has a URI pattern compatible with the
				// parameter URI
				for ( Service servi: services )
				{
					if ( acc != null )
					{
						// Rebuild it when you have an accession
						String urii = buildUriFromAcc ( acc, servi.getUriPattern () );
						if ( uri.equals ( urii ) ) {
							service = servi;
							isUriAccVerified = true;
						}
					}
					else
					{
						// Else try to extract the accession
						String acci = extractAccession ( uri, servi.getUriPattern () );
						if ( acci != null ) {
							acc = acci;
							service = servi;
							isUriAccVerified = true;
						}
					}
					
					// If you found something, we're done. Ambiguity is left to the user.
					if ( service != null ) break;
				}
				
				if ( service == null ) 
					// If you didn't find any service, then fall back to unspecified service.
					service = Service.UNSPECIFIED_SERVICE;
				
			} // if ( service == null )
		} // if serviceName
			
		// We have an already URI-verified service, congratulations! Return the result
		if ( isUriAccVerified ) return new EntityId ( service, acc, uri );
		
		// Else, let's see if the URI matches the pattern
		String uriPattern = service.getUriPattern ();
		if ( "$id".equals ( uriPattern ) )
			acc = uri;
		else
		{
			if ( acc != null ) 
			{
				// As above, rebuild the URI if you have an accession to do so
				String builtUri = EntityIdResolver.buildUriFromAcc ( acc, uriPattern );
				if ( !builtUri.equals ( uri ) ) throw new RuntimeException ( String.format (
					"Entity ID error the URI <%s> is incompatible with the service '%s' having URI pattern '%s'",
					uri, serviceName, uriPattern
				));
			}
			else
			{
				// Else, try to rebuild the pattern from the URI and see if it matches the service's pattern. 
				String rebuiltUriPattern = EntityIdResolver.breakUri ( uri );
				if ( !uriPattern.equals ( rebuiltUriPattern ) ) throw new RuntimeException ( String.format (
					"Entity ID error the URI <%s> seems incompatible with the service '%s' having URI pattern '%s'",
					uri, serviceName, uriPattern
				));

				// If yes, user the pattern to extract the still-missing accession. 
				acc = EntityIdResolver.extractAccession ( uri, uriPattern );
			}
		} // if uriPattern != $id
		
		// Now the result should contains all of service, accession and URI.
		return new EntityId ( service, acc, uri );
	}

	/**
	 * Uses {@link #findServices(String, Callable)}, which adds up caching. 
	 */
	private synchronized Service findServiceByName ( final String name )
	{
		return findServices ( name, new Callable<Service>() 
		{
			@Override
			public Service call () throws Exception {
				return serviceDao.findByName ( name, false );
			}
		});
	}

	/**
	 * Uses {@link #findServices(String, Callable)}, which adds up caching. 
	 */
	private synchronized Service findServiceByUriPattern ( final String uriPattern )
	{
		return findServices ( uriPattern, new Callable<Service>() 
		{
			@Override
			public Service call () throws Exception {
				return serviceDao.findByUriPattern ( uriPattern, false );
			}
		});
	}

	/**
	 * Uses {@link #findServices(String, Callable)}, which adds up caching. 
	 */
	private synchronized List<Service> findServicesByUriPatternLike ( final String uriPatternLike )
	{
		return findServices ( uriPatternLike, new Callable<List<Service>>() 
		{
			@Override
			public List<Service> call () throws Exception 
			{
				return serviceDao.findByUriPatternLike ( uriPatternLike, false );
			}
		});
	}


	/**
	 * This is a wrapper that caches DB results fetched by {@link #serviceDao}.
	 * 
	 * In practice, it first lookup a T (which is supposed to be {@link Service} or a collection of services) 
	 * into {@link #serviceCache} and returns any non-null result, or, if the cache doesn't contain such key, 
	 * it searches it via finder, populate the case with non-null results, which is also returned.
	 * 
	 */
	private <T> T findServices ( String key, Callable<T> finder )
	{
		try
		{
			synchronized ( key.intern () )
			{
				@SuppressWarnings ( "unchecked" )
				T result = (T) serviceCache.get ( key );
				if ( result != null ) return result;
				
				result = finder.call ();
				
				if ( result != null ) serviceCache.put ( key, result );
				return result;
			}
		}
		catch ( Exception ex )
		{
			throw new RuntimeException ( 
				String.format ( "Internal error while searching service '%s': %s", key, ex.getMessage ()),
				ex 
			);
		}
	}
	
	
	public void setEntityManager ( EntityManager entityManager )
	{
		this.serviceDao.setEntityManager ( entityManager );
	}
}
