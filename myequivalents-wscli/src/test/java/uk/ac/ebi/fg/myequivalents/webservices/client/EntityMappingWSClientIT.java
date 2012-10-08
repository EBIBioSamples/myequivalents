/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.webservices.client;

import static java.lang.System.out;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;

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
	@Test
	public void testGet ()
	{
		EntityMappingManager mmgr = new EntityMappingWSClient ();
		EntityMappingSearchResult result = mmgr.getMappings ( false, "test.testweb.service6:acc1", "test.testweb.service6:foo" );
		String resultStr = result.toString ();
		
		out.println ( "\n\n ====================== '/mapping/get' says:\n" + resultStr + "============================" );
		
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
	
}
