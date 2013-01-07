/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.webservices.client;

import static java.lang.System.out;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;

/**
 * A dirty/quick test for a new server of the web service. 
 * 
 * <dl><dt>date</dt><dd>Dec 3, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MyServerWSTest
{
	@Test @Ignore ( "This is a sort of quick/dirty manual test against your new web service instance. Usually disabled" )
	public void testGetMapping ()
	{

		EntityMappingManager mmgr = new EntityMappingWSClient ( "http://wwwdev.ebi.ac.uk/fg/myequivalents/ws" );
		EntityMappingSearchResult result = mmgr.getMappings ( false, "biosamples-service:SAMEA1006750" );
		String resultStr = result.toString ();
		
		out.println ( "\n\n ====================== '/mapping/get' says:\n" + resultStr + "============================" );
		out.println ( "\n\n\n ---------------------------> WARNING!!! It is known that this test DOES NOT WORK with Java < 7\n\n" );
		
		assertNotNull ( "'mapping get' didn't work!", result );
		assertTrue ( "Wrong result from 'mapping get' (ena-repository/DRS000019)!", 
			resultStr.toString ().contains ( "ena-repository" ) && resultStr.contains ( "DRS000019" )
		);
	}
}
