package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.adminUser;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.editorUser;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.testPass;
import static uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbServiceManagerTest.testSecret;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * Tests the functionality of {@link AccessControlManager}
 *
 * <dl><dt>date</dt><dd>16 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbAccessControlManagerTest
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
	public void testUsers ()
	{
		DbManagerFactory mgrFact = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		
		AccessControlManager accMgr = mgrFact.newAccessControlManagerFullAuth ( adminUser.getEmail (), testPass );
		
		User user = new User ( 
			"test.new.user", "Test New", "User", "test.pwd", null, Role.VIEWER, "test.secret" 
		);
		accMgr.storeUser ( user );

		// Has the above been tracked?
		em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );
		List<ProvenanceRegisterEntry> proves = provDao.find ( 
			adminUser.getEmail (), "accessControl.storeUser", Arrays.asList ( p ( "user", "test.new.user" ) )
		);

		out.println ( "------ MAPPING RECORDS: " + proves );
		assertEquals ( "Expected provenance records not saved (test.new.user)!", 1, proves.size () );
	}
	
	/**
	 * Test visibility commands. 
	 */
	@Test
	public void testVisibility ()
	{
		DbManagerFactory mgrFact = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();

		// The services we will play with
		Reader xmlIn = new InputStreamReader ( this.getClass ().getResourceAsStream ( "/data/foo_services.xml" ) );
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		smgr.storeServicesFromXML ( xmlIn );
		
		// Test mappings
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		mapMgr.storeMappings ( "test.testmain.service6:acc1", "test.testmain.service8:acc1" );
		
		AccessControlManager accMgr = mgrFact.newAccessControlManager ( editorUser.getEmail (), testSecret );
		accMgr.setEntitiesVisibility ( "true", "2014-12-31", "test.testmain.service6:acc1", "test.testmain.service8:acc1" );
	
		// Has the above been tracked?
		em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );
		List<ProvenanceRegisterEntry> proves = provDao.find ( 
			null, "%.setEntitiesVisibility", 
			Arrays.asList ( p ( "publicFlag", "true" ), p ( "%Date", "2014%" ), p ( "entity", null, "%acc1" ) )
		);

		out.println ( "------ MAPPING RECORDS: " + proves );
		assertEquals ( "Expected provenance records not saved (test.new.user)!", 1, proves.size () );
	}
	
}
