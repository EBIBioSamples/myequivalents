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
 * TODO: comment me!
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
			// Just verify it exists
			service = findServiceByName ( serviceName );
			
			if ( service == null ) throw new RuntimeException ( String.format (  
				"Error: cannot find service '%s'", serviceName 
			)); 
		}
		else
		{
			// We don't have serviceName, first try with breakUri ()
			String uriPattern = EntityIdResolver.breakUri ( acc, uri );
			
			// Now search based on uriPattern
			service = findServiceByUriPattern ( uriPattern );

			if ( service == null )
			{
				// Try to search multiple services by domain
				String uriDom = getDomain ( uri );
				
				// Now search services and see if there is one matching the pattern
				List<Service> services = findServicesByUriPatternLike ( uriDom + "%" );
				
				for ( Service servi: services )
				{
					if ( acc != null )
					{
						String urii = buildUriFromAcc ( acc, servi.getUriPattern () );
						if ( uri.equals ( urii ) ) {
							service = servi;
							isUriAccVerified = true;
						}
					}
					else
					{
						String acci = extractAccession ( uri, servi.getUriPattern () );
						if ( acci != null ) {
							acc = acci;
							service = servi;
							isUriAccVerified = true;
						}
					}
					
					if ( service != null ) break;
				}
				
				if ( service == null ) 
					// If you don't find any service, then fall back to unspecified service.
					service = Service.UNSPECIFIED_SERVICE;
				
			} // if ( service == null )
		} // if serviceName
			
		// We have a service, check the URI and accession match
		if ( isUriAccVerified ) return new EntityId ( service, acc, uri );
		
		String uriPattern = service.getUriPattern ();
		
		if ( "$id".equals ( uriPattern ) )
			acc = uri;
		else
		{
			if ( acc != null ) 
			{
				String builtUri = EntityIdResolver.buildUriFromAcc ( acc, uriPattern );
				if ( !builtUri.equals ( uri ) ) throw new RuntimeException ( String.format (
					"Entity ID error the URI <%s> is incompatible with the service '%s' having URI pattern '%s'",
					uri, serviceName, uriPattern
				));
			}
			else
			{
				String rebuiltUriPattern = EntityIdResolver.breakUri ( uri );
				if ( !uriPattern.equals ( rebuiltUriPattern ) ) throw new RuntimeException ( String.format (
					"Entity ID error the URI <%s> seems incompatible with the service '%s' having URI pattern '%s'",
					uri, serviceName, uriPattern
				));
	
				acc = EntityIdResolver.extractAccession ( uri, uriPattern );
			}
		} // if uriPattern != $id
		
		return new EntityId ( service, acc, uri );
	}

	
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
