package uk.ac.ebi.fg.myequivalents.webservices.client;


import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
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
	public User setAuthenticationCredentials ( String email, String apiPassword, boolean connectServer ) throws SecurityException
	{
		this.email = StringUtils.trimToNull ( email );
		this.apiPassword = StringUtils.trimToNull ( apiPassword );
		if ( !connectServer ) return null;
					
		Form req = prepareReq ();
		return invokeWsReq ( "/login", req, User.class );
	}

	@Override
	public void storeMappings ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  invokeWsReq ( "/store-mappings", req );
	}

	@Override
	public void storeMappingBundle ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  invokeWsReq ( "/store-bundle", req );
	}

	@Override
	public int deleteMappings ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  return invokeWsReq ( "/delete-mappings", req, Integer.class );
	}

	@Override
	public int deleteEntities ( String ... entityIds )
	{
		Form req = prepareReq ();
	  for ( String eid: entityIds ) req.add ( "entity", eid );

	  return invokeWsReq ( "/delete-entities", req, Integer.class );
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

//	private void throwUnsupportedException () 
//	{
//		throw new UnsupportedOperationException ( 
//			"This operation from the WS client is not implemented yet. Please ask developers" );
//	}
}
