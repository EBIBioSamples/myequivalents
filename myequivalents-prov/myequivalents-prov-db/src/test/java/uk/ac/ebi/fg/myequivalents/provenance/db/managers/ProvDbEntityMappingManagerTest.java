package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.editorUser;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.testSecret;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.pent;
import static uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver.buildUriFromAcc;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.DbEntityIdResolver;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;

/**
 * Tests  {@link ProvDbEntityMappingManager}
 *
 * <dl><dt>date</dt><dd>16 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbEntityMappingManagerTest
{
	private final Logger log = LoggerFactory.getLogger ( this.getClass () );

	@Before
	public void init ()
	{
		ProvDbServiceManagerTest.init ();
	}
	
	@After
	public void cleanUp ()
	{
		ProvDbServiceManagerTest.cleanUp ();
	}
	
	@Test
	public void testCreation ()
	{
		ProvDbManagerFactory mgrFact = (ProvDbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();

		// The services we will play with
		Reader xmlIn = new InputStreamReader ( this.getClass ().getResourceAsStream ( "/data/foo_services.xml" ) );
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		smgr.storeServicesFromXML ( xmlIn );
		
		// Test mappings
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		log.debug ( "Saving test mappings" );
		mapMgr.storeMappings ( "test.testmain.service6:acc1", "test.testmain.service8:acc1" );
		mapMgr.storeMappingBundle ( "test.testmain.service6:acc2", "test.testmain.service8:acc1" );
		log.debug ( "Test mappings saved" );
		
		// Has the above been tracked?
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( editorUser.getEmail (), testSecret );
		
		List<ProvenanceRegisterEntry> proves = regMgr.find ( editorUser.getEmail (), "mapping.storeMappings", null, null, 
			Arrays.asList ( p ( "entity", "%.service6", "acc1" ) )
		);

		log.info ( "------ MAPPING RECORDS:\n{}", proves );
		assertEquals ( "Expected provenance records not saved (service6:acc1)!", 1, proves.size () );
		
		proves = regMgr.find ( editorUser.getEmail (), "mapping.storeMapping%", null, null,
			Arrays.asList ( p ( "entity", "%.service8", "acc1" ) ) 
		);
		log.info ( "------ MAPPING RECORDS:\n{}", proves );
		assertEquals ( "Expected provenance records not saved (service6:acc1)!", 2, proves.size () );
	}

	@Test
	public void testFindEntityProv ()
	{
		ProvDbManagerFactory mgrFact = (ProvDbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		
		String sname = "prov.test.service1";
		Service serv = new Service ( sname );
		smgr.storeServices ( serv );

		EntityMappingManager mmgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		mmgr.storeMappings ( sname + ":a", sname + ":b" );
		mmgr.storeMappingBundle ( sname + ":b", sname + ":c", sname + ":d" );
		
		
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( editorUser.getEmail (), testSecret );

		
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
		ProvDbManagerFactory mgrFact = (ProvDbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		EntityIdResolver entityIdResolver = new DbEntityIdResolver ( mgrFact.getEntityManagerFactory ().createEntityManager () );
		
		String sname = "prov.test.service1";
		Service serv = new Service ( sname );
		smgr.storeServices ( serv );

		EntityMappingManager mmgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		mmgr.storeMappings ( sname + ":a", sname + ":b" );
		mmgr.storeMappings ( sname + ":b", sname + ":c" );
		//mmgr.storeMappings ( sname + ":c", sname + ":d" );
		mmgr.storeMappingBundle ( sname + ":c", sname + ":e", sname + ":d" );
		mmgr.storeMappings ( sname + ":d", sname + ":a" );

		mmgr.deleteMappings ( sname + ":a" );
		smgr.deleteServices ( sname );
		mmgr.close ();
		
		
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( editorUser.getEmail (), testSecret );
		
		// First, return all the entries, no matter the users
		Set<List<ProvenanceRegisterEntry>> provs = regMgr.findMappingProv ( sname + ":a", sname + ":d", null );
		
		// Check
		for ( List<ProvenanceRegisterEntry> provsl: provs )
			log.info ( "Provenance chain returned for a-d:\n{}", provsl );
		
		
		boolean foundab = false, foundbc = false, foundcd = false, foundad = false;
		int foundChains = provs.size (), foundOps = 0, foundEntities = 0;
		for ( List<ProvenanceRegisterEntry> chain: provs )
		{
			for ( ProvenanceRegisterEntry prove: chain )
			{
				foundOps++;
				foundab = foundab || prove.containsParameters ( 
					ProvenanceRegisterParameter.pent ( entityIdResolver, Arrays.asList ( sname + ":a", sname + ":b" ) ) 
				).size () == 2;

				foundbc = foundbc || prove.containsParameters ( 
					ProvenanceRegisterParameter.pent ( entityIdResolver, Arrays.asList ( sname + ":b", sname + ":c" ) ) 
				).size () == 2;

				foundcd = foundcd || prove.containsParameters ( 
					ProvenanceRegisterParameter.pent ( entityIdResolver, Arrays.asList ( sname + ":c", sname + ":d" ) ) 
				).size () == 2;

				foundad = foundad || prove.containsParameters ( 
					ProvenanceRegisterParameter.pent ( entityIdResolver, Arrays.asList ( sname + ":a", sname + ":d" ) ) 
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
	
	
	@Test
	public void testUris ()
	{
		ProvDbManagerFactory mgrFact = (ProvDbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		EntityIdResolver entityIdResolver = new DbEntityIdResolver ( mgrFact.getEntityManagerFactory ().createEntityManager () );

		Service serv = new Service ( "foo.uri.test" );
		serv.setUriPattern ( "foo://test.uri/$id" );
		smgr.storeServices ( serv );

		
		smgr.storeServices ( Service.UNSPECIFIED_SERVICE );
		EntityMappingManager mmgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		
		String uri1 = "http://foo.web.uri/test/acc1", uri2 = "lsid://another.foo.uri/acc2";
		mmgr.storeMappings ( 
			"<" + uri1 + ">", ":<" + uri2 + ">",
			serv.getName () + ":acc3", "<" + buildUriFromAcc ( serv.getName (), "acc4" + ">" )
		);
		
		// Verify provenance
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( editorUser.getEmail (), testSecret );
		List<ProvenanceRegisterEntry> proves = regMgr.find ( editorUser.getEmail (), "mapping.storeMappings", null, null, 
			Arrays.asList ( pent ( Service.UNSPECIFIED_SERVICE_NAME, uri2 ) )
		);
		log.info ( "------ MAPPING RECORDS (_:URI2):\n{}", proves );
		assertEquals ( "Expected provenance records not saved (_:URI2)!", 1, proves.size () );
		
		proves = regMgr.find ( editorUser.getEmail (), "mapping.storeMappings", null, null, 
			Arrays.asList ( pent ( serv.getName (), "acc3" ) )
		);
		log.info ( "------ MAPPING RECORDS (serv:acc) :\n{}", proves );
		assertEquals ( "Expected provenance records not saved (serv:acc)!", 1, proves.size () );
		
		proves = regMgr.find ( editorUser.getEmail (), "mapping.storeMappings", null, null, 
			Arrays.asList ( pent ( entityIdResolver, "<" + buildUriFromAcc ( serv.getName (), "acc4" + ">" ) ) )
		);
		log.info ( "------ MAPPING RECORDS (uriri(serv:acc)):\n{}", proves );
		assertEquals ( "Expected provenance records not saved uri(serv:acc)!", 1, proves.size () );
	}
}
