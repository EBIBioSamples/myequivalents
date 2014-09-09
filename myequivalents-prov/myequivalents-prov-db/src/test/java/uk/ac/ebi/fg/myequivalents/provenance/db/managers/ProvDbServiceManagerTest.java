package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * Tests {@link ProvDbServiceManager}.
 *
 * <dl><dt>date</dt><dd>10 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbServiceManagerTest
{
	static final String testPass = "test.password";
	static final String testSecret = "test.secret"; 
	
	static final User editorUser = new User ( 
		"test.editor", "Test Editor", "User", testPass, "test editor notes", Role.EDITOR, testSecret 
	);

	static final User adminUser = new User ( 
		"test.admin", "Test Admin", "User", testPass, "test admin notes", Role.ADMIN, testSecret 
	);

	private final Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@BeforeClass
	public static void init ()
	{
		editorUser.setApiPassword ( testSecret );
		editorUser.setPassword ( testPass );
		
		adminUser.setApiPassword ( testSecret );
		adminUser.setPassword ( testPass );
		
		DbManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		
		UserDao userDao = new UserDao ( em );

		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.storeUnauthorized ( editorUser );
		userDao.storeUnauthorized ( adminUser );
		ts.commit ();
	}
	
	@AfterClass
	public static void cleanUp ()
	{
		DbManagerFactory mgrFact = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		
		UserDao userDao = new UserDao ( em );

		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.deleteUnauthorized ( editorUser.getEmail () );
		userDao.deleteUnauthorized ( adminUser.getEmail () );
		ts.commit ();
		
		em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		ts = em.getTransaction ();
		ts.begin ();
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );
		provDao.purgeAll ( new DateTime ().minusMinutes ( 1 ).toDate (), null );
		//em.createNativeQuery ( "DELETE FROM provenance_register_parameter" ).executeUpdate ();
		//em.createQuery ( "DELETE FROM " + ProvenanceRegisterEntry.class.getName () ).executeUpdate ();
		ts.commit ();
	}
	
	
	@Test
	public void testCreation () throws Exception
	{
		DbManagerFactory mgrFact = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		
		Reader xmlIn = new InputStreamReader ( this.getClass ().getResourceAsStream ( "/data/foo_services.xml" ) );
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		smgr.storeServicesFromXML ( xmlIn );
		
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( mgrFact.getEntityManagerFactory ().createEntityManager () );
		List<ProvenanceRegisterEntry> proves = provDao.find ( null, "%storeServices%", Arrays.asList ( p ( "service", "%.service7" ) ) );
		assertEquals ( "Expected provenance records not saved (service7)!", 1, proves.size () );

		proves = provDao.find ( null, "%storeServices%", Arrays.asList ( p ( "repository", "test.testmain.addedRepo1" ) ) );
		assertEquals ( "Expected provenance records not saved (addedRepo1)!", 1, proves.size () );
	
		// This is how you can use JodaTime to set dates like '10 days ago' and pass them to search methods. 
		proves = provDao.find ( null, null, new DateTime ().minusDays ( 10 ), new DateTime (),
			Arrays.asList ( p ( "serviceCollection", (String) null ) ) 
		);
		assertEquals ( "Expected provenance records not saved (servCollections)!", 1, proves.size () );
		
		ProvenanceRegisterEntry prove = proves.get ( 0 );
		log.info ( "---- SERVCOLL:\n{}", prove );

		assertEquals ( "Fetched provenance record wrong (parameters)", 7, prove.getParameters ().size () );
		assertEquals ( "Fetched provenance record wrong (user)", editorUser.getEmail (), prove.getUserEmail () );
	}
	

}
