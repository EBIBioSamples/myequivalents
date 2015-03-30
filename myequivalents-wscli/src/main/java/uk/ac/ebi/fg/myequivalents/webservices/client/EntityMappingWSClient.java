package uk.ac.ebi.fg.myequivalents.webservices.client;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
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
import uk.ac.ebi.fg.myequivalents.utils.jaxb.JAXBUtils;
import uk.ac.ebi.utils.io.IOUtils;

import com.sun.jersey.api.representation.Form;

/**
 * The web service client implementation of {@link EntityMappingManager}.
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
	public void storeMappingBundles ( EntityMappingSearchResult mappings )
	{
		storeMappingBundlesFromXML ( 
			new StringReader ( JAXBUtils.marshal ( mappings, EntityMappingSearchResult.class ) )
		);
	}


	@Override
	public void storeMappingBundlesFromXML ( Reader reader )
	{
		try
		{
			String mappingsXml = IOUtils.readInputFully ( reader ); 
			Form req = prepareReq ();
			req.add ( "mappings-xml", mappingsXml );
			invokeVoidWsReq ( "/bundles/store", req );
		}
		catch ( IOException ex )
		{
			throw new RuntimeException ( 
				"Error while invoking the myEq web service for 'bundles/store': " + ex.getMessage (), ex 
			);
		}
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
	 * This rebuilds links like the one between {@link Entity} and {@link Service}, by looking at objects in the 
	 * flat XML that is returned by the web service. 
	 * 
	 * This will also reconstruct {@link Bundle bundles} in emsr, using fictitious identifiers, which replace the ones
	 * actually stored on the server storage back end. This shouldn't be a problem, since the clients should consider
	 * these IDs opaque and volatile.  
	 * 
	 */
	private EntityMappingSearchResult rebuildEntityMappingLinks ( EntityMappingSearchResult emsr )
	{
		if ( emsr == null ) return null;
		
		// Do you have more than one mapping?
		
		Set<Service> orgServs = emsr.getServices ();
		if ( orgServs == null ) return emsr;
		
		// OK, first let's index that
	  Map<String, Service> servs = new HashMap<String, Service> ();
	  for ( Service s: orgServs ) servs.put ( s.getName (), s );

		Map<String, Repository> repos = new HashMap<String, Repository> ();
	  for ( Repository repo: emsr.getRepositories () ) repos.put ( repo.getName (), repo );
	  
	  Map<String, ServiceCollection> scs = new HashMap<String, ServiceCollection> ();
	  for ( ServiceCollection sc: emsr.getServiceCollections () ) scs.put ( sc.getName (), sc );

	  // Do you *really* have something?
	  if ( servs.isEmpty () && repos.isEmpty () && scs.isEmpty () ) return emsr;
	  
	  // If yes, let's rebuild entity mappings with proper links
	  List<EntityMapping> newEms = new LinkedList<EntityMapping> ();
	  int bid = 0; // It's OK to have fictitious bundlle IDs, you're not supposed to mess up with these anyway

		for ( Bundle b: emsr.getBundles () )
		{
			String bidStr = Integer.toString ( bid++ );
			for ( Entity e : b.getEntities () )
			{
				// Try with either the service or its name reference
				Service s = e.getService ();
				if ( s == null ) s = servs.get ( e.getServiceName () );
				
				// Create the new entity mapping and clone from the old one.
				EntityMapping newEm = new EntityMapping ( s, e.getAccession (), bidStr );
				newEm.setPublicFlag ( e.getPublicFlag () );
				newEm.setReleaseDate ( e.getReleaseDate () );
				newEms.add ( newEm );
			}
		}

		// Rebuild the service->repository relationship
		for ( Service s: orgServs ) 
		{
			String rname = s.getRepositoryName ();
			if ( rname != null ) s.setRepository ( repos.get ( rname ) );
			
			String scname = s.getServiceCollectionName ();
			if ( scname != null ) s.setServiceCollection ( scs.get ( scname ) );
		}
		
		// Whoaa! Last bit and return
		EntityMappingSearchResult result = new EntityMappingSearchResult ( false );
		result.addAllEntityMappings ( newEms );
		return result;
	}
}
