package uk.ac.ebi.fg.myequivalents.webservices.client;


import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;

import com.sun.jersey.api.representation.Form;

/**
 * The myequivalents web-service client. This can be used to access myequivalents web-services. 
 * 
 * <dl><dt>date</dt><dd>Oct 1, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class EntityMappingWSClient extends MyEquivalentsWSClient implements EntityMappingManager
{

	public EntityMappingWSClient ()
	{
		super ();
	}


	public EntityMappingWSClient ( String baseUrl )
	{
		super ( baseUrl );
	}


	@Override
	protected String getServicePath () {
		return "/mapping";
	}

	@Override
	public void storeMappings ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  invokeVoidWsReq ( "/store-mappings", req );
	}

	@Override
	public void storeMappingBundle ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  invokeVoidWsReq ( "/store-bundle", req );
	}

	@Override
	public int deleteMappings ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  return invokeIntWsReq ( "/delete-mappings", req );
	}

	@Override
	public int deleteEntities ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  return invokeIntWsReq ( "/delete-entities", req );
	}

	@Override
	public EntityMappingSearchResult getMappings ( Boolean wantRawResult, String ... entityIds )
	{
		Form req = prepareReq ();
	  req.add ( "raw", wantRawResult.toString () );
	  for ( String eid: entityIds ) req.add ( "entity", eid );

		return invokeWsReq ( "/get", req, EntityMappingSearchResult.class );
	}

	@Override
	public EntityMappingSearchResult getMappingsForTarget ( Boolean wantRawResult, String targetServiceName, String entityId )
	{
		Form req = prepareReq ();
	  req.add ( "raw", wantRawResult.toString () );
	  req.add ( "service", targetServiceName );
	  req.add ( "entity", entityId );

	  return invokeWsReq ( "/get-target", req, EntityMappingSearchResult.class );
	}

	@Override
	public String getMappingsAs ( String outputFormat, Boolean wantRawResult, String ... entityIds )
	{
		Form req = prepareReq ();
	  req.add ( "raw", wantRawResult.toString () );
	  for ( String eid: entityIds ) req.add ( "entity", eid );
	  
	  return getRawResult ( "/get", req, outputFormat );
	}
	
	@Override
	public String getMappingsForTargetAs ( String outputFormat, Boolean wantRawResult, String targetServiceName, String entityId )
	{
		Form req = prepareReq ();
	  req.add ( "raw", wantRawResult.toString () );
	  req.add ( "service", targetServiceName );
	  req.add ( "entity", entityId );
	  
	  return getRawResult ( "/get-target", req, outputFormat );
	}
}
