package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.testSecret;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.editorUser;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
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

		out.println ( "------ MAPPING RECORDS: " + proves );
		assertEquals ( "Expected provenance records not saved (service6:acc1)!", 1, proves.size () );
		
		proves = provDao.find ( editorUser.getEmail (), "mapping.storeMapping%", 
			Arrays.asList ( p ( "entity", "%.service8", "acc1" ) ) 
		);
		out.println ( "------ MAPPING RECORDS: " + proves );

		// To check that lazy collections still works, which can only happen if they were fetched before closing (as it is
		// triggered by toString() above)
		em.close (); 
		out.println ( "------ MAPPING RECORDS (after EM closing): " + proves );
		assertEquals ( "Expected provenance records not saved (service6:acc1)!", 2, proves.size () );
	}
	
}
