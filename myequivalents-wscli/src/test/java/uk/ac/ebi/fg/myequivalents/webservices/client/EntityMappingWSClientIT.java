package uk.ac.ebi.fg.myequivalents.webservices.client;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static java.lang.System.out;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;


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
	// Default is http://localhost:8080/myequivalents/ws
	// We use a non-standard port here cause 8080 is often already taken on EBI hosts
	//
	private EntityMappingManager mmgr = new EntityMappingWSClient ( "http://localhost:10973/ws" );
	// DEBUG EntityMappingManager mmgr = new EntityMappingWSClient ( "http://localhost:8080/ws" );
	
	@Test
	public void testGet ()
	{
		EntityMappingSearchResult result = mmgr.getMappings ( false, "test.testweb.service6:acc1", "test.testweb.service6:foo" );
		String resultStr = result.toString ();
		
		out.println ( "\n\n ====================== '/mapping/get' says:\n" + resultStr + "============================" );
		out.println ( "\n\n\n ---------------------------> WARNING!!! It is known that this test DOES NOT WORK with Java < 7\n\n" );
		
		assertNotNull ( "'mapping get' didn't work!", result );
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
		String resultStr = result.toString ();
		
		out.println ( "\n\n ====================== '/mapping/get-target' says:\n" + resultStr + "============================" );
		out.println ( "\n\n\n ---------------------------> WARNING!!! It is known that this test MIGHT NOT WORK with Java < 7\n\n" );
		
		assertNotNull ( "'mapping get-target' didn't work!", result );
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


}
