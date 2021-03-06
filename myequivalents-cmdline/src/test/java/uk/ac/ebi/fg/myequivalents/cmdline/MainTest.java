package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * 
 * Tests for the Command Line Interface.
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MainTest
{	
	private ServiceManager serviceMgr;
	private String testServiceXml =
		"<service-items>\n" +
		"  <services>\n" +
    "    <service uri-pattern='http://somewhere.in.the.net/testmain/service6/someType1/$id'\n" + 
    "           entity-type='testmain.someType1' title='A Test Service 6' name='test.testmain.service6'>\n" +
    "      <description>The Description of a Test Service 6</description>\n" + 
    "    </service>\n" + 
    "    <service entity-type='testmain.someType7' title='A Test Service 7' name='test.testmain.service7'" +
    "           repository-name = 'test.testmain.repo1'" +
    "           service-collection-name = 'test.testmain.serviceColl1'>\n" +
    "      <description>The Description of a Test Service 7</description>\n" +
    "    </service>\n" +
    "    <service\n" +
    "             entity-type='testmain.someType2' title='A Test Service 8' name='test.testmain.service8'" +
    "             repository-name = 'test.testmain.addedRepo1'>\n" + 
    "      <description>The Description of a Test Service 8</description>\n" + 
    "    </service>\n" +
    "  </services>\n" +
    "  <repositories>" +
    "  		<repository name = 'test.testmain.addedRepo1'>\n" +
    "       <description>A test Added Repo 1</description>\n" +
    "     </repository>\n" +
    "  </repositories>\n" +
    "  <service-collections>" +
    "  		<service-collection name = 'test.testmain.added-sc-1' title = 'Added Test SC 1'>\n" +
    "       <description>A test Added SC 1</description>\n" +
    "     </service-collection>\n" +
    "  </service-collections>\n" +
    "</service-items>";

	private String editorPass = "test.password";
	private String editorSecret = User.generateSecret ();
	private String adminPass = "test.admin.pwd";
	private String adminSecret = User.generateSecret ();
	
	private User editorUser = new User ( 
		"test.editor", "Test Editor", "User", editorPass, "test editor notes", Role.EDITOR, editorSecret );
	private User adminUser = new User ( 
			"test.admin", "Test Admin", "User", adminPass, "test admin notes", Role.ADMIN, adminSecret );
	
	static {
		System.setProperty ( "uk.ac.ebi.fg.myequivalents.test_flag", "true" );
	}

	@Before
	public void init ()
	{
		// An editor is needed for writing operations.
		EntityManager em = ( (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory () )
			.getEntityManagerFactory ().createEntityManager ();
		UserDao userDao = new UserDao ( em );
		
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.storeUnauthorized ( editorUser );
		userDao.storeUnauthorized ( adminUser );
		ts.commit ();

		serviceMgr = Resources.getInstance ().getMyEqManagerFactory ().newServiceManager ( editorUser.getEmail (), this.editorSecret );
		
		ServiceCollection sc1 = new ServiceCollection ( 
			"test.testmain.serviceColl1", null, "Test Service Collection 1", "The Description of the SC 1" 
		);
		serviceMgr.storeServiceCollections ( sc1 );
		
		Repository repo1 = new Repository ( "test.testmain.repo1", "Test Repo 1", "The Description of Repo1" );
		serviceMgr.storeRepositories ( repo1 );
	}

	
	
	/**
	 * Tests the storage of some services (and related stuff) via command line and via XML. Here you find an example of the
	 * sort of XML you need for this API call. 
	 */
	@Test
	public void testServiceCommands () throws UnsupportedEncodingException
	{
		System.setIn ( new ByteArrayInputStream ( testServiceXml.getBytes ( "UTF-8" ) ));
		Main.main ( "service", "store", "--user", editorUser.getEmail (), "--secret", editorSecret );
		
		ServiceSearchResult result = serviceMgr.getServices ( 
			"test.testmain.service6", "test.testmain.service7", "test.testmain.service8" );
		
		out.format ( "Storage Result:\n%s\n", result );
		
		assertEquals ( "Wrong no of services stored!", 3, result.getServices ().size () );
		assertEquals ( "Wrong no of SC stored!", 1, result.getServiceCollections ().size () );
		assertEquals ( "'service store' returned a wrong exit code!", 0, Main.exitCode );

		// Before the invocation, capture the standard output
		PrintStream stdOut = System.out;
		ByteArrayOutputStream getOut = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( getOut ) );
		
		// And get rid of stuff like log messages. Unfortunately this won't be enough if hibernate.show_sql=true, 
		// but at least we move out of our way as much as possible
		PrintStream stdErr = System.err;
		PrintStream devNull = new PrintStream ( new NullOutputStream () );
		System.setErr ( devNull ); 
		
		Main.main ( 
			"service", "get", "-u", editorUser.getEmail (), "-s", editorSecret, 
			"--format", "xml", "test.testmain.service7", "test.testmain.service8" 
		);
		String getOutStr = getOut.toString ( "UTF-8" );
		System.setOut ( stdOut );
		System.setErr ( stdErr );
		
		out.println ( "\n\n ====================== 'service get' says:\n" + getOutStr + "============================" );
		
		assertNotNull ( "'service get' didn't work!", getOutStr );
		assertTrue ( "Wrong result from 'service get'!", getOutStr.contains ( "test.testmain.service8" ) );
		assertTrue ( "Wrong result from 'service get' (xml)!", 
			getOutStr.contains ( "<service-items>" ) );
		assertTrue ( "Wrong result from 'service get' (xml, addedRepo1)!", 
			getOutStr.contains ( "<repository name=\"test.testmain.addedRepo1\"" ) );
		
		Main.main ( 
			"service", "delete", "-u", editorUser.getEmail (), "-s", editorSecret, "test.testmain.service8" 
		);
		assertTrue ( "Service not deleted!", this.serviceMgr.getServices ( "test.testmain.service8" ).getServices ().isEmpty () );
		assertEquals ( "'service delete' returned a wrong exit code!", 0, Main.exitCode );

	}	
	
	
	/**
	 * Tests service-collection commands
	 */
	@Test
	public void testServiceCollectionCommands () throws UnsupportedEncodingException, JAXBException
	{
		serviceMgr.storeServicesFromXML ( new StringReader ( testServiceXml ) );

		// Before the invocation, capture the standard output
		PrintStream stdOut = System.out;
		ByteArrayOutputStream getOut = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( getOut ) );
		
		// And get rid of stuff like log messages. Unfortunately this won't be enough if hibernate.show_sql=true, 
		// but at least we move out of our way as much as possible
		PrintStream stdErr = System.err;
		PrintStream devNull = new PrintStream ( new NullOutputStream () );
		System.setErr ( devNull ); 
		
		Main.main ( "service-collection", "get", "--format", "xml", "test.testmain.added-sc-1", "test.testmain.serviceColl1" );
		String getOutStr = getOut.toString ( "UTF-8" );
		System.setOut ( stdOut );
		System.setErr ( stdErr );
		
		out.println ( "\n\n ====================== 'service-collection get' says:\n" + getOutStr + "============================" );
		
		assertNotNull ( "'service-collection get' didn't work!", getOutStr );
		assertTrue ( "Wrong result from 'service-collection get' (added-sc-1)!", getOutStr.contains ( "test.testmain.added-sc-1" ) );
		assertTrue ( "Wrong result from 'service-collection get' (serviceColl1)!", getOutStr.contains ( "test.testmain.serviceColl1" ) );
		assertTrue ( "Wrong result from 'service-collection get' (xml)!",	getOutStr.contains ( "<service-items>" ) );
		
		Main.main ( "service-collection", "delete", "-u", editorUser.getEmail (), "-s", editorSecret, "test.testmain.added-sc-1" );
		assertTrue ( "Service-collection not deleted!", this.serviceMgr.getServices ( "test.testmain.added-sc-1" ).getServices ().isEmpty () );
		assertEquals ( "'service-collection delete' returned a wrong exit code!", 0, Main.exitCode );

	}	
	
	
	/**
	 * Tests repository commands
	 */
	@Test
	public void testRepositoryCommands () throws UnsupportedEncodingException, JAXBException
	{
		serviceMgr.storeServicesFromXML ( new StringReader ( testServiceXml ) );

		// Before the invocation, capture the standard output
		PrintStream stdOut = System.out;
		ByteArrayOutputStream getOut = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( getOut ) );
		
		// And get rid of stuff like log messages. Unfortunately this won't be enough if hibernate.show_sql=true, 
		// but at least we move out of our way as much as possible
		PrintStream stdErr = System.err;
		PrintStream devNull = new PrintStream ( new NullOutputStream () );
		System.setErr ( devNull ); 
		
		Main.main ( "repository", "get", "--format", "xml", "test.testmain.addedRepo1", "test.testmain.repo1" );
		String getOutStr = getOut.toString ( "UTF-8" );
		System.setOut ( stdOut );
		System.setErr ( stdErr );
		
		out.println ( "\n\n ====================== 'repository get' says:\n" + getOutStr + "============================" );
		
		assertNotNull ( "'service-collection get' didn't work!", getOutStr );
		assertTrue ( "Wrong result from 'repository get' (addedRepo1)!", getOutStr.contains ( "test.testmain.addedRepo1" ) );
		assertTrue ( "Wrong result from 'repository get' (repo1)!", getOutStr.contains ( "test.testmain.repo1" ) );
		assertTrue ( "Wrong result from 'repository get' (xml)!", getOutStr.contains ( "<service-items>" ) );
		
		Main.main ( "service-collection", "delete", "-u", editorUser.getEmail (), "-s", editorSecret, "test.testmain.added-sc-1" );
		assertTrue ( "Service-collection not deleted!", this.serviceMgr.getServices ( "test.testmain.added-sc-1" ).getServices ().isEmpty () );
		assertEquals ( "'service-collection delete' returned a wrong exit code!", 0, Main.exitCode );
	}	

	
	/**
	 * Test commands about entity mappings
	 */
	@Test
	public void testMappingCommands () throws JAXBException, UnsupportedEncodingException
	{
		serviceMgr.storeServicesFromXML ( new StringReader ( testServiceXml ) );
		Main.main ( "mapping", "store", "-u", editorUser.getEmail (), "-s", editorSecret,
			"test.testmain.service6:acc1", "test.testmain.service8:acc2", 
			"test.testmain.service6:acc3", "test.testmain.service6:acc4" 
		);
		Main.main ( "mapping", "store-bundle", "-u", editorUser.getEmail (), "-s", editorSecret,
			"test.testmain.service7:acc1", "test.testmain.service6:acc4", "test.testmain.service6:acc1"
		);
		
		// Before the invocation, capture the standard output
		PrintStream stdOut = System.out;
		ByteArrayOutputStream getOut = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( getOut ) );
		
		// And get rid of stuff like log messages. Unfortunately this won't be enough if hibernate.show_sql=true, 
		// but at least we move out of our way as much as possible
		PrintStream stdErr = System.err;
		PrintStream devNull = new PrintStream ( new NullOutputStream () );
		System.setErr ( devNull ); 
		
		Main.main ( "mapping", "get", "--raw", "test.testmain.service6:acc1" );
		String getOutStr = getOut.toString ( "UTF-8" );
		System.setOut ( stdOut );
		System.setErr ( stdErr );
		
		out.println ( "\n\n ====================== 'mapping get' says:\n" + getOutStr + "============================" );
		
		assertNotNull ( "'mapping get' didn't work!", getOutStr );
		assertTrue ( "Wrong result from 'mapping get' (service8/acc2)!", 
			getOutStr.contains ( "test.testmain.service8" ) && getOutStr.contains ( "acc2" )
		);
		assertTrue ( "Wrong result from 'repository get' (service6/acc3)!", 
			getOutStr.contains ( "test.testmain.service6" ) && getOutStr.contains ( "acc3" ) 
		);
		assertTrue ( "Wrong result from 'repository get' (service7/acc1)!", 
			getOutStr.contains ( "test.testmain.service7" ) && getOutStr.contains ( "acc1" ) 
		);
		
		
		// Deletion
		//
		Main.main ( "mapping", "delete-entity",  "-u", editorUser.getEmail (), "-s", editorSecret, "test.testmain.service6:acc3" );
		EntityMappingManager emMgr = Resources.getInstance ().getMyEqManagerFactory ().newEntityMappingManager ( 
			this.editorUser.getEmail (), this.editorSecret 
		);
		assertTrue ( "'mapping delete-entity' didn't work!", 
			emMgr.getMappings ( true, "test.testmain.service6:acc3" ).getBundles ().isEmpty () );
		
		Main.main ( "mapping", "delete", "-u", editorUser.getEmail (), "-s", editorSecret, "test.testmain.service7:acc1" );
		assertTrue ( "'mapping delete' didn't work!", 
			emMgr.getMappings ( true, "test.testmain.service6:acc1" ).getBundles ().isEmpty () );
	}
	
	
	@Test
	public void testMappingWithUris () throws JAXBException, UnsupportedEncodingException
	{
		String uri = "http://somewhere.in.the.net/testmain/service6/someType1/acc1";
		serviceMgr.storeServicesFromXML ( new StringReader ( testServiceXml ) );
		Main.main ( "mapping", "store-bundle", "-u", editorUser.getEmail (), "-s", editorSecret,
			"<" + uri + ">", 
			"test.testmain.service6:acc2",
			"test.testmain.service6:acc3"
		);
		
		// Before the invocation, capture the standard output
		PrintStream stdOut = System.out;
		ByteArrayOutputStream getOut = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( getOut ) );
		
		// And get rid of stuff like log messages. Unfortunately this won't be enough if hibernate.show_sql=true, 
		// but at least we move out of our way as much as possible
		PrintStream stdErr = System.err;
		PrintStream devNull = new PrintStream ( new NullOutputStream () );
		System.setErr ( devNull ); 
		
		Main.main ( "mapping", "get", "--raw", "test.testmain.service6:acc2" );
		String getOutStr = getOut.toString ( "UTF-8" );
		System.setOut ( stdOut );
		System.setErr ( stdErr );
		
		out.println ( "\n\n ====================== 'mapping get' says:\n" + getOutStr + "============================" );
		
		assertNotNull ( "'mapping get' didn't work!", getOutStr );
		assertTrue ( "Wrong result from 'mapping get' (service6/acc2)!", 
			getOutStr.contains ( "test.testmain.service6" ) && getOutStr.contains ( "acc2" )
		);
		

		// Again, with URI fetching
		//

		Main.main ( "mapping", "get", "--raw", "<" + uri + ">" );
		getOutStr = getOut.toString ( "UTF-8" );
		System.setOut ( stdOut );
		System.setErr ( stdErr );
		
		out.println ( "\n\n ====================== 'mapping get <uri>' says:\n" + getOutStr + "============================" );
		
		assertNotNull ( "'mapping get' didn't work!", getOutStr );
		
		assertTrue ( "Wrong result from 'repository get' (service6/acc3)!", 
			getOutStr.contains ( "test.testmain.service6" ) && getOutStr.contains ( "acc1" ) 
		);
		assertTrue ( "Wrong result from 'repository get' (service7/acc1)!", 
			getOutStr.contains ( "http://somewhere.in.the.net/testmain/service6/someType1/acc3" ) 
		);
		
		
		// Deletion
		//
		Main.main ( "mapping", "delete-entity",  "-u", editorUser.getEmail (), "-s", editorSecret, "test.testmain.service6:acc3" );
		EntityMappingManager emMgr = Resources.getInstance ().getMyEqManagerFactory ().newEntityMappingManager ( 
			this.editorUser.getEmail (), this.editorSecret 
		);
		assertTrue ( "'mapping delete-entity' didn't work!", 
			emMgr.getMappings ( true, "test.testmain.service6:acc3" ).getBundles ().isEmpty () );
	}
	
	
	/** Tests 'user get' command. */
	@Test
	public void testUserGet () throws Exception
	{
		// Before the invocation, capture the standard output
		PrintStream stdOut = System.out;
		ByteArrayOutputStream getOut = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( getOut ) );
		
		// And get rid of stuff like log messages. Unfortunately this won't be enough if hibernate.show_sql=true, 
		// but at least we move out of our way as much as possible
		PrintStream stdErr = System.err;
		PrintStream devNull = new PrintStream ( new NullOutputStream () );
		System.setErr ( devNull ); 
		
		Main.main ( "user", "get", editorUser.getEmail (), "-u" + editorUser.getEmail (), "-s", editorSecret );
		String getOutStr = getOut.toString ( "UTF-8" );
		System.setOut ( stdOut );
		System.setErr ( stdErr );
		
		out.println ( "\n\n ====================== 'user get' says:\n" + getOutStr + "============================" );
		// TODO: checks
	}
	
	
	/**
	 * Tests user-related line commands
	 */
	@Test
	public void testUserCommands () throws Exception
	{
		String testEmail = "cmdline.test.user";
		String uxml = String.format ( 
			"<user email = '%s' name = 'Test User from' surname = 'Command Line Tests' role = 'VIEWER' " +
			"      secret = 'test.secret' password = 'test.password'>" +
			"  <notes>Test User from Command Line Tests, Test Note</notes>" + 
			"</user>",
			testEmail
		);
		System.setIn ( new ByteArrayInputStream ( uxml.getBytes ( "UTF-8" ) ));
		Main.main ( "user", "store", "--user", adminUser.getEmail (), "--password", adminPass );

		EntityManager em = ( (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory () )
			.getEntityManagerFactory ().createEntityManager ();
		UserDao userDao = new UserDao ( em );
		User u = userDao.findByEmailUnauthorized ( testEmail );
		assertNotNull ( "User Not saved!", u );
		assertEquals ( "user.getEmail() doesn't correspond to the stored user!", testEmail, u.getEmail () );

		Main.main ( 
			"user", "set", "role", testEmail, User.Role.EDITOR.toString (), 
			"--user", adminUser.getEmail (), "--password", adminPass 
		);
		
		em = ( (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory () )
			.getEntityManagerFactory ().createEntityManager ();
		userDao = new UserDao ( em );
		u = userDao.findByEmailUnauthorized ( testEmail );
		out.println ( "Modified User after 'set role':\n" + u );
		assertTrue ( "'user set role' didn't work!", u.hasPowerOf ( User.Role.EDITOR ) );

		
		Main.main (	"user", "delete", testEmail, "--user", adminUser.getEmail (), "--password", adminPass	);

		em = ( (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory () )
			.getEntityManagerFactory ().createEntityManager ();
		userDao = new UserDao ( em );
		assertNull ( "'user delete' didn't work!", userDao.findByEmailUnauthorized ( testEmail ) );
	}
	
	
	/**
	 * Checks the output of --help and alike
	 */
	@Test
	public void testHelp ()
	{
		out.println ( "\n\n ---- Testing --help" );
		Main.main ( "--help" );
		assertEquals ( "--help returned a wrong exit code!", 1, Main.exitCode );
		// TODO: link the output to a stream and verify with some reg-ex
		
		out.println ( "\n\n ---- Testing wrong command" );
		Main.main ( "foo", "command" );
		assertEquals ( "syntax-error test returned a wrong exit code!", 1, Main.exitCode );
		// TODO: link the output to a stream and verify with some reg-ex

		out.println ( "\n\n ---- Testing wrong option" );
		Main.main ( "service", "store", "--foo-option" );
		assertEquals ( "syntax-error test returned a wrong exit code!", 1, Main.exitCode );
		// TODO: link the output to a stream and verify with some reg-ex
	}
	
}
