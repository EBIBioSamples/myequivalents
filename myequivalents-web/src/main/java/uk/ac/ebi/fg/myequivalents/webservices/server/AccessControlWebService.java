package uk.ac.ebi.fg.myequivalents.webservices.server;

import java.io.StringReader;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

import static uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>16 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@Path ( "/perms" )
public class AccessControlWebService
{
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	@POST
	@Path( "/user/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public User getUser (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-pwd" ) String authPassword, 
		@FormParam ( "login-secret" ) String authApiPassword, 
		@FormParam ( "email" ) String email
	)
	{
		AccessControlManager mgr = getAccessControlManager ( authEmail, authPassword, authApiPassword );
		User result = mgr.getUser ( email );
		mgr.close ();
		return result;
	}
	
	@GET
	@Path( "/user/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public User getUserViaGET (
		@QueryParam ( "login" ) String authEmail, 
		@QueryParam ( "login-pwd" ) String authPassword, 
		@QueryParam ( "login-secret" ) String authApiPassword, 
		@QueryParam ( "email" ) String email
	)
	{
		return getUser ( authEmail, authPassword, authApiPassword, email );
	}
	
	
	@POST
	@Path( "/user/store" )
	@Produces ( MediaType.APPLICATION_XML )
	public void storeUser ( 
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-pwd" ) String authPassword, 
		@FormParam ( "email" ) String email, 
		@FormParam ( "name" ) String name, 
		@FormParam ( "surname" ) String surname, 
		@FormParam ( "password" ) String userPassword, 
		@FormParam ( "notes" ) String notes, 
		@FormParam ( "role" ) String role, 
		@FormParam ( "secret" ) String apiPassword )
	{
		AccessControlManager mgr = getAccessControlManager ( authEmail, authPassword, null );
		mgr.storeUser ( new User ( 
			email, name, surname, userPassword, notes, 
			Role.valueOf ( StringUtils.upperCase ( role ) ), 
			apiPassword 
		));
		mgr.close ();
	}
	
	@POST
	@Path ( "/user/store/from-xml" )
	@Produces ( MediaType.APPLICATION_XML )
	public void storeUserFromXml (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-pwd" ) String authPassword, 
		@FormParam ( "user-xml" ) String userXml )
	{
		AccessControlManager mgr = getAccessControlManager ( authEmail, authPassword, null );
		mgr.storeUserFromXml ( new StringReader ( userXml ) );
		mgr.close ();
	}
	
	

	@POST
	@Path( "/user/role/set" )
	@Produces ( MediaType.APPLICATION_XML )
	public void setUserRole ( 
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-pwd" ) String authPassword, 
		@FormParam ( "login-secret" ) String authApiPassword, 
		@FormParam ( "email" ) String email,
		@FormParam ( "role" ) String role
	)
	{
		AccessControlManager mgr = getAccessControlManager ( authEmail, authPassword, authApiPassword );
		mgr.setUserRole ( email, Role.valueOf ( StringUtils.upperCase ( role ) ) );
		mgr.close ();
	}

	@POST
	@Path( "/user/delete" )
	@Produces ( MediaType.APPLICATION_XML )
	public String deleteUser ( 
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-pwd" ) String authPassword, 
		@FormParam ( "login-secret" ) String authApiPassword, 
		@FormParam ( "email" ) String email
	)
	{
		AccessControlManager mgr = getAccessControlManager ( authEmail, authPassword, authApiPassword );
		boolean result = mgr.deleteUser ( email );
		mgr.close ();
		return String.valueOf ( result );
	}

	
	@POST
	@Path( "/visibility/service/set" )
	@Produces ( MediaType.APPLICATION_XML )
	public void setServicesVisibility ( 
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword, 
		@FormParam ( "public-flag" ) String publicFlagStr, 
		@FormParam ( "release-date" ) String releaseDateStr, 
		@FormParam ( "cascade" ) boolean cascade, 
		@FormParam ( "service" ) List<String> serviceNames			
	)
	{
		AccessControlManager mgr = getAccessControlManager ( authEmail, null, authApiPassword );
		mgr.setServicesVisibility ( publicFlagStr, releaseDateStr, cascade, serviceNames.toArray ( new String [ 0 ] ) );
		mgr.close ();
	}

	
	@POST
	@Path( "/visibility/repository/set" )
	@Produces ( MediaType.APPLICATION_XML )
	public void setRepositoriesVisibility ( 
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword, 
		@FormParam ( "public-flag" ) String publicFlagStr, 
		@FormParam ( "release-date" ) String releaseDateStr, 
		@FormParam ( "cascade" ) boolean cascade, 
		@FormParam ( "repository" ) List<String> repositoryNames			
	)
	{
		AccessControlManager mgr = getAccessControlManager ( authEmail, null, authApiPassword );
		mgr.setRepositoriesVisibility ( publicFlagStr, releaseDateStr, cascade, repositoryNames.toArray ( new String [ 0 ] ) );
		mgr.close ();
	}
	
	@POST
	@Path( "/visibility/service-coll/set" )
	@Produces ( MediaType.APPLICATION_XML )
	public void setServiceCollectionVisibility ( 
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword, 
		@FormParam ( "public-flag" ) String publicFlagStr, 
		@FormParam ( "release-date" ) String releaseDateStr, 
		@FormParam ( "cascade" ) boolean cascade, 
		@FormParam ( "service-coll" ) List<String> serviceCollNames			
	)
	{
		AccessControlManager mgr = getAccessControlManager ( authEmail, null, authApiPassword );
		mgr.setServiceCollectionsVisibility ( publicFlagStr, releaseDateStr, cascade, serviceCollNames.toArray ( new String [ 0 ] ) );
		mgr.close ();
	}

	
	@POST
	@Path( "/visibility/entity/set" )
	@Produces ( MediaType.APPLICATION_XML )
	public void setEntitiesVisibility ( 
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword, 
		@FormParam ( "public-flag" ) String publicFlagStr, 
		@FormParam ( "release-date" ) String releaseDateStr, 
		@FormParam ( "entity" ) List<String> entityIds			
	)
	{
		AccessControlManager mgr = getAccessControlManager ( authEmail, null, authApiPassword );
		mgr.setEntitiesVisibility ( publicFlagStr, releaseDateStr, entityIds.toArray ( new String [ 0 ] ) );
		mgr.close ();
	}

	/**
	 * TODO: comment me. 
	 * TODO: auth requests should be factorised to an Access service (they're not used by other WS requests).
	 * TODO: it's being done in inefficient way (authentication done twice).
	 */
	@POST
	@Path( "/login" )
	@Produces ( MediaType.APPLICATION_XML )
	public User setAuthenticationCredentials ( 
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-pwd" ) String authPassword, 
		@FormParam ( "login-secret" ) String authApiPassword
	) 
		throws SecurityException
	{
		AccessControlManager mgr = getAccessControlManager ( authEmail, authPassword, authApiPassword );
		User result = authPassword == null 
			? mgr.setAuthenticationCredentials ( authEmail, authApiPassword )
			: mgr.setFullAuthenticationCredentials ( authEmail, authPassword );
		mgr.close ();
		return result;
	}

	/**
	 * The GET variant of {@link #setAuthenticationCredentials(String, String)}, used for testing purposes only.
	 */
	@GET
	@Path( "/login" )
	@Produces ( MediaType.APPLICATION_XML )
	public User setAuthenticationCredentialsViaGET ( 
		@QueryParam ( "login" ) String authEmail, 
		@QueryParam ( "login-pwd" ) String authPassword, 
		@QueryParam ( "login-secret" ) String authApiPassword
	) 
		throws SecurityException
	{
		return setAuthenticationCredentials ( authEmail, authPassword, authApiPassword );
	}	
	
	
	/** Gets the {@link AccessControlManager} that is used internally in this web service TODO: AOP */
	private AccessControlManager getAccessControlManager ( String email, String userPassword, String apiPassword )
	{
		log.trace ( String.format ( "Returning access manager for the user %s, %s, %s", 
			email, userPassword == null ? null: "***", apiPassword == null ? null : "***" ));
		ManagerFactory fact = Resources.getInstance ().getMyEqManagerFactory ();
		return userPassword == null 
			? fact.newAccessControlManager ( email, apiPassword )
			: fact.newAccessControlManagerFullAuth ( email, userPassword );
	}
}
