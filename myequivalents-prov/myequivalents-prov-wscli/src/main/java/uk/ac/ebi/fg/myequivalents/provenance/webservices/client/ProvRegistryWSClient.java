package uk.ac.ebi.fg.myequivalents.provenance.webservices.client;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.sun.jersey.api.representation.Form;

import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegisterEntryList;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter;
import uk.ac.ebi.fg.myequivalents.webservices.client.MyEquivalentsWSClient;

/**
 * The web service client implementation of {@link ProvRegistryManager}.
 *
 * <dl><dt>date</dt><dd>26 Sep 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvRegistryWSClient extends MyEquivalentsWSClient implements ProvRegistryManager
{
	public ProvRegistryWSClient ()
	{
		super ();
	}


	public ProvRegistryWSClient ( String baseUrl )
	{
		super ( baseUrl );
	}


	@Override
	protected String getServicePath () {
		return "/provenance";
	}

	

	@Override
	public List<ProvenanceRegisterEntry> find ( 
		String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params 
	)
	{
		Form req = prepareReq ();
		if ( email != null ) req.add ( "email", userEmail );
		if ( operation != null ) req.add ( "operation", operation );
		
		String fromStr = DateJaxbXmlAdapter.STR2DATE.marshal ( from ) ;
		if ( fromStr != null ) req.add ( "from", fromStr );

		String toStr = DateJaxbXmlAdapter.STR2DATE.marshal ( to );
		req.add ( "to", toStr );

		if ( params != null && params.size () != 0 )
			for ( ProvenanceRegisterParameter param: params )
			{
				String paramStr = trimToEmpty ( param.getValueType () ) 
					+ ":" + trimToEmpty ( param.getValue () )
					+ ":" + trimToEmpty ( param.getExtraValue () );
				req.add ( "param", paramStr );
		}
		
		ProvRegisterEntryList result = invokeWsReq ( "/find", req, ProvRegisterEntryList.class );
		
		if ( result == null ) return Collections.emptyList ();
		return result.getEntries ();
	}


	@Override
	public String findAs ( 
		String outputFormat, String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params 
	)
	{
		Form req = prepareReq ();
		if ( email != null ) req.add ( "email", userEmail );
		if ( operation != null ) req.add ( "operation", operation );
		
		String fromStr = DateJaxbXmlAdapter.STR2DATE.marshal ( from ) ;
		if ( fromStr != null ) req.add ( "from", fromStr );

		String toStr = DateJaxbXmlAdapter.STR2DATE.marshal ( to );
		req.add ( "to", toStr );

		if ( params != null && params.size () != 0 )
			for ( ProvenanceRegisterParameter param: params )
			{
				String paramStr = trimToEmpty ( param.getValueType () ) 
					+ ":" + trimToEmpty ( param.getValue () )
					+ ":" + trimToEmpty ( param.getExtraValue () );
				req.add ( "param", paramStr );
		}
		
	  return getRawResult ( "/find", req, outputFormat );
	}


	@Override
	public List<ProvenanceRegisterEntry> findEntityMappingProv ( String entityId, List<String> validUsers )
	{
		Form req = prepareReq ();
		
		req.add ( "entity", entityId );
		if ( validUsers != null ) for ( String u: validUsers )
			req.add ( "valid-user", u );
		
		ProvRegisterEntryList result = invokeWsReq ( "/find-entity-mapping-prov", req, ProvRegisterEntryList.class );

		if ( result == null ) return Collections.emptyList ();
		return result.getEntries ();
	}


	@Override
	public String findEntityMappingProvAs ( String outputFormat, String entityId, List<String> validUsers )
	{
		Form req = prepareReq ();

		req.add ( "entity", entityId );
		if ( validUsers != null ) for ( String u: validUsers )
			req.add ( "valid-user", u );
		
		return getRawResult ( "/find-entity-mapping-prov", req, outputFormat );
	}


	@Override
	public Set<List<ProvenanceRegisterEntry>> findMappingProv ( String xEntityId, String yEntityId, List<String> validUsers )
	{
		Form req = prepareReq ();
		req.add ( "xentity", xEntityId );
		req.add ( "yentity", yEntityId );
		if ( validUsers != null ) for ( String u: validUsers )
			req.add ( "valid-user", u );
		
		ProvRegisterEntryList.ProvRegisterEntryNestedList result = invokeWsReq ( 
			"/find-mapping-prov", req, ProvRegisterEntryList.ProvRegisterEntryNestedList.class 
		);

		if ( result == null ) return Collections.emptySet ();
		return result.getEntryListsUnwrapped ();
	}


	@Override
	public String findMappingProvAs ( String outputFormat, String xEntityId, String yEntityId, List<String> validUsers )
	{
		Form req = prepareReq ();
		req.add ( "xentity", xEntityId );
		req.add ( "yentity", yEntityId );
		if ( validUsers != null ) for ( String u: validUsers )
			req.add ( "valid-user", u );
		
		return getRawResult ( 
			"/find-mapping-prov", req, outputFormat
		);
	}


	@Override
	public int purge ( Date from, Date to )
	{
		Form req = prepareReq ();

		String fromStr = DateJaxbXmlAdapter.STR2DATE.marshal ( from ) ;
		if ( fromStr != null ) req.add ( "from", fromStr );

		String toStr = DateJaxbXmlAdapter.STR2DATE.marshal ( to );
		req.add ( "to", toStr );
		
	  return invokeIntWsReq ( "/purge", req );
	}
	
	/**
	 * This is used by JUnit tests, in order to create test data needed at run time. Please don't use this in production.
	 */
	void _createTestProvenanceEntries ()
	{
		Form req = prepareReq ();
		invokeVoidWsReq ( "/create-test-entries", req );
	}

	/**
	 * This is used by JUnit tests, in order to create test data needed at run time. Please don't use this in production.
	 */
	int _purgeAll ( Date from )
	{
		Form req = prepareReq ();

		String fromStr = DateJaxbXmlAdapter.STR2DATE.marshal ( from ) ;
		if ( fromStr != null ) req.add ( "from", fromStr );
		
		return invokeIntWsReq ( "/purge-all", req );
	}

}
