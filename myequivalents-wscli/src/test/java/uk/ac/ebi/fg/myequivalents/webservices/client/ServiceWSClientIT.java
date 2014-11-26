package uk.ac.ebi.fg.myequivalents.webservices.client;

import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.StringReader;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;

import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import static uk.ac.ebi.fg.myequivalents.webservices.client.AccessControlWSClientIT.CLI_SPRING_CONFIG_FILE_NAME;
import static uk.ac.ebi.fg.myequivalents.webservices.client.AccessControlWSClientIT.EDITOR_USER;
import static uk.ac.ebi.fg.myequivalents.webservices.client.AccessControlWSClientIT.EDITOR_SECRET;

/**
 * Tests for {@link ServiceWSClient}.
 *
 * <dl><dt>date</dt><dd>2 Nov 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceWSClientIT
{
	private ServiceManager serviceMgr;

	private Service service1, service2, service3, service4, service5;
	private ServiceCollection sc1;
	private Repository repo1;
	
	@Before
	public void init ()
	{
		// This is how you should obtain a manager from a factory. Well, almost: normally you'll invoke getMyEqManagerFactory()
		// without parameters and a default file name will be picked. This is instead an extended approach, needed to cope 
		// with client/server conflicting files in the Maven-built environment.
		//
		serviceMgr = Resources.getInstance ()
				.getMyEqManagerFactory ( CLI_SPRING_CONFIG_FILE_NAME ).newServiceManager ( EDITOR_USER.getEmail (), EDITOR_SECRET  );
		
		service1 = new Service ( "test.testweb.service1", "testweb.someType1", "A Test Service 1", "The Description of a Test Service 1" );
		service1.setUriPrefix ( "http://somewhere.in.the.net/testweb/service1/" );
		service1.setUriPattern ( "http://somewhere.in.the.net/testweb/service1/someType1/${accession}" );
				
		sc1 = new ServiceCollection ( 
			"test.testweb.serviceColl1", service1.getEntityType (), "Test Service Collection 1", "The Description of the SC 1" 
		);
		service1.setServiceCollection ( sc1 );
		
		repo1 = new Repository ( "test.testweb.repo1", "Test Repo 1", "The Description of Repo1" );
		service1.setRepository ( repo1 );

		service2 = new Service ( "test.testweb.service2", "testweb.someType1", "A Test Service 2", "The Description of a Test Service 2" );
		service1.setUriPrefix ( "http://somewhere.in.the.net/testweb/service2/" );
		// Should pop-up on the XML
		service2.setReleaseDate ( new GregorianCalendar ( 2010, GregorianCalendar.APRIL, 25, 18, 13 ).getTime () );


		service3 = new Service ( "test.testweb.service3", "testweb.someType2", "A Test Service 3", "The Description of a Test Service 3" );
		service3.setUriPrefix ( "http://somewhere-else.in.the.net/testweb/service3/" );

		service4 = new Service ( "test.testweb.service4", "testweb.someType2", "A Test Service 4", "The Description of a Test Service 4" );
		service4.setUriPrefix ( "http://somewhere-else.in.the.net/testweb/service4/" );

		service5 = new Service ( "test.testweb.service5", "testweb.someType2", "A Test Service 5", "The Description of a Test Service 5" );
		service5.setUriPrefix ( "http://somewhere-else.in.the.net/testweb/service5/" );
				
		serviceMgr.storeServices ( service1, service2, service3, service4, service5 );
	
		String servNames[] = new String[] { 
			service1.getName (), service2.getName (), service3.getName (), service4.getName (), service5.getName ()
		};

		assertEquals ( "Services not created!", 5, serviceMgr.getServices ( servNames ).getServices ().size () );
		assertEquals ( "Repository not created!", 1, serviceMgr.getRepositories ( repo1.getName () ).getRepositories ().size () );
		assertEquals ( "Service-Collection not created!", 1, serviceMgr.getServiceCollections ( sc1.getName () ).getServiceCollections ().size () );

		// TODO: more checks 
	}
	
	
	@After
	public void cleanUp () 
	{
		// That's another way to get managers, but should be avoided, prefer the factory.
		//serviceMgr = new ServiceWSClient ( WS_BASE_URL );
		//serviceMgr.setAuthenticationCredentials ( EDITOR_USER.getEmail (), EDITOR_SECRET );
		
		serviceMgr = Resources.getInstance ()
		.getMyEqManagerFactory ( CLI_SPRING_CONFIG_FILE_NAME ).newServiceManager ( EDITOR_USER.getEmail (), EDITOR_SECRET  );
		
		String servNames[] = new String[] { 
			service1.getName (), service2.getName (), service3.getName (), service4.getName (), service5.getName (),
			"test.testweb.service6", "test.testweb.service7", "test.testweb.service8"
		};
		
		serviceMgr.deleteServices ( servNames );
		serviceMgr.deleteServiceCollections ( sc1.getName () );
		serviceMgr.deleteRepositories ( repo1.getName () );
		

		assertTrue ( "Services not deleted!", serviceMgr.getServices ( servNames ).getServices ().isEmpty () );
		assertTrue ( "Repository not deleted!", serviceMgr.getRepositories ( repo1.getName () ).getRepositories ().isEmpty () );
		assertTrue ( "Service-Collection not deleted!", serviceMgr.getServiceCollections ( sc1.getName () ).getServiceCollections ().isEmpty () );
	}

	@Test
	public void testSearch ()
	{
		// Work as anonymous
		serviceMgr = Resources.getInstance ()
				.getMyEqManagerFactory ( CLI_SPRING_CONFIG_FILE_NAME ).newServiceManager ();
		
		ServiceSearchResult result = serviceMgr.getServices ( 
			service4.getName (), service2.getName (), service5.getName (), "test.servMgr.foo" 
		);
		out.println ( "Search Result:\n" + result.toString () );
		
		assertEquals ( "Search returns a wrong no. of results!", 3, result.getServices ().size () );
		// TODO: More checks
		
		String xml = serviceMgr.getServicesAs ( "xml", service4.getName (), service2.getName () );
		out.println ( "Search Result:\n" + xml );
		
		// TODO: checks over the XML
	}

	
	@Test
	public void testAddFromXML () throws JAXBException
	{
		// TODO: Test Service Collection too
		
		String xml =
		"<service-items>\n" +
		"  <services>\n" +
    "    <service uri-pattern='http://somewhere.in.the.net/testweb/service6/someType1/${accession}'\n" + 
		"           uri-prefix='http://somewhere.in.the.net/testweb/service6/'\n" + 
    "           entity-type='testweb.someType1' title='A Test Service 6' name='test.testweb.service6'>\n" +
    "      <description>The Description of a Test Service 6</description>\n" + 
    "    </service>\n" + 
    "    <service entity-type='testweb.someType7' title='A Test Service 7' name='test.testweb.service7'" +
    "           repository-name = 'test.testweb.repo1'" +
    "           service-collection-name = 'test.testweb.serviceColl1'>\n" +
    "      <description>The Description of a Test Service 7</description>\n" +
    "    </service>\n" +
    "    <service uri-prefix='http://somewhere-else.in.the.net/testweb/service8/'\n" +
    "             entity-type='testweb.someType2' title='A Test Service 8' name='test.testweb.service8'>\n" + 
    "      <description>The Description of a Test Service 8</description>\n" + 
    "    </service>\n" +
    "  </services>\n" +
    "</service-items>";

		serviceMgr.setAuthenticationCredentials ( EDITOR_USER.getEmail (), EDITOR_SECRET );
		serviceMgr.storeServicesFromXML ( new StringReader ( xml ) );

		String servNames[] = new String[] { 
				"test.testweb.service6", "test.testweb.service7", "test.testweb.service8" };

		ServiceSearchResult result = serviceMgr.getServices ( servNames );
		
		out.format ( "Storage Result:\n%s\n", result );
		assertEquals ( "Wrong no of services stored", 3, result.getServices ().size () );
		
		xml = serviceMgr.getServicesAs ( "xml", servNames );
		out.println ( "Search Result (XML):\n" + xml );

		// TODO: checks on the XML
		
	}
	
	
	@Test
	public void testComplexAddFromXML () throws JAXBException
	{
		// TODO: Test Service Collection too
		
		String xml =
		"<service-items>\n" +
		"  <services>\n" +
    "    <service uri-pattern='http://somewhere.in.the.net/testweb/service6/someType1/${accession}'\n" + 
		"           uri-prefix='http://somewhere.in.the.net/testweb/service6/'\n" + 
    "           entity-type='testweb.someType1' title='A Test Service 6' name='test.testweb.service6'\n" +
    "						release-date = '20130110' public-flag = 'null'>\n" +
    "      <description>The Description of a Test Service 6</description>\n" + 
    "    </service>\n" + 
    "    <service entity-type='testweb.someType7' title='A Test Service 7' name='test.testweb.service7'" +
    "           repository-name = 'test.testweb.repo1'" +
    "           service-collection-name = 'test.testweb.serviceColl1'>\n" +
    "      <description>The Description of a Test Service 7</description>\n" +
    "    </service>\n" +
    "    <service uri-prefix='http://somewhere-else.in.the.net/testweb/service8/'\n" +
    "             entity-type='testweb.someType2' title='A Test Service 8' name='test.testweb.service8'" +
    "             repository-name = 'test.testweb.addedRepo1'>\n" + 
    "      <description>The Description of a Test Service 8</description>\n" + 
    "    </service>\n" +
    "  </services>\n" +
    "  <repositories>" +
    "  		<repository name = 'test.testweb.addedRepo1' public-flag = 'false'>\n" +
    "       <description>A test Added Repo 1</description>\n" +
    "     </repository>\n" +
    "  </repositories>\n" +
    "  <service-collections>" +
    "  		<service-collection name = 'test.testweb.added-sc-1' title = 'Added Test SC 1'>\n" +
    "       <description>A test Added SC 1</description>\n" +
    "     </service-collection>\n" +
    "  </service-collections>\n" +
    "</service-items>";

		serviceMgr.storeServicesFromXML ( new StringReader ( xml ) );
		serviceMgr.setAuthenticationCredentials ( EDITOR_USER.getEmail (), EDITOR_SECRET );
		
		ServiceSearchResult result = serviceMgr.getServices ( 
			"test.testweb.service6", "test.testweb.service7", "test.testweb.service8" );
		
		out.format ( "Storage Result:\n%s\n", result );
		assertEquals ( "Wrong no of services stored!", 3, result.getServices ().size () );
		
		xml = serviceMgr.getServicesAs ( "xml", 
			"test.testweb.service6", "test.testweb.service7", "test.testweb.service8" );
		out.println ( "Search Result (XML):\n" + xml );
		// TODO: checks on the XML
		
		result = serviceMgr.getServiceCollections ( "test.testweb.added-sc-1" );
		out.format ( "Storage Result:\n%s\n", result );
		assertEquals ( "Wrong no of SC stored!", 1, result.getServiceCollections ().size () );

		xml = serviceMgr.getServiceCollectionsAs ( "xml", "test.testweb.added-sc-1" );
		out.println ( "Search Result (XML):\n" + xml );
		// TODO: checks on the XML
		
		Service srv6 = serviceMgr.getServices ( "test.testweb.service6" ).getServices ().iterator ().next ();
		assertTrue ( "release date defined in the XML not stored!", 
			new DateMidnight ( 2013, 01, 10 ).isEqual ( srv6.getReleaseDate ().getTime () )
		);
		assertNull ( "public flag defined in the XML not stored!", srv6.getPublicFlag () );
		
		Repository repo1 = serviceMgr.getRepositories ( "test.testweb.addedRepo1" ).getRepositories ().iterator ().next ();
		assertFalse ( "public flag defined in the XML not stored (repo1)!", repo1.getPublicFlag () );
		
		// Local clean-up
		
		assertEquals ( "Test Repo not removed!", 3, serviceMgr.deleteServices ( 
			"test.testweb.service6", "test.testweb.service7", "test.testweb.service8" ));
		assertEquals ( "Test Repo not removed!", 1, serviceMgr.deleteRepositories ( repo1.getName () ) );
		assertEquals ( "Test service collection not removed!", 1, serviceMgr.deleteServiceCollections ( "test.testweb.added-sc-1" ) );
	}	
	
}
