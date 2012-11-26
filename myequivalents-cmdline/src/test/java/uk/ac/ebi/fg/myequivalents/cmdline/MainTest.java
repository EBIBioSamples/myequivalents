package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

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
    "    <service uri-pattern='http://somewhere.in.the.net/testmain/service6/someType1/${accession}'\n" + 
		"           uri-prefix='http://somewhere.in.the.net/testmain/service6/'\n" + 
    "           entity-type='testmain.someType1' title='A Test Service 6' name='test.testmain.service6'>\n" +
    "      <description>The Description of a Test Service 6</description>\n" + 
    "    </service>\n" + 
    "    <service entity-type='testmain.someType7' title='A Test Service 7' name='test.testmain.service7'" +
    "           repository-name = 'test.testmain.repo1'" +
    "           service-collection-name = 'test.testmain.serviceColl1'>\n" +
    "      <description>The Description of a Test Service 7</description>\n" +
    "    </service>\n" +
    "    <service uri-prefix='http://somewhere-else.in.the.net/testmain/service8/'\n" +
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

	
	static {
		System.setProperty ( "uk.ac.ebi.fg.myequivalents.test_flag", "true" );
	}

	@Before
	public void init ()
	{
		serviceMgr = new DbManagerFactory ().newServiceManager ();
		
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
		Main.main ( "service", "store" );
		
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
		
		Main.main ( "service", "get", "--format", "xml", "test.testmain.service7", "test.testmain.service8" );
		String getOutStr = getOut.toString ( "UTF-8" );
		System.setOut ( stdOut );
		System.setErr ( stdErr );
		
		out.println ( "\n\n ====================== 'service get' says:\n" + getOutStr + "============================" );
		
		assertNotNull ( "'service get' didn't work!", getOutStr );
		assertTrue ( "Wrong result from 'service get'!", getOutStr.contains ( "test.testmain.service8" ) );
		assertTrue ( "Wrong result from 'service get' (xml)!", 
			getOutStr.contains ( "<service-items>" ) );
		assertTrue ( "Wrong result from 'service get' (xml, addedRepo1)!", 
			getOutStr.contains ( "<repository name=\"test.testmain.addedRepo1\">" ) );
		
		Main.main ( "service", "delete", "test.testmain.service8" );
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
		
		Main.main ( "service-collection", "delete", "test.testmain.added-sc-1" );
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
		
		Main.main ( "service-collection", "delete", "test.testmain.added-sc-1" );
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
		Main.main ( "mapping", "store", 
			"test.testmain.service6:acc1", "test.testmain.service8:acc2", 
			"test.testmain.service6:acc3", "test.testmain.service6:acc4" 
		);
		Main.main ( "mapping", "store-bundle",
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
		Main.main ( "mapping", "delete-entity", "test.testmain.service6:acc3" );
		EntityMappingManager emMgr = new DbManagerFactory ().newEntityMappingManager ();
		assertTrue ( "'mapping delete-entity' didn't work!", 
			emMgr.getMappings ( true, "test.testmain.service6:acc3" ).getBundles ().isEmpty () );
		
		Main.main ( "mapping", "delete", "test.testmain.service7:acc1" );
		assertTrue ( "'mapping delete' didn't work!", 
				emMgr.getMappings ( true, "test.testmain.service6:acc1" ).getBundles ().isEmpty () );
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
