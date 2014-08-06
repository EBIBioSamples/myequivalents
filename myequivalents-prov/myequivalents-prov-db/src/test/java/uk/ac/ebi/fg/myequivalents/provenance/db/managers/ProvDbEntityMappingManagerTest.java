package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.testSecret;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.editorUser;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>16 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbEntityMappingManagerTest
{
	private final Logger log = LoggerFactory.getLogger ( this.getClass () );

	@BeforeClass
	public static void init ()
	{
		ProvDbServiceManagerTest.init ();
	}
	
	@AfterClass
	public static void cleanUp ()
	{
		ProvDbServiceManagerTest.cleanUp ();
	}
	
	@Test
	public void testCreation ()
	{
		DbManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();

		// The services we will play with
		Reader xmlIn = new InputStreamReader ( this.getClass ().getResourceAsStream ( "/data/foo_services.xml" ) );
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		smgr.storeServicesFromXML ( xmlIn );
		
		// Test mappings
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		mapMgr.storeMappings ( "test.testmain.service6:acc1", "test.testmain.service8:acc1" );
		mapMgr.storeMappingBundle ( "test.testmain.service6:acc2", "test.testmain.service8:acc1" );
		
		// Has the above been tracked?
		em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );
		List<ProvenanceRegisterEntry> proves = provDao.find ( editorUser.getEmail (), "mapping.storeMappings", 
			Arrays.asList ( p ( "entity", "%.service6", "acc1" ) )
		);

		log.info ( "------ MAPPING RECORDS:\n{}", proves );
		assertEquals ( "Expected provenance records not saved (service6:acc1)!", 1, proves.size () );
		
		proves = provDao.find ( editorUser.getEmail (), "mapping.storeMapping%", 
			Arrays.asList ( p ( "entity", "%.service8", "acc1" ) ) 
		);
		log.info ( "------ MAPPING RECORDS:\n{}", proves );

		// To check that lazy collections still works, which can only happen if they were fetched before closing (as it is
		// triggered by toString() above)
		em.close (); 
		log.info ( "------ MAPPING RECORDS (after EM closing):\n{}", proves );
		assertEquals ( "Expected provenance records not saved (service6:acc1)!", 2, proves.size () );
		
	}

	@Test
	public void testFindEntityProv ()
	{
		DbManagerFactory mgrFact = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		
		String sname = "prov.test.service1";
		Service serv = new Service ( sname );
		smgr.storeServices ( serv );

		EntityMappingManager mmgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		mmgr.storeMappings ( sname + ":a", sname + ":b" );
		mmgr.storeMappingBundle ( sname + ":b", sname + ":c", sname + ":d" );
		
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager (); 
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em ); // TODO: use the manager

		// First, return all the entries, no matter the users
		List<ProvenanceRegisterEntry> provs = provDao.findEntityMappingProv ( sname + ":b", null );
		
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
		
		provs = provDao.findEntityMappingProv ( sname + ":b", Arrays.asList ( editorUser.getEmail () ) );
		assertEquals ( "user filter didn't work!", 2, provs.size () );

		provs = provDao.findEntityMappingProv ( sname + ":b", Arrays.asList ( "foo.user" ) );
		assertEquals ( "user filter didn't work!", 0, provs.size () );
	}

	
	@Test
	public void testFindMappingProv ()
	{
		DbManagerFactory mgrFact = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		
		String sname = "prov.test.service1";
		Service serv = new Service ( sname );
		smgr.storeServices ( serv );

		EntityMappingManager mmgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		mmgr.storeMappings ( sname + ":a", sname + ":b" );
		mmgr.storeMappings ( sname + ":b", sname + ":c" );
		//mmgr.storeMappings ( sname + ":c", sname + ":d" );
		mmgr.storeMappingBundle ( sname + ":c", sname + ":e", sname + ":d" );
		mmgr.storeMappings ( sname + ":d", sname + ":a" );
		
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager (); 
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em ); // TODO: use the manager

		// First, return all the entries, no matter the users
		Set<List<ProvenanceRegisterEntry>> provs = provDao.findMappingProv ( sname + ":a", sname + ":d", null );
		
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
		
		provs = provDao.findMappingProv ( sname + ":a", sname + ":d", Arrays.asList ( editorUser.getEmail () ) );
		assertEquals ( "user filter didn't work!", 2, provs.size () );
	}
}
