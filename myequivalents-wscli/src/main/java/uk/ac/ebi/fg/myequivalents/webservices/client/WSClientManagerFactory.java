package uk.ac.ebi.fg.myequivalents.webservices.client;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>21 Jan 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class WSClientManagerFactory implements ManagerFactory
{
	private String baseUrl = null;
	
	/**
	 * Default is http://localhost:8080/myequivalents/ws
	 */
	public WSClientManagerFactory ( String baseUrl )
	{
		super ();
		this.baseUrl = StringUtils.trimToNull ( baseUrl );
		if ( this.baseUrl == null ) baseUrl = "http://localhost:8080/myequivalents/ws";
	}
	
	public WSClientManagerFactory ()
	{
		this ( null );
	}


	@Override
	public EntityMappingManager newEntityMappingManager ()
	{
		return newEntityMappingManager ( null, null );
	}

	@Override
	public EntityMappingManager newEntityMappingManager ( String email, String apiPassword )
	{
		return newEntityMappingManager ( email, apiPassword, false );
	}

	public EntityMappingManager newEntityMappingManager ( String email, String apiPassword, boolean connectServer )
	{
		EntityMappingWSClient result = new EntityMappingWSClient ( this.baseUrl );
		result.setAuthenticationCredentials ( email, apiPassword, connectServer );
		
		return result;
	}
	
	
	
	@Override
	public ServiceManager newServiceManager ()
	{
		return newServiceManager ( null, null );
	}

	@Override
	public ServiceManager newServiceManager ( String email, String apiPassword )
	{
		return newServiceManager ( email, apiPassword, false );
	}
	
	public ServiceManager newServiceManager ( String email, String apiPassword, boolean connectServer )
	{
		ServiceWSClient result = new ServiceWSClient ( this.baseUrl );
		result.setAuthenticationCredentials ( email, apiPassword, connectServer );
		return result;
	}

	
	
	@Override
	public AccessControlManager newAccessControlManager ( String email, String apiPassword )
	{
		return newAccessControlManager ( email, apiPassword, false );
	}

	public AccessControlManager newAccessControlManager ( String email, String apiPassword, boolean connectServer )
	{
		AccessControlWSClient result = new AccessControlWSClient ( this.baseUrl );
		result.setAuthenticationCredentials ( email, apiPassword, connectServer );
		return result;
	}
	
	
	
	@Override
	public AccessControlManager newAccessControlManagerFullAuth ( String email, String userPassword )
	{
		return newAccessControlManagerFullAuth ( email, userPassword, false );
	}

	public AccessControlManager newAccessControlManagerFullAuth ( String email, String userPassword, boolean connectServer )
	{
		AccessControlWSClient result = new AccessControlWSClient ( this.baseUrl );
		result.setFullAuthenticationCredentials ( email, userPassword, connectServer );
		return result;
	}

}
