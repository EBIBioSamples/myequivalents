package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.managers.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.ServiceSearchResult;
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
	
	static {
		System.setProperty ( "uk.ac.ebi.fg.myequivalents.test_flag", "true" );
	}

	@Before
	public void init ()
	{
		serviceMgr = new ServiceManager ();
		
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
		String xml =
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
		
		System.setIn ( new ByteArrayInputStream ( xml.getBytes ( "UTF-8" ) ));
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
