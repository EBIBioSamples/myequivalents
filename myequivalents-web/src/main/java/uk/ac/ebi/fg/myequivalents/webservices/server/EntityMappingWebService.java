/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.webservices.server;

import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;

/**
 * <p>The web service version of the {@link EntityMappingManager} interface. This uses Jersey and set up a REST web service
 * See {@link uk.ac.ebi.fg.myequivalents.webservices.client.EntityMappingWSClientTest} for usage examples.</p>
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
public class EntityMappingWebService implements EntityMappingManager
{
	/** 
	 * This is just the GET version of {@link #getMappings(Boolean, List)}. Normally you will want the 
	 * {{@link #getMappingsViaPOST(Boolean, List) POST invocation}, this is here for testing purposes only. 
	 * 
	 */
	@GET
	@Path( "/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public EntityMappingSearchResult getMappingsViaGET ( 
		@QueryParam ( "raw" ) Boolean isRaw, 
		@QueryParam ( "entity" ) List<String> entitites 
	) 
	{
		return getMappings ( isRaw, entitites.toArray ( new String [ 0 ] ) );
	}
	
	/**
	 * We need this version of {@link #getMappings(Boolean, String...)} because Jersey/JAX-WS doesn't like arrays.
	 */
	@POST
	@Path( "/get" )
	@Produces ( MediaType.APPLICATION_XML )
	public EntityMappingSearchResult getMappings ( 
		@FormParam ( "raw" ) Boolean isRaw, 
		@FormParam ( "entity" ) List<String> entitites 
	) 
	{
		return getMappings ( isRaw, entitites.toArray ( new String [ 0 ] ) );
	}
	

	@Override
	public EntityMappingSearchResult getMappings ( Boolean wantRawResult, String ... entityIds ) 
	{
		EntityMappingManager emgr = new DbManagerFactory ().newEntityMappingManager ();
		EntityMappingSearchResult result = emgr.getMappings ( wantRawResult, entityIds );
		emgr.close();
		return result; 
	}
	
	
	@Override
	public void storeMappings ( String ... entityIds )
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void storeMappingBundle ( String ... entityIds )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int deleteMappings ( String ... entityIds )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteEntities ( String ... entityIds )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMappingsAs ( String outputFormat, Boolean wantRawResult, String ... entityIds )
	{
		// TODO Auto-generated method stub
		return null;
	}	
	
	/**
	 * Does nothing, it's stateless.
	 */
	@Override
	public void close () {
	}	
	
}
