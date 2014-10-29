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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>25 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@Path ( "/service" )
public class ServiceWebService
{
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	@POST
	@Path( "/store" )
	@Produces ( MediaType.APPLICATION_XML )
	public void store (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "service-items-xml" ) String serviceItemsXml
	) 
	{
		ServiceManager smgr = getServiceManager ( authEmail, authApiPassword );
		smgr.storeServicesFromXML ( new StringReader ( serviceItemsXml ) );
		smgr.close ();
	}
	
	
	@POST
	@Path( "/delete" )
	@Produces ( MediaType.APPLICATION_XML )
	public String deleteServices (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "service" ) List<String> serviceNames ) 
	{
		ServiceManager smgr = getServiceManager ( authEmail, authApiPassword );
		int result = smgr.deleteServices ( serviceNames.toArray ( new String [ 0 ] ) );
		smgr.close ();
		
		return String.valueOf ( result );
	}

	@POST
	@Path( "/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public ServiceSearchResult getServices (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "service" ) List<String> serviceNames 
	)
	{
		ServiceManager smgr = getServiceManager ( authEmail, authApiPassword );
		ServiceSearchResult result = smgr.getServices ( serviceNames.toArray ( new String [ 0 ] ) );
		smgr.close ();
		
		return result;
	}

	@GET
	@Path( "/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public ServiceSearchResult getServicesViaGET (
		@QueryParam ( "login" ) String authEmail, 
		@QueryParam ( "login-secret" ) String authApiPassword,
		@QueryParam ( "service" ) List<String> serviceNames 
	)
	{
		return getServices ( authEmail, authApiPassword, serviceNames );
	}

	

	@POST
	@Path( "/service-collection/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public ServiceSearchResult getServiceCollections (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "service-coll" ) List<String> serviceCollNames 
	)
	{
		ServiceManager smgr = getServiceManager ( authEmail, authApiPassword );
		ServiceSearchResult result = smgr.getServiceCollections ( serviceCollNames.toArray ( new String [ 0 ] ) );
		smgr.close ();
		
		return result;
	}

	@GET
	@Path( "/service-collection/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public ServiceSearchResult getServiceCollectionsViaGET (
		@QueryParam ( "login" ) String authEmail, 
		@QueryParam ( "login-secret" ) String authApiPassword,
		@QueryParam ( "service-coll" ) List<String> serviceCollNames 
	)
	{
		return getServices ( authEmail, authApiPassword, serviceCollNames );
	}	
	
	@POST
	@Path( "/service-collection/delete" )
	@Produces ( MediaType.APPLICATION_XML )
	public String deleteServiceCollections (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "service-coll" ) List<String> serviceCollNames ) 
	{
		ServiceManager smgr = getServiceManager ( authEmail, authApiPassword );
		int result = smgr.deleteServiceCollections ( serviceCollNames.toArray ( new String [ 0 ] ) );
		smgr.close ();
		
		return String.valueOf ( result );
	}
	
	
	
	
	@POST
	@Path( "/repository/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public ServiceSearchResult getRepositories (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "repository" ) List<String> repoNames 
	)
	{
		ServiceManager smgr = getServiceManager ( authEmail, authApiPassword );
		ServiceSearchResult result = smgr.getRepositories ( repoNames.toArray ( new String [ 0 ] ) );
		smgr.close ();
		
		return result;
	}

	@GET
	@Path( "/repository/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public ServiceSearchResult getRepositoriesViaGET (
		@QueryParam ( "login" ) String authEmail, 
		@QueryParam ( "login-secret" ) String authApiPassword,
		@QueryParam ( "repository" ) List<String> repoNames  
	)
	{
		return getServices ( authEmail, authApiPassword, repoNames );
	}	
	
	@POST
	@Path( "/repository/delete" )
	@Produces ( MediaType.APPLICATION_XML )
	public String deleteServiceRepositories (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "repository" ) List<String> repoNames 
	) 
	{
		ServiceManager smgr = getServiceManager ( authEmail, authApiPassword );
		int result = smgr.deleteRepositories ( repoNames.toArray ( new String [ 0 ] ) );
		smgr.close ();
		
		return String.valueOf ( result );
	}
	
	
	
	private ServiceManager getServiceManager ( String authEmail, String authApiPassword ) 
	{
		log.trace ( "Returning access manager for the user {}, {}", authEmail, authApiPassword == null ? null: "***" );
		return Resources.getInstance ().getMyEqManagerFactory ().newServiceManager ( authEmail, authApiPassword );
	}
}
