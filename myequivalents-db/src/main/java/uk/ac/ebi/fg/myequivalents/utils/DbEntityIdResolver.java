package uk.ac.ebi.fg.myequivalents.utils;

import java.util.List;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.model.EntityId;
import uk.ac.ebi.fg.myequivalents.model.Service;

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
			service = serviceDao.findByName ( serviceName );
			
			if ( service == null ) throw new RuntimeException ( String.format (  
				"Error: cannot find service '%s'", serviceName 
			)); 
		}
		else
		{
			// We don't have serviceName, first try with breakUri ()
			String uriPattern = EntityIdResolver.breakUri ( acc, uri );
			
			// Now search based on uriPattern
			service = serviceDao.findByUriPattern ( uriPattern, false );

			if ( service == null )
			{
				// Try to search multiple services by domain
				String uriDom = getDomain ( uri );
				
				// Now search services and see if there is one matching the pattern
				List<Service> services = serviceDao.findByUriPatternLike ( uriDom + "%", false );
				
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
				
				if ( service == null ) throw new RuntimeException ( String.format ( 
					"Error: cannot find any service for the URI <%s>", uri
				));
			} // if ( service == null )
		} // if serviceName
			
		// We have a service, check the URI and accession match
		if ( isUriAccVerified ) return new EntityId ( service, acc, uri );
		
		String uriPattern = service.getUriPattern ();
		
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
		
		return new EntityId ( service, acc, uri );
	}
	
	public void setEntityManager ( EntityManager entityManager )
	{
		this.serviceDao.setEntityManager ( entityManager );
	}
}
