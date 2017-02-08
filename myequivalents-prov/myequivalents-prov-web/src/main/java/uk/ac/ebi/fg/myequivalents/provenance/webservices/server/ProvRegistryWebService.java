package uk.ac.ebi.fg.myequivalents.provenance.webservices.server;

import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegisterEntryList;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.resources.Const;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter;
import uk.ac.ebi.fg.myequivalents.webservices.server.test.WebTestDataInitializer;

/**
 * The web service that provides the functionalities available from {@link ProvRegistryManager}.
 * See the myequivalents-web module for details.
 *
 * <dl><dt>date</dt><dd>25 Sep 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
@Path ( "/provenance" )
public class ProvRegistryWebService
{
	@Context
	private HttpServletRequest request; 
	
	public static final ProvenanceRegisterEntry e = new ProvenanceRegisterEntry ( 
		"foo.user", "foo.op", p ( "foo.entity", Arrays.asList ( "acc1", "acc2", "acc3" ) )
	);
	public static final ProvenanceRegisterEntry e1 = new ProvenanceRegisterEntry ( 
		"foo.user1", "foo.op1", p ( "foo.entity", Arrays.asList ( "acc1", "acc2", "acc3" ) )
	);
	
	private final Logger log = LoggerFactory.getLogger ( this.getClass () );

	
	@POST
	@Path( "/find" )
	@Produces ( MediaType.APPLICATION_XML )
	public ProvRegisterEntryList find (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "email" ) String userEmail, 
		@FormParam ( "operation" ) String operation, 
		@FormParam ( "from" ) String from, 
		@FormParam ( "to" ) String to, 
		@FormParam ( "param" ) List<String> paramsString
	)
	{
		ProvRegistryManager mgr = getProvRegistryManager ( authEmail, authApiPassword );
		Date dfrom = DateJaxbXmlAdapter.STR2DATE.unmarshal ( from );
		Date dto = DateJaxbXmlAdapter.STR2DATE.unmarshal ( to );
		List<ProvenanceRegisterParameter> params = ProvenanceRegisterParameter.p ( paramsString );

		List<ProvenanceRegisterEntry> result = mgr.find ( userEmail, operation, dfrom, dto, params );
		fetchLazyLinks ( result );
		mgr.close ();
		
		return new ProvRegisterEntryList ( result );
	}
	
	@POST
	@Path( "/find-entity-mapping-prov" )
	@Produces ( MediaType.APPLICATION_XML )
	public ProvRegisterEntryList findEntityMappingProv (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "entity" ) String entityId,
		@FormParam ( "valid-user" ) List<String> validUsers 
	)
	{
		ProvRegistryManager mgr = getProvRegistryManager ( authEmail, authApiPassword );
		List<ProvenanceRegisterEntry> result = mgr.findEntityMappingProv ( entityId, validUsers );
		fetchLazyLinks ( result );
		mgr.close ();
		
		return new ProvRegisterEntryList ( result );
	}

	
	@POST
	@Path( "/find-mapping-prov" )
	@Produces ( MediaType.APPLICATION_XML )
	public ProvRegisterEntryList.ProvRegisterEntryNestedList findMappingProv (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "xentity" ) String xEntityId,
		@FormParam ( "yentity" ) String yEntityId,
		@FormParam ( "valid-user" ) List<String> validUsers 
	)
	{
		ProvRegistryManager mgr = getProvRegistryManager ( authEmail, authApiPassword );
		Set<List<ProvenanceRegisterEntry>> result = mgr.findMappingProv ( xEntityId, yEntityId, validUsers );
		for ( List<ProvenanceRegisterEntry> le: result ) fetchLazyLinks ( le );
		mgr.close ();
		
		return new ProvRegisterEntryList.ProvRegisterEntryNestedList ( result );
	}
	
	
	
	
	@POST
	@Path( "/purge" )
	@Produces ( MediaType.APPLICATION_XML )
	public String purge (
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "from" ) String from, 
		@FormParam ( "to" ) String to 
	)
	{
		ProvRegistryManager mgr = getProvRegistryManager ( authEmail, authApiPassword );

		Date dfrom = DateJaxbXmlAdapter.STR2DATE.unmarshal ( from );
		Date dto = DateJaxbXmlAdapter.STR2DATE.unmarshal ( to );
		
		int result = mgr.purge ( dfrom, dto );
		mgr.close ();
		
		return String.valueOf ( result );
	}

	
	private ProvRegistryManager getProvRegistryManager ( String authEmail, String authApiPassword ) 
	{
		log.trace ( "Returning access manager for the user {}, {}", authEmail, authApiPassword == null ? null: "***" );
		ProvManagerFactory fact = Resources.getInstance ().getMyEqManagerFactory ();
		return fact.newProvRegistryManager ( authEmail, authApiPassword );
	}

	/**
	 * Fix lazy-loading issues, we keep lazy mode for the DB/programmatic interface, here we cannot avoid to
	 * reproduce eager fetching.
	 * 
	 */
	private void fetchLazyLinks ( List<ProvenanceRegisterEntry> entries )
	{
		// TODO: might be faster, using a Jersey filter and delegating it the mgr.close() job.
		for ( ProvenanceRegisterEntry e: entries ) {
			List<ProvenanceRegisterParameter> ep = e.getParameters ();
			if ( ep != null ) ep.size ();
		}
	}
	
	
	/**
	 * This is used by JUnit tests in the client, creates test provenance entries, for which operation no interface exists
	 * in myEquivalents (every provenance record is auto-created upon myEq operations).
	 * 
	 * This should not be used outside tests and its execution is restricted to the 
	 * {@link Const#PROP_NAME_TEST_FLAG} property set to true, plus localhost-only invocation.
	 *  
	 */
	@POST
	@Path( "/create-test-entries" )
	@Produces ( MediaType.APPLICATION_XML )
	public void _createTestProvenanceEntries 
	(
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword
	)
	{
		if ( !"true".equals ( System.getProperty ( Const.PROP_NAME_TEST_FLAG, null ) ) )  throw new SecurityException ( 
			"/provenance/create-test-entries can only be invoked when " + Const.PROP_NAME_TEST_FLAG + " is true"
		);

		// Only from localhost
		if ( request == null || !"127.0.0.1".equals ( request.getRemoteAddr () ) ) throw new SecurityException (
			"/provenance/create-test-entries is for testing purposes only and can only be invoked from localhost"
		);
		
		// Only editors can do this
		getProvRegistryManager ( authEmail, authApiPassword );
		
		log.info ( "\n\n __________________ Creating Test Provenance Data ____________________ \n\n\n" );
		ManagerFactory mgrf = Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = ((DbManagerFactory) mgrf).getEntityManagerFactory ().createEntityManager ();
		
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );
		
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		provDao.create ( e );
		provDao.create ( e1 );
		ts.commit ();
		em.close ();
	}
	
	
	
	/**
	 * This is used by JUnit tests in the client, does a complete cleaning of provenance entries stored from the date 
	 * parameter.
	 * 
	 * This should not be used outside tests and its execution is restricted to the 
	 * {@link Const#PROP_NAME_TEST_FLAG} property set to true, plus localhost-only invocation.
	 *  
	 */
	@POST
	@Path( "/purge-all" )
	@Produces ( MediaType.APPLICATION_XML )
	public String _purgeAll 
	(
		@FormParam ( "login" ) String authEmail, 
		@FormParam ( "login-secret" ) String authApiPassword,
		@FormParam ( "from" ) String from 
	)
	{
		if ( !"true".equals ( System.getProperty ( Const.PROP_NAME_TEST_FLAG, null ) ) )  throw new SecurityException ( 
			"/provenance/create-test-entries can only be invoked when " + Const.PROP_NAME_TEST_FLAG + " is true"
		);

		// Only from localhost
		if ( request == null || !"127.0.0.1".equals ( request.getRemoteAddr () ) ) throw new SecurityException (
			"/provenance/create-test-entries is for testing purposes only and can only be invoked from localhost"
		);
		
		// Only editors can do this
		getProvRegistryManager ( authEmail, authApiPassword );
		
		Date dfrom = DateJaxbXmlAdapter.STR2DATE.unmarshal ( from );

		ManagerFactory mgrf = Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = ((DbManagerFactory) mgrf).getEntityManagerFactory ().createEntityManager ();
		
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );
		
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		int result = provDao.purgeAll ( dfrom, null );
		ts.commit ();
		em.close ();
		
		return String.valueOf ( result );
	}
}
