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
 * TODO: Comment me!
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
				String paramStr = 
					trimToEmpty ( param.getValueType () ) 
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
				String paramStr = 
					trimToEmpty ( param.getValueType () ) 
					+ ":" + trimToEmpty ( param.getValue () )
					+ ":" + trimToEmpty ( param.getExtraValue () );
				req.add ( "param", paramStr );
		}
		
	  return getRawResult ( "/find", req, outputFormat );
	}


	@Override
	public List<ProvenanceRegisterEntry> findEntityMappingProv ( String entityId, List<String> validUsers )
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String findEntityMappingProvAs ( String outputFormat, String entityId, List<String> validUsers )
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Set<List<ProvenanceRegisterEntry>> findMappingProv ( String xEntityId, String yEntityId, List<String> validUsers )
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String findMappingProvAs ( String outputFormat, String xEntityId, String yEntityId, List<String> validUsers )
	{
		// TODO Auto-generated method stub
		return null;
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
	 * This is used by JUnit tests, in order to create test data needed at run time. Please don't use this in production
	 */
	void createTestProvenanceEntries ()
	{
		Form req = prepareReq ();
		invokeVoidWsReq ( "/create-test-entries", req );
	}

}
