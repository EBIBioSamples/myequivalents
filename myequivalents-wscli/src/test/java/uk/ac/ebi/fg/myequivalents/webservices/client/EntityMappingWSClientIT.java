package uk.ac.ebi.fg.myequivalents.webservices.client;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver.buildUriFromAcc;
import static uk.ac.ebi.fg.myequivalents.webservices.client.AccessControlWSClientIT.CLI_SPRING_CONFIG_FILE_NAME;

import java.util.Collection;

import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;

/**
 * Tests the Web service client for the mappings. This relies on some data that are initialised by the myequivalents-web
 * package during integration test phase (which, in turn, is bring up by the Jetty plug-in in this package).
 *  
 * The IT postfix in the name is required by the Maven Failsafe plug-in.  
 * 
 * <dl><dt>date</dt><dd>Oct 3, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class EntityMappingWSClientIT
{
	// This is how you should obtain a manager from a factory. Well, almost: normally you'll invoke getMyEqManagerFactory()
	// without parameters and a default file name will be picked. This is instead an extended approach, needed to cope 
	// with client/server conflicting files in the Maven-built environment.
	//
	private EntityMappingManager mmgr =  Resources.getInstance ()
		.getMyEqManagerFactory ( CLI_SPRING_CONFIG_FILE_NAME ).newEntityMappingManager ();
	
	@Test
	public void testGet ()
	{
		EntityMappingSearchResult result = mmgr.getMappings ( false, "test.testweb.service6:acc1", "test.testweb.service6:foo" );
		assertNotNull ( "'mapping get' didn't work!", result );

		String resultStr = result.toString ();
		
		out.println ( "\n\n ====================== '/mapping/get' says:\n" + resultStr + "============================" );
		out.println ( "\n\n\n ---------------------------> WARNING!!! It is known that this test DOES NOT WORK with Java < 7\n\n" );
		
		assertTrue ( "Wrong result from 'mapping get' (service8/acc2)!", 
			resultStr.toString ().contains ( "test.testweb.service8" ) && resultStr.contains ( "acc2" )
		);
		assertTrue ( "Wrong result from 'repository get' (service6/acc3)!", 
			resultStr.contains ( "test.testweb.service6" ) && resultStr.contains ( "acc3" ) 
		);
		assertTrue ( "Wrong result from 'repository get' (service7/acc1)!", 
			resultStr.contains ( "test.testweb.service7" ) && resultStr.contains ( "acc1" ) 
		);
	}
	

	@Test
	public void testGetForTarget ()
	{
		EntityMappingSearchResult result = mmgr.getMappingsForTarget ( false, "test.testweb.service6", "test.testweb.service7:acc1" );
		assertNotNull ( "'mapping get-target' didn't work!", result );

		String resultStr = result.toString ();
		
		out.println ( "\n\n ====================== '/mapping/get-target' says:\n" + resultStr + "============================" );
		out.println ( "\n\n\n ---------------------------> WARNING!!! It is known that this test MIGHT NOT WORK with Java < 7\n\n" );
		
		assertTrue ( "Wrong result from 'mapping get-target' (service6/acc4)!", 
			resultStr.toString ().contains ( "test.testweb.service6" ) && resultStr.contains ( "acc4" )
		);
		assertTrue ( "Wrong result from 'mapping get-target' (service6/acc3)!", 
			resultStr.contains ( "test.testweb.service6" ) && resultStr.contains ( "acc3" ) 
		);
		assertTrue ( "Wrong result from 'mapping get-target' (service6/acc1)!", 
			resultStr.contains ( "test.testweb.service6" ) && resultStr.contains ( "acc1" ) 
		);
		assertFalse ( "Wrong result from 'mapping get-target' (service7) should not be here!", 
			resultStr.contains ( "test.testweb.service7" ) 
		);
	}

	
	@Test
	public void testGetForTargetWithUris ()
	{
		String uri = "http://somewhere.in.the.net/testweb/service7/someType1/acc1";
		
		EntityMappingSearchResult result = mmgr.getMappingsForTarget ( 
			false, "test.testweb.service6", "<" + uri + ">" 
		);
		assertNotNull ( "'mapping get-target' didn't work!", result );

		String resultStr = result.toString ();
		
		out.println ( "\n\n ====================== '/mapping/get-target' says:\n" + resultStr + "============================" );
		out.println ( "\n\n\n ---------------------------> WARNING!!! It is known that this test MIGHT NOT WORK with Java < 7\n\n" );
		
		assertTrue ( "Wrong result from 'mapping get-target' (service6/acc4)!", 
			resultStr.toString ().contains ( "test.testweb.service6" ) && resultStr.contains ( "acc4" )
		);
		assertTrue ( "Wrong result from 'mapping get-target' (service6/acc3)!", 
			resultStr.contains ( "test.testweb.service6" ) && resultStr.contains ( "acc3" ) 
		);
		assertTrue ( "Wrong result from 'mapping get-target' (service6/acc1)!", 
			resultStr.contains ( "test.testweb.service6" ) && resultStr.contains ( "acc1" ) 
		);
		assertFalse ( "Wrong result from 'mapping get-target' (service7) should not be here!", 
			resultStr.contains ( "test.testweb.service7" ) 
		);
	}

	
	
	
	@Test
	public void testAuthentication ()
	{
		catchException ( mmgr ).storeMappingBundle ( 
			"test.testweb.service6:new-acc1", "test.testweb.service7:new-acc2", "test.testweb.service7:new-acc3" );
		assertTrue ( "Authenticated EntityMappingManager.store() didn't work!", caughtException () instanceof SecurityException );

		// We don't want to import the whole server project just to access the test user constants 
		mmgr.setAuthenticationCredentials ( "test.editor", "test.secret" );
		
		mmgr.storeMappingBundle ( 
			"test.testweb.service6:new-acc1", "test.testweb.service7:new-acc2", "test.testweb.service7:new-acc3" );
		
		EntityMappingSearchResult sr = mmgr.getMappings ( true, "test.testweb.service7:new-acc2" );
		out.println ( "\n\n ================== authenticated '/mapping/get' says:\n" + sr + "=======================" );

		
		Collection<Bundle> bundles = sr.getBundles ();
		assertEquals ( "Wrong no of bundles saved by the authenticated user!", 1, bundles.size () );
		assertEquals ( "Wrong no of mappings saved by the authenticated user!", 3, bundles.iterator ().next ().getEntities ().size () );

		// TODO: Make stuff private
		
		
		// Deletion
		
		assertEquals ( "deleteMappings() didn't return a correct value!", 3, mmgr.deleteMappings ( "test.testweb.service6:new-acc1" ) );
		
		sr = mmgr.getMappings ( true, "test.testweb.service7:new-acc2" );
		out.println ( "\n\n ================== after 'mapping/delete-mappings' I get:\n" + sr + "=======================" );
		assertTrue ( "deleteMappings() didn't work!", mmgr.getMappings ( false, "test.testweb.service7:new-acc2" ).getBundles ().isEmpty () );
		
	}

	
	@Test
	public void testUrisAndAccessControl ()
	{
		ManagerFactory managerFactory = Resources.getInstance ().getMyEqManagerFactory ( CLI_SPRING_CONFIG_FILE_NAME );
		
		Service service1 = new Service ( "test.testemdao.service1", "testemdao.someType1", "A Test Service 1", "The Description of a Test Service 1" );
		service1.setUriPattern ( "http://test.testemdao.com/service1/$id" );
		Service service2 = new Service ( "test.testemdao.service2", "testemdao.someType1", "A Test Service 2", "The Description of a Test Service 2" );
		service2.setUriPattern ( "http://test.testemdao.com/service2/$id" );

		ServiceManager servMgr = managerFactory.newServiceManager ( "test.editor", "test.secret" );
		servMgr.storeServices ( service1, service2 );
		
		AccessControlManager acMgr = managerFactory.newAccessControlManager ( "test.editor", "test.secret" );
		EntityMappingManager emMgr = managerFactory.newEntityMappingManager ( "test.editor", "test.secret" );
		
	  emMgr.storeMappingBundle ( 
	  	service1.getName () + ":b1.1", "<" + EntityIdResolver.buildUriFromAcc ( "b2.1", service2.getUriPattern () ) + ">" 
	  );

	  EntityMappingSearchResult emsr = emMgr.getMappings ( true, service2.getName () + ":b2.1" );
	  out.println ( "\n\nStored mappings:\n" + emsr + "\n\n" );
	  
	  assertTrue ( "Private entity is not created!", emsr.getBundles ().iterator().next ().getEntities ()
			.contains ( new Entity ( service1, "b1.1" ) )
	  );

	  acMgr.setServicesVisibility ( "false", "null", false, service1.getName () );
	  acMgr.setEntitiesVisibility ( "null", "null", 
	  	"<" + EntityIdResolver.buildUriFromAcc ( "b1.1", service1.getUriPattern () ) + ">" 
	  );
	  
		emMgr = managerFactory.newEntityMappingManager ();
	  emsr = emMgr.getMappings ( true, service2.getName () + ":b2.1" );
	  out.println ( "\n\nProtected mappings:\n" + emsr + "\n\n" );
		assertFalse ( "Private entity is accessible!", emsr.getBundles ().iterator().next ().getEntities ()
			.contains ( new Entity ( service1, "b1.1" ) )
	  );
		
		// Clean up
		emMgr = managerFactory.newEntityMappingManager ( "test.editor", "test.secret" );
		emMgr.deleteMappings ( 
			service2.getName () + ":<" + buildUriFromAcc ( "b2.1", service2.getUriPattern () ) + ">"
		);
		emsr = emMgr.getMappings ( true, service1.getName () + ":b1.1" );
		assertTrue ( "Test data not deleted!", emsr.getBundles ().isEmpty () );		
	}

}
