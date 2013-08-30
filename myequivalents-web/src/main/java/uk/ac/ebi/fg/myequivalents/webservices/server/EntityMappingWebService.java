package uk.ac.ebi.fg.myequivalents.webservices.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.JAXBUtils;

/**
 * <p>The web service version of the {@link EntityMappingManager} interface. This uses Jersey and set up a REST web service
 * See {@link uk.ac.ebi.fg.myequivalents.webservices.client.EntityMappingWSClientTest} for usage examples.</p>
 * 
 * <p>The web service is backed by a {@link ManagerFactory}, which needs to be configured via Spring, see {@link Resources}.
 * By default {@link DbManagerFactory} is used.</p>
 * 
 * <p>Usually these services are located at /ws/mapping, e.g., 
 * "http://localhost:8080/ws/mapping/get?entityId=service1:acc1". You can build the path by appending the value in 
 * &#064;Path to /mapping.</p> 
 *
 * <dl><dt>date</dt><dd>Sep 11, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Path ( "/mapping" )
public class EntityMappingWebService
{
	@Context 
	private ServletContext servletContext;
		
	/**
	 * We need this version of {@link #getMappings(Boolean, String...)} because Jersey/JAX-WS doesn't like arrays.
	 */
	@POST
	@Path( "/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public EntityMappingSearchResult getMappings ( 
		@FormParam ( "email" ) String email, 
		@FormParam ( "secret" ) String apiPassword,
		@FormParam ( "raw" ) Boolean isRaw, 
		@FormParam ( "entity" ) List<String> entitites 
	) 
	{
		EntityMappingManager emgr = getEntityMappingManager ( email, apiPassword );
		EntityMappingSearchResult result = emgr.getMappings ( isRaw, entitites.toArray ( new String [ 0 ] ) );
		emgr.close();
		return result; 
	}
	
	/** 
	 * This is just the HTTP/GET version of {@link #getMappings(Boolean, List)}. Normally you will want the 
	 * POST invocation, this is here for testing purposes only. 
	 */
	@GET
	@Path( "/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public EntityMappingSearchResult getMappingsViaGET ( 
		@QueryParam ( "email" ) String email, 
		@QueryParam ( "secret" ) String apiPassword,
		@QueryParam ( "raw" ) Boolean isRaw, 
		@QueryParam ( "entity" ) List<String> entitites 
	) 
	{
		return getMappings ( email, apiPassword, isRaw, entitites );
	}
	
	
	public void storeMappings ( String ... entityIds )
	{
		// TODO Auto-generated method stub
	}

	public void storeMappingBundle ( String ... entityIds )
	{
		// TODO Auto-generated method stub
		
	}

	public int deleteMappings ( String ... entityIds )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int deleteEntities ( String ... entityIds )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public String getMappingsAs ( String outputFormat, Boolean wantRawResult, String ... entityIds )
	{
		// TODO Auto-generated method stub
		return null;
	}	
	

	@POST
	@Path( "/get-target" )
	@Produces ( MediaType.APPLICATION_XML )
	public EntityMappingSearchResult getMappingsForTarget (
		@FormParam ( "email" ) String email, 
		@FormParam ( "secret" ) String apiPassword,
		@FormParam ( "raw" ) Boolean wantRawResult, 
		@FormParam ( "service" ) String targetServiceName, 
		@FormParam ( "entity" ) String entityId )
	{
		EntityMappingManager emgr = getEntityMappingManager ( email, apiPassword );
		EntityMappingSearchResult result = emgr.getMappingsForTarget ( wantRawResult, targetServiceName, entityId );
		emgr.close();
		return result; 
	}

	/** 
	 * This is just the HTTP/GET version of {@link #getMappingsForTarget(Boolean, String, String)}. Normally you will want the 
	 * POST invocation, this is here for testing purposes only. 
	 */
	@GET
	@Path( "/get-target" )
	@Produces ( MediaType.APPLICATION_XML )
	public EntityMappingSearchResult getMappingsForTargetViaGET (
			@QueryParam ( "email" ) String email, 
			@QueryParam ( "secret" ) String apiPassword,
			@QueryParam ( "raw" ) Boolean wantRawResult, 
			@QueryParam ( "service" ) String targetServiceName, 
			@QueryParam ( "entity" ) String entityId )
	{
		return getMappingsForTarget ( email, apiPassword, wantRawResult, targetServiceName, entityId );
	}

	/**
	 * <p>This returns the result from {@link #getMappingsForTarget(Boolean, String, String)} in the format of 
	 * an HTTP 301 response, so the browser is re-directed to the result's URI {@link Entity#getURI()}. In case
	 * the result is not unique, some XML is returned, that is similar to the XML returned by 
	 * {@link #getMappingsAs(String, Boolean, String...) getMappingsAs ( "xml", ... )}. You can couple such XML
	 * with the URL of an XSL, using the xsl/xslUri parameter. The default for such xsl is /go-to-target/multiple-results.xsl.
	 * See the myEquivalents wiki for details. If the input has no mapping HTTP/404 (Not Found) is returned</p> 
	 * 
	 * <p>You can test this with the examples (after 'mvn jetty:run'):
	 *   <ul>  
	 *   	<li><a href = "http://localhost:8080/ws/mapping/go-to-target?entity=test.testweb.service8:acc10&service=test.testweb.service7">common case</a></li>
	 *   	<li><a href = "http://localhost:8080/ws/mapping/go-to-target?entity=test.testweb.service7:acc1&service=test.testweb.service6">multiple entities on the target</a></li>
	 *   	<li><a href = "http://localhost:8080/ws/mapping/go-to-target?entity=foo-service:foo-acc&service=foo-target">non-existing target</a></li>
	 *   </ul>
	 * </p>
	 */
	@GET
	@Path( "/go-to-target" )
	@Produces ( MediaType.TEXT_XML )
	public Response getMappingsForTargetRedirection (
			@QueryParam ( "email" ) String email, 
			@QueryParam ( "secret" ) String apiPassword,
			@QueryParam ( "service" ) String targetServiceName, 
			@QueryParam ( "entity" ) String entityId,
			@QueryParam ( "xsl" ) String xslUri 
	) throws URISyntaxException
	{
		EntityMappingSearchResult result = getMappingsForTarget ( email, apiPassword, false, targetServiceName, entityId );
		Collection<Bundle> bundles = result.getBundles ();

		if ( bundles.size () == 0 ) return Response.status ( Status.NOT_FOUND ).build ();
		if ( bundles.size () > 1 ) throw new RuntimeException ( 
			"Internal error while getting mappings for '" + entityId + "' to '" + targetServiceName 
			+ ": there is more than a bundle as result and that's impossible, must be some bug, sorry." );
		
		Bundle bundle = bundles.iterator ().next ();
		Set<Entity> entities = bundle.getEntities ();
		if ( entities.size () == 1 )
			return Response.status ( Status.MOVED_PERMANENTLY ).location ( 
					new URI ( entities.iterator ().next ().getURI () ) ).build ();
		
		if ( xslUri == null )
			xslUri = UriBuilder.fromPath ( 
				servletContext.getContextPath () + "/go-to-target/multiple-results.xsl" ).build ().toASCIIString ();
		else
			xslUri = new URI ( xslUri ).toASCIIString ();
		
		String resultXml = JAXBUtils.marshal ( result, EntityMappingSearchResult.class );
		resultXml = resultXml.replaceFirst ( 
			"<\\?xml(.*)\\?>", "<?xml$1 ?>\n" + "<?xml-stylesheet type='text/xsl' href='" + xslUri + "' ?>\n" );
		resultXml = resultXml.replaceFirst ( 
				"<mappings>", String.format ( "<mappings entity-id = '%s' target-service-name = '%s'>\n", entityId, targetServiceName ) );
		
		return Response.ok ( resultXml, MediaType.TEXT_XML ).build ();		
	}

	/**
	 * TODO: comment me. 
	 * TODO: auth requests should be factorised to an Access service (they're not used by other WS requests)
	 */
	@POST
	@Path( "/login" )
	@Produces ( MediaType.APPLICATION_XML )
	public User setAuthenticationCredentials ( 
		@FormParam ( "email" ) String email, @FormParam ( "secret" ) String apiPassword
	) 
		throws SecurityException
	{
		EntityMappingManager em = Resources.getInstance ().getMyEqManagerFactory ().newEntityMappingManager ();		
		return em.setAuthenticationCredentials ( email, apiPassword );
	}

	/**
	 * The GET variant of {@link #setAuthenticationCredentials(String, String)}, used for testing purposes only.
	 * @param email
	 * @param apiPassword
	 * @return
	 * @throws SecurityException
	 */
	@GET
	@Path( "/login" )
	@Produces ( MediaType.APPLICATION_XML )
	public User setAuthenticationCredentialsViaGET ( 
		@QueryParam ( "email" ) String email, @QueryParam ( "secret" ) String apiPassword
	) 
		throws SecurityException
	{
		return setAuthenticationCredentials ( email, apiPassword );
	}

	/** TODO: Comment met! TODO: AOP */
	private EntityMappingManager getEntityMappingManager ( String email, String apiPassword )
	{
		return Resources.getInstance ().getMyEqManagerFactory ().newEntityMappingManager ( email, apiPassword );
	}
}
