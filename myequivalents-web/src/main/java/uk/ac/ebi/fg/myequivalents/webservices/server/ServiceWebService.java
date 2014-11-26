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
 * This implement the REST web service corresponding to {@link ServiceManager}, using the Jersey library and hence
 * JAX-RS. See {@link uk.ac.ebi.fg.myequivalents.webservices.client.ServiceWSClientIT} for usage examples}.
 *
 * <p>Usually these services are located at /ws/mapping, e.g., 
 * "https://localhost:8080/ws/service/get?service=service1". You can build the path by appending the value in 
 * &#064;Path (which annotates every service method) to /service.</p> 
 *
 * @see EntityMappingWebService.
 *
 * <dl><dt>date</dt><dd>25 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@Path ( "/service" )
public class ServiceWebService
{
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	/**
	 * This is the equivalent of {@link ServiceManager#storeServicesFromXML(java.io.Reader)}.
	 */
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
	
	
	/**
	 * This is the equivalent of {@link ServiceManager#deleteServices(String...)}.
	 */
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

	/**
	 * This is the equivalent of {@link ServiceManager#getServices(String...)}.
	 */
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

	/**
	 * This is an HTTP/GET version of {@link #getServices(String, String, List)}.
	 */
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

	
	/**
	 * The equivalent of {@link ServiceManager#getServiceCollections(String...)}.
	 */
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

	/**
	 * A HTTP/GET version of {@link #getServiceCollections(String, String, List)}.
	 */
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
	
	/**
	 * The equivalent of {@link ServiceManager#deleteServiceCollections(String...)}.
	 * @return an integer in the form of a string, cause Jersey doesn't like other types very much.
	 */
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
	
	
	
	/**
	 * The equivalent of {@link ServiceManager#getRepositories(String...)}
	 */
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

	/**
	 * An HTTP/GET version of {@link #getRepositoriesViaGET(String, String, List)}.
	 */
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
	
	/**
	 * The equivalent of {@link ServiceManager#deleteRepositories(String...)}.
	 * @return an integer in the form of a string, cause Jersey doesn't like other types very much.
	 */
	@POST
	@Path( "/repository/delete" )
	@Produces ( MediaType.APPLICATION_XML )
	public String deleteRepositories (
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
	
	
	/** 
	 * Gets the {@link ServiceManager} that is used internally in this web service. This is obtained from
	 * {@link Resources} and hence it depends on the Spring configuration, accessed through {@link WebInitializer}. 
	 * 
	 * TODO: AOP 
	 */
	private ServiceManager getServiceManager ( String authEmail, String authApiPassword ) 
	{
		log.trace ( "Returning access manager for the user {}, {}", authEmail, authApiPassword == null ? null: "***" );
		return Resources.getInstance ().getMyEqManagerFactory ().newServiceManager ( authEmail, authApiPassword );
	}
}
