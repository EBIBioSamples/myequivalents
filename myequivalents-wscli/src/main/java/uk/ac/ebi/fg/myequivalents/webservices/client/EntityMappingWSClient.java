package uk.ac.ebi.fg.myequivalents.webservices.client;


import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;

import com.sun.jersey.api.representation.Form;

/**
 * TODO: Comment me again! 
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

	  invokeVoidWsReq ( "/store", req );
	}

	@Override
	public void storeMappingBundle ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  invokeVoidWsReq ( "/bundle/store", req );
	}

	@Override
	public int deleteMappings ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  return invokeIntWsReq ( "/delete", req );
	}

	@Override
	public int deleteEntities ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  return invokeIntWsReq ( "/entity/delete", req );
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

	  return invokeWsReq ( "/target/get", req, EntityMappingSearchResult.class );
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
	  
	  return getRawResult ( "/target/get", req, outputFormat );
	}
}
