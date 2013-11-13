package uk.ac.ebi.fg.myequivalents.webservices.client;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

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

		return invokeMappingGetWsReq ( "/get", req );
	}

	@Override
	public EntityMappingSearchResult getMappingsForTarget ( Boolean wantRawResult, String targetServiceName, String entityId )
	{
		Form req = prepareReq ();
	  req.add ( "raw", wantRawResult.toString () );
	  req.add ( "service", targetServiceName );
	  req.add ( "entity", entityId );

	  return invokeMappingGetWsReq ( "/target/get", req );
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

	
	private EntityMappingSearchResult invokeMappingGetWsReq ( String reqPath, Form req )
	{
		return rebuildEntityMappingLinks ( invokeWsReq ( reqPath, req, EntityMappingSearchResult.class ) );
	}


	/**
	 * TODO: comment me!
	 */
	private EntityMappingSearchResult rebuildEntityMappingLinks ( EntityMappingSearchResult emsr )
	{
		if ( emsr == null ) return null;
		
		Set<Service> orgServs = emsr.getServices ();
		if ( orgServs == null ) return emsr;
		
	  Map<String, Service> servs = new HashMap<String, Service> ();
	  for ( Service s: orgServs ) servs.put ( s.getName (), s );

		Map<String, Repository> repos = new HashMap<String, Repository> ();
	  for ( Repository repo: emsr.getRepositories () ) repos.put ( repo.getName (), repo );
	  
	  Map<String, ServiceCollection> scs = new HashMap<String, ServiceCollection> ();
	  for ( ServiceCollection sc: emsr.getServiceCollections () ) scs.put ( sc.getName (), sc );

	  if ( servs.isEmpty () && repos.isEmpty () && scs.isEmpty () ) return emsr;
	  
	  List<EntityMapping> newEms = new LinkedList<EntityMapping> ();
	  
	  int bid = 0;
		for ( Bundle b: emsr.getBundles () )
		{
			String bidStr = Integer.toString ( bid++ );
			for ( Entity e : b.getEntities () )
			{
				Service s = e.getService ();
				if ( s == null ) s = servs.get ( e.getServiceName () );
				newEms.add ( new EntityMapping ( s, e.getAccession (), bidStr ) );
			}
		}
		
		EntityMappingSearchResult result = new EntityMappingSearchResult ( false );
		
		for ( Service s: orgServs ) 
		{
			String rname = s.getRepositoryName ();
			if ( rname != null ) s.setRepository ( repos.get ( rname ) );
			
			String scname = s.getServiceCollectionName ();
			if ( scname != null ) s.setServiceCollection ( scs.get ( scname ) );
		}
		
		result.addAllEntityMappings ( newEms );
		
		return result;
	}
}
