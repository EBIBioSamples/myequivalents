package uk.ac.ebi.fg.myequivalents.provenance.cmdline;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;
import static uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter.STR2DATE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.cmdline.Main;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>22 Sep 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvLineCommandsTest
{
	public static final String testPass = "test.password";
	public static final String testSecret = "test.secret"; 
	
	public static final User editorUser = new User ( 
		"test.editor", "Test Editor", "User", testPass, "test editor notes", Role.EDITOR, testSecret 
	);

	public static final User adminUser = new User ( 
		"test.admin", "Test Admin", "User", testPass, "test admin notes", Role.ADMIN, testSecret 
	);

	private final Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	static {
		// Avoids that main() exits
		System.setProperty ( "uk.ac.ebi.fg.myequivalents.test_flag", "true" );
	}
	
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
	public void testProvenanceFind () throws InterruptedException, IOException
	{
		// Here we get the specific factory, since we want newProvRegistryManager() 
		ProvDbManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );

		ProvenanceRegisterEntry e = new ProvenanceRegisterEntry ( 
			"foo.user", "foo.op", p ( "foo.entity", Arrays.asList ( "acc1", "acc2", "acc3" ) )
		);
		ProvenanceRegisterEntry e1 = new ProvenanceRegisterEntry ( 
			"foo.user1", "foo.op1", p ( "foo.entity", Arrays.asList ( "acc1", "acc2", "acc3" ) )
		);
		
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		provDao.create ( e );
		provDao.create ( e1 );
		ts.commit ();
		
		// Before the invocation, capture the standard output
		PrintStream stdOut = System.out;
		ByteArrayOutputStream myOut = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( myOut ) );
		
		// Now invoke the command line
		//
		Main.main ( 
			"provenance", "find", "-u", adminUser.getEmail (), "-s", testSecret, 
			"--prov-user", "foo.user%", 
			"--prov-operation", "foo.op%",
			"--prov-from", STR2DATE.marshal ( new DateTime ().minusDays ( 3 ).toDate () ),
			"--prov-param", "foo.entity:acc%"
		);
		
		// Get the result and restore the stdout
		System.setOut ( stdOut );
		myOut.close ();
		String resultStr = myOut.toString ( "UTF-8" );
		
		log.info ( "---- COMMAND OUTPUT -----\n{}\n---- /END: COMMAND OUTPUT ----", resultStr );
		
		assertTrue ( "Wrong XML result!", resultStr.contains ( "<entry operation=\"foo.op\"" ) );
		assertTrue ( "Wrong XML result!", resultStr.contains ( "<parameter" ) );
		assertTrue ( "Wrong XML result!", resultStr.contains ( "value=\"acc2\"") );
		assertTrue ( "Wrong XML result!", resultStr.contains ( "value-type=\"foo.entity\"/>" ) );
		assertTrue ( "Wrong XML result!", resultStr.contains ( "timestamp=" ) );
		
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager (
			adminUser.getEmail (), testSecret
		);

		regMgr.purge ( new DateTime ().minusMinutes ( 1 ).toDate (), null );
		em.close (); // flushes data for certain DBs (eg, H2)

		provDao = new ProvenanceRegisterEntryDAO ( em = mgrFact.getEntityManagerFactory ().createEntityManager () );
		assertEquals ( "purgeAll() didn't work!", 1, provDao.find ( "foo.user%", null, null ).size () );
	}
	
	
	@Test
	public void testFindEntityProv () throws IOException
	{
		ProvDbManagerFactory mgrFact = (ProvDbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), testSecret );
		
		String sname = "prov.test.service1";
		Service serv = new Service ( sname );
		smgr.storeServices ( serv );

		EntityMappingManager mmgr = mgrFact.newEntityMappingManager ( editorUser.getEmail (), testSecret );
		mmgr.storeMappings ( sname + ":a", sname + ":b" );
		mmgr.storeMappingBundle ( sname + ":b", sname + ":c", sname + ":d" );
		
		// Before the invocation, capture the standard output
		PrintStream stdOut = System.out;
		ByteArrayOutputStream myOut = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( myOut ) );
		
		// Now invoke the find-entity command
		Main.main ( 
			"provenance", "find-entity", "-u", editorUser.getEmail (), "-s", testSecret,
			sname + ":b", editorUser.getEmail ()
		);

		// Get the result and restore the stdout
		//
		System.setOut ( stdOut );
		myOut.close ();
		String provsXml = myOut.toString ( "UTF-8" );
		
		log.info ( "---- COMMAND OUTPUT -----\n{}\n---- /END: COMMAND OUTPUT ----", provsXml );

		
		log.info ( "Provenance-XML:\n{}", provsXml );

		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<provenance>" ) ); 
		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<entry operation=\"mapping.storeMappingBundle\"" ) ); 
		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<parameter extra-value=\"c\"" ) ); 
	}
	
	@Test
	public void testFindMappingProv () throws IOException
	{
		ProvDbManagerFactory mgrFact = (ProvDbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
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
				
		
		// Before the invocation, capture the standard output
		PrintStream stdOut = System.out;
		ByteArrayOutputStream myOut = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( myOut ) );
		
		// Now invoke the find-entity command
		Main.main ( 
			"provenance", "find-mapping", "-u", editorUser.getEmail (), "-s", testSecret,
			sname + ":a", sname + ":d", editorUser.getEmail ()
		);

		// Get the result and restore the stdout
		//
		System.setOut ( stdOut );
		myOut.close ();
		String provsXml = myOut.toString ( "UTF-8" );
		
		log.info ( "---- COMMAND OUTPUT -----\n{}\n---- /END: COMMAND OUTPUT ----", provsXml );

		
		log.info ( "Provenance-XML:\n{}", provsXml );

		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<provenance-entry-lists>" ) ); 
		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<entries>" ) ); 
		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<entry operation=\"mapping.storeMappingBundle\"" ) ); 
		assertTrue ( "Wrong provenance XML!", provsXml.contains ( "<parameter extra-value=\"c\"" ) );
		
		// Clean-up
		mmgr.deleteMappings ( sname + ":a" );
		smgr.deleteServices ( sname );
	}
	
	
	@Test
	public void testPurge () throws IOException
	{
		// Here we get the specific factory, since we want newProvRegistryManager() 
		ProvDbManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );

		ProvenanceRegisterEntry e = new ProvenanceRegisterEntry ( 
			"foo.user", "foo.op", p ( "foo.entity", Arrays.asList ( "acc1", "acc2", "acc3" ) )
		);
		ProvenanceRegisterEntry e1 = new ProvenanceRegisterEntry ( 
			"foo.user1", "foo.op1", p ( "foo.entity", Arrays.asList ( "acc1", "acc2", "acc3" ) )
		);
		
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		provDao.create ( e );
		provDao.create ( e1 );
		ts.commit ();		
		
		// Before the invocation, capture the standard output
		PrintStream stdOut = System.out;
		ByteArrayOutputStream myOut = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( myOut ) );
		
		// Now invoke the command line
		//
		Main.main ( 
			"provenance", "purge", "-u", adminUser.getEmail (), "-s", testSecret, 
			"--prov-from", STR2DATE.marshal ( new DateTime ().minusMinutes ( 1 ).toDate () )
		);
		
		// Get the result and restore the stdout
		System.setOut ( stdOut );
		myOut.close ();
		String resultStr = myOut.toString ( "UTF-8" );
		
		log.info ( "---- COMMAND OUTPUT -----\n{}\n---- /END: COMMAND OUTPUT ----", resultStr );
		
		RegEx re = new RegEx ( ".+^Done, ([0-9]+) provenance entries removed\\.$.+", Pattern.MULTILINE | Pattern.DOTALL );
		String resultChunks[] = re.groups ( resultStr );
		Assert.assertNotNull ( "Wrong purge output!", resultChunks );
		Assert.assertEquals ( "Wrong purge output!", 2, resultChunks.length );
		
		int result = Integer.valueOf ( resultChunks [ 1 ] );
		assertTrue ( "Wrong purge output!", result > 0 ); 
	}
}
