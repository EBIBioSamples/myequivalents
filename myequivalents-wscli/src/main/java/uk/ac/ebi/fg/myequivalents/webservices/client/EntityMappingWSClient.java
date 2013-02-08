/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.webservices.client;


import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;

/**
 * The myequivalents web-service client. This can be used to access myequivalents web-services. 
 * 
 * <dl><dt>date</dt><dd>Oct 1, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class EntityMappingWSClient implements EntityMappingManager
{
	private final String baseUrl;	
	
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	
	/**
	 * All the invocations provided by this client will be routed at the web service base address passed here. The default
	 * it 'http://localhost:8080/ws', /ws is usually the path where the web service package locates its implementation. 
	 */
	public EntityMappingWSClient ( String baseUrl )
	{
		super ();
		baseUrl = StringUtils.trimToNull ( baseUrl );
		if ( baseUrl == null ) baseUrl = "http://localhost:8080/myequivalents/ws";
		else if ( baseUrl.charAt ( baseUrl.length () - 1 ) == '/' ) baseUrl = baseUrl.substring ( 0, baseUrl.length () - 2 );
		
		this.baseUrl = baseUrl;
	}
	
	public EntityMappingWSClient () {
		this ( null );
	}

	private void throwUnsupportedException () {
		throw new UnsupportedOperationException ( 
			"This operation from the WS client is not implemented yet. Please ask developers" );
	}
	

	/**
	 * TODO: NOT IMPLEMENTED YET, THEY RAISE AN EXCEPTION
	 */
	@Override
	public void storeMappings ( String ... entityIds )
	{
		throwUnsupportedException ();		
	}

	@Override
	public void storeMappingBundle ( String ... entityIds )
	{
		throwUnsupportedException ();		
	}

	@Override
	public int deleteMappings ( String ... entityIds )
	{
		throwUnsupportedException ();
		return 0;
	}

	@Override
	public int deleteEntities ( String ... entityIds )
	{
		throwUnsupportedException ();
		return 0;
	}

	@Override
	public EntityMappingSearchResult getMappings ( Boolean wantRawResult, String ... entityIds )
	{
		try
		{
			Form req = new Form ();
		  req.add ( "raw", wantRawResult.toString () );
		  for ( String eid: entityIds )
		  	req.add ( "entity", eid );
			
			if ( log.isTraceEnabled () ) log.trace ( "requested web service\n: " + req );

			// DEBUG
			//try { while ( "".equals ( "" ) ) Thread.sleep ( 3000 ); } catch ( InterruptedException ex ) { throw new RuntimeException ( ex ); }

			Client cli = Client.create ();
			WebResource webres = cli.resource ( this.baseUrl + "/mapping/get" );
			
			return webres
				.accept( MediaType.APPLICATION_XML_TYPE )
			  .post ( EntityMappingSearchResult.class, req );
		} 
		catch ( Exception ex )
		{
			throw new RuntimeException ( 
				"Internal error while invoking the myequivalents web-service: " + ex.getMessage (), ex 
			);
		}
	}

	@Override
	public String getMappingsAs ( String outputFormat, Boolean wantRawResult, String ... entityIds )
	{
		throwUnsupportedException ();
		return null;
	}


	@Override
	public EntityMappingSearchResult getMappingsForTarget ( Boolean wantRawResult, String targetServiceName, String entityId )
	{
		try
		{
			Form req = new Form ();
		  req.add ( "raw", wantRawResult.toString () );
		  req.add ( "service", targetServiceName );
		  req.add ( "entity", entityId );
			
			if ( log.isTraceEnabled () ) log.trace ( "requested web service\n: " + req );

			// DEBUG
			//try { while ( "".equals ( "" ) ) Thread.sleep ( 3000 ); } catch ( InterruptedException ex ) { throw new RuntimeException ( ex ); }

			Client cli = Client.create ();
			WebResource webres = cli.resource ( this.baseUrl + "/mapping/get-target" );
			
			return webres
				.accept( MediaType.APPLICATION_XML_TYPE )
			  .post ( EntityMappingSearchResult.class, req );
		} 
		catch ( Exception ex )
		{
			throw new RuntimeException ( 
				"Internal error while invoking the myequivalents web-service: " + ex.getMessage (), ex 
			);
		}	
	}

	@Override
	public String getMappingsForTargetAs ( String outputFormat, Boolean wantRawResult, String targetServiceName, String entityId )
	{
		throwUnsupportedException ();
		return null;
	}

	/** 
	 * Does nothing, it's stateless.
	 */
	@Override
	public void close () {
	}

}
