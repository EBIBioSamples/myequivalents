package uk.ac.ebi.fg.myequivalents.provenance.webservices.client;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;
import static uk.ac.ebi.fg.myequivalents.webservices.server.test.WebTestDataInitializer.editorSecret;
import static uk.ac.ebi.fg.myequivalents.webservices.server.test.WebTestDataInitializer.editorUser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.webservices.server.test.WebTestDataInitializer;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>31 Oct 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvEntityMappingWSClientIT
{
	// Here we get the specific factory, since we want newProvRegistryManager() 
	private ProvManagerFactory mgrFact = Resources.getInstance ( ).getMyEqManagerFactory (
		ProvRegistryWSClientIT.CLI_SPRING_CONFIG_FILE_NAME
	);

	private DateTime startTime;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	@Before
	public void markStartTime ()
	{
		startTime = new DateTime();
	}
	
	
	@After
	public void purgeEntries ()
	{
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager (
			WebTestDataInitializer.adminUser.getEmail (), WebTestDataInitializer.adminSecret
		);
		((ProvRegistryWSClient) regMgr)._purgeAll ( startTime.toDate () );
	}
	
	
	
	@Test
	public void testCreation ()
	{
		// The services we will play with
		Reader xmlIn = new InputStreamReader ( this.getClass ().getResourceAsStream ( "/data/foo_services.xml" ) );
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), editorSecret );
		smgr.storeServicesFromXML ( xmlIn );
		
		// Test mappings
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), editorSecret );
		log.debug ( "Saving test mappings" );
		mapMgr.storeMappings ( "test.provtest.service6:acc1", "test.provtest.service8:acc1" );
		mapMgr.storeMappingBundle ( "test.provtest.service6:acc2", "test.provtest.service8:acc1" );
		log.debug ( "Test mappings saved" );
		
		// Has the above been tracked?
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( editorUser.getEmail (), editorSecret );
		
		List<ProvenanceRegisterEntry> proves = regMgr.find ( editorUser.getEmail (), "mapping.storeMappings", null, null, 
			Arrays.asList ( p ( "entity", "%.provtest.service6", "acc1" ) )
		);

		log.info ( "------ MAPPING RECORDS:\n{}", proves );
		assertEquals ( "Expected provenance records not saved (service6:acc1)!", 1, proves.size () );
		
		proves = regMgr.find ( editorUser.getEmail (), "mapping.storeMapping%", null, null,
			Arrays.asList ( p ( "entity", "%.provtest.service8", "acc1" ) ) 
		);
		log.info ( "------ MAPPING RECORDS:\n{}", proves );
		assertEquals ( "Expected provenance records not saved (service6:acc1)!", 2, proves.size () );
	}


	@Test
	public void testFindEntityProv ()
	{
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), editorSecret );
		
		String sname = "prov.test.service1";
		Service serv = new Service ( sname );
		smgr.storeServices ( serv );

		EntityMappingManager mmgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), editorSecret );
		mmgr.storeMappings ( sname + ":a", sname + ":b" );
		mmgr.storeMappingBundle ( sname + ":b", sname + ":c", sname + ":d" );
		
		
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( editorUser.getEmail (), editorSecret );

		
		// First, return all the entries, no matter the users
		List<ProvenanceRegisterEntry> provs = regMgr.findEntityMappingProv ( sname + ":b", null );
		
		log.info ( "Provenance entries returned for :a:\n{}", provs );
		
		mmgr.deleteMappings ( sname + ":b" );
		smgr.deleteServices ( sname );
		
		boolean founda = false, foundc = false, foundd = false;
		int foundOps = 0, foundEntities = 0;
		for ( ProvenanceRegisterEntry prove: provs )
		{
			foundOps++;
			for ( ProvenanceRegisterParameter param: prove.getParameters () ) 
			{
				if ( !"entity".equals ( param.getValueType () ) ) continue;
				
				foundEntities++;
				founda |= sname.equals ( param.getValue () ) && "a".equals ( param.getExtraValue () );
				foundc |= sname.equals ( param.getValue () ) && "c".equals ( param.getExtraValue () );
				foundd |= sname.equals ( param.getValue () ) && "d".equals ( param.getExtraValue () );
			}
		}
		
		assertEquals ( "Wrong count of found operations!", 2, foundOps );
		assertEquals ( "Wrong count of found entities!", 5, foundEntities );
		assertTrue ( ":a not found!", founda );
		assertTrue ( ":c not found!", foundc );
		assertTrue ( ":d not found!", foundd );
		
		provs = regMgr.findEntityMappingProv ( sname + ":b", Arrays.asList ( editorUser.getEmail () ) );
		assertEquals ( "user filter didn't work!", 2, provs.size () );

		provs = regMgr.findEntityMappingProv ( sname + ":b", Arrays.asList ( "foo.user" ) );
		assertEquals ( "user filter didn't work!", 0, provs.size () );
		
		String provsXml = regMgr.findEntityMappingProvAs ( "xml", sname + ":b", null );
		log.info ( "Provenance-XML:\n{}", provsXml );

		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<provenance>" ) ); 
		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<entry operation=\"mapping.storeMappingBundle\"" ) ); 
		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<parameter extra-value=\"c\"" ) ); 
	}

	
	@Test
	public void testFindMappingProv ()
	{
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), editorSecret );
		
		String sname = "prov.test.service2";
		Service serv = new Service ( sname );
		smgr.storeServices ( serv );

		EntityMappingManager mmgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), editorSecret );
		mmgr.storeMappings ( sname + ":a", sname + ":b" );
		mmgr.storeMappings ( sname + ":b", sname + ":c" );
		//mmgr.storeMappings ( sname + ":c", sname + ":d" );
		mmgr.storeMappingBundle ( sname + ":c", sname + ":e", sname + ":d" );
		mmgr.storeMappings ( sname + ":d", sname + ":a" );
		

		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( editorUser.getEmail (), editorSecret );
		
		// First, return all the entries, no matter the users
		Set<List<ProvenanceRegisterEntry>> provs = regMgr.findMappingProv ( sname + ":a", sname + ":d", null );
		
		// Check
		for ( List<ProvenanceRegisterEntry> provsl: provs )
			log.info ( "Provenance chain returned for a-d:\n{}", provsl );
		
		mmgr.deleteMappings ( sname + ":a" );
		smgr.deleteServices ( sname );
		
		boolean foundab = false, foundbc = false, foundcd = false, foundad = false;
		int foundChains = provs.size (), foundOps = 0, foundEntities = 0;
		for ( List<ProvenanceRegisterEntry> chain: provs )
		{
			for ( ProvenanceRegisterEntry prove: chain )
			{
				foundOps++;
				foundab = foundab || prove.containsParameters ( 
					ProvenanceRegisterParameter.pent ( Arrays.asList ( sname + ":a", sname + ":b" ) ) 
				).size () == 2;

				foundbc = foundbc || prove.containsParameters ( 
					ProvenanceRegisterParameter.pent ( Arrays.asList ( sname + ":b", sname + ":c" ) ) 
				).size () == 2;

				foundcd = foundcd || prove.containsParameters ( 
					ProvenanceRegisterParameter.pent ( Arrays.asList ( sname + ":c", sname + ":d" ) ) 
				).size () == 2;

				foundad = foundad || prove.containsParameters ( 
					ProvenanceRegisterParameter.pent ( Arrays.asList ( sname + ":a", sname + ":d" ) ) 
				).size () == 2;

				for ( ProvenanceRegisterParameter param: prove.getParameters () ) 
					if ( "entity".equals ( param.getValueType () ) ) foundEntities++;
			}
		}
		
		assertEquals ( "Wrong count of found chains!", 2, foundChains );
		assertEquals ( "Wrong count of found operations!", 4, foundOps );
		assertEquals ( "Wrong count of found entities!", 9, foundEntities );
		assertTrue ( "a-b not found!", foundab );
		assertTrue ( "b-c not found!", foundbc );
		assertTrue ( "c-d not found!", foundcd );
		assertTrue ( "a-d not found!", foundad );
		
		provs = regMgr.findMappingProv ( sname + ":a", sname + ":d", Arrays.asList ( editorUser.getEmail () ) );
		assertEquals ( "user filter didn't work!", 2, provs.size () );
		
		provs = regMgr.findMappingProv ( sname + ":a", sname + ":d", Arrays.asList ( "foo-user" ) );
		assertEquals ( "user filter didn't work!", 0, provs.size () );

		String provsXml = regMgr.findMappingProvAs ( "xml", sname + ":a", sname + ":d", null );
		log.info ( "Provenance-XML:\n{}", provsXml );

		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<provenance-entry-lists>" ) ); 
		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<entries>" ) ); 
		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<entry operation=\"mapping.storeMappingBundle\"" ) ); 
		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<parameter extra-value=\"c\"" ) ); 
	}


}
