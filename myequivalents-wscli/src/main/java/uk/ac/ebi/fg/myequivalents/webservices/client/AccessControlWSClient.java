package uk.ac.ebi.fg.myequivalents.webservices.client;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang.StringUtils;

import com.sun.jersey.api.representation.Form;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.utils.io.IOUtils;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>17 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AccessControlWSClient extends MyEquivalentsWSClient implements AccessControlManager
{
	private String userPassword;
	
	
	public AccessControlWSClient ()
	{
		super ();
	}


	public AccessControlWSClient ( String baseUrl )
	{
		super ( baseUrl );
	}


	@Override
	protected String getServicePath () {
		return "/access-control";
	}
	
	@Override
	protected Form prepareReq ()
	{
		Form req = super.prepareReq ();
		if ( this.userPassword != null ) req.add ( "login-pwd", this.userPassword );
		
		return req;
	}


	@Override
	public User setFullAuthenticationCredentials ( String email, String userPassword ) throws SecurityException 
	{
		return setFullAuthenticationCredentials ( email, userPassword, false );
	}

	public User setFullAuthenticationCredentials ( String email, String userPassword, boolean connectServer ) throws SecurityException
	{
		this.email = StringUtils.trimToNull ( email );
		this.userPassword = StringUtils.trimToNull ( userPassword );
		this.apiPassword = null;
		
		if ( !connectServer ) return null;
					
		Form req = prepareReq ();
		return invokeWsReq ( "/login", req, User.class );
	}

	public User setAuthenticationCredentials ( String email, String apiPassword, boolean connectServer ) throws SecurityException
	{
		this.userPassword = null;
		return super.setAuthenticationCredentials ( email, apiPassword, connectServer );
	}
	
	/**
	 * TODO: comment me
	 * 
	 */
	@Override
	public void storeUser ( User user )
	{
		if ( user == null ) throw new IllegalArgumentException ( "cannot save a null user" );
		
		Form req = prepareReq ();
	  req.add ( "email", user.getEmail () );
		req.add ( "name",  user.getName () );
		req.add ( "surname", user.getSurname () );
		req.add ( "password", user.getPassword () );
		req.add ( "secret", user.getApiPassword () );
		req.add ( "notes", user.getNotes () );
		Role role = user.getRole ();
		if ( role != null ) req.add ( "role", role.toString () );
		
		this.invokeVoidWsReq ( "/user/store", req );
	}

	@Override
	public void storeUserFromXml ( Reader reader )
	{
		try
		{
			Form req = prepareReq ();
			req.add ( "user-xml", IOUtils.readInputFully ( reader ) );
			invokeVoidWsReq ( "/user/store/from-xml", req );
		} 
		catch ( IOException ex ) {
			throw new RuntimeException ( "Error while reading user XML: " + ex.getMessage (), ex );
		}
		
	}

	@Override
	public User getUser ( String email )
	{
		Form req = prepareReq ();
		req.add ( "email", email );
		
	  return invokeWsReq ( "/user/get", req, User.class );
	}

	@Override
	public String getUserAs ( String outputFormat, String email )
	{
		Form req = prepareReq ();
		req.add ( "email", email );
		
	  return getRawResult ( "/user/get", req, outputFormat );
	}

	@Override
	public void setUserRole ( String email, Role role )
	{
		Form req = prepareReq ();
		req.add ( "email", email );
		if ( role != null ) req.add ( "role", role.toString () );
		
		invokeVoidWsReq ( "/user/role/set", req );
	}

	@Override
	public boolean deleteUser ( String email )
	{
		Form req = prepareReq ();
		req.add ( "email", email );
		return invokeBooleanWsReq ( "/user/delete", req );
	}

	@Override
	public void setServicesVisibility ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceNames )
	{
		Form req = prepareReq ();
		req.add ( "public-flag", publicFlagStr );
		req.add ( "release-date", releaseDateStr );
		req.add ( "cascade", cascade );
	  for ( String serviceName: serviceNames ) req.add ( "service", serviceName );
		
	  invokeVoidWsReq ( "/visibility/service/set", req );
	}


	@Override
	public void setRepositoriesVisibility ( 
		String publicFlagStr, String releaseDateStr, boolean cascade, String ... repositoryNames )
	{
		Form req = prepareReq ();
		req.add ( "public-flag", publicFlagStr );
		req.add ( "release-date", releaseDateStr );
		req.add ( "cascade", cascade );
	  for ( String repoName: repositoryNames ) req.add ( "repository", repoName );
		
	  invokeVoidWsReq ( "/visibility/repository/set", req );
	}


	@Override
	public void setServiceCollectionsVisibility ( 
		String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceCollNames )
	{
		Form req = prepareReq ();
		req.add ( "public-flag", publicFlagStr );
		req.add ( "release-date", releaseDateStr );
		req.add ( "cascade", cascade );
	  for ( String serviceCollName: serviceCollNames ) req.add ( "service-coll", serviceCollName );
		
	  invokeVoidWsReq ( "/visibility/service-collection/set", req );
	}


	@Override
	public void setEntitiesVisibility ( String publicFlagStr, String releaseDateStr, String ... entityIds )
	{
		Form req = prepareReq ();
		req.add ( "public-flag", publicFlagStr );
		req.add ( "release-date", releaseDateStr );
	  for ( String entityId: entityIds ) req.add ( "entity", entityId );
		
	  invokeVoidWsReq ( "/visibility/entity/set", req );
	}

}
