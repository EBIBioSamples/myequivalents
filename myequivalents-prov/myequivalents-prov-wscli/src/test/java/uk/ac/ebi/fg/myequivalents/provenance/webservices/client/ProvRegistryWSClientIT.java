package uk.ac.ebi.fg.myequivalents.provenance.webservices.client;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static uk.ac.ebi.fg.myequivalents.provenance.webservices.server.ProvRegistryWebService.e;
import static uk.ac.ebi.fg.myequivalents.provenance.webservices.server.ProvRegistryWebService.e1;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.webservices.server.test.WebTestDataInitializer;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>27 Oct 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvRegistryWSClientIT
{
	// The client is configured with this Spring file, not the usual one.
	public static final String CLI_SPRING_CONFIG_FILE_NAME = "myeq-cli-config.xml"; 

	// Default is http://localhost:8080/myequivalents/ws
	// We use a non-standard port here cause 8080 is often already taken on EBI hosts
	//
  public static final String WS_BASE_URL = "http://localhost:10973/ws";
	//public static final String WS_BASE_URL = "http://localhost:8080/ws";

	// Here we get the specific factory, since we want newProvRegistryManager() 
	private ProvManagerFactory mgrFact = Resources.getInstance ( ).getMyEqManagerFactory ( CLI_SPRING_CONFIG_FILE_NAME );

  
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
		
	
	/**
	 * WARNING: this test might purge {@link ProvenanceRegisterEntry}s in production, which you might want to keep
	 * (in most cases, it's fine to remove them, but see the comments inside the method's implementation).
	 */
	@Test
	public void testProvenanceManager () throws InterruptedException
	{				
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager (
			WebTestDataInitializer.adminUser.getEmail (), WebTestDataInitializer.adminSecret
		);
		
		// Create the test data we need
		((ProvRegistryWSClient) regMgr)._createTestProvenanceEntries ();
		
		List<ProvenanceRegisterEntry> result = regMgr.find ( 
			"foo.user%", null, new DateTime ().minusDays ( 3 ).toDate (), null, null
		);

		assertEquals ( "find() doesn't work!", 2, result.size () );
		
		// We cannot use ProvenanceRegisterEntry.equals(), cause we don't know wich timestamp wss given to e, e1
		ProvenanceRegisterEntry mye = result.get ( 0 ), mye1 = result.get ( 1 );
		if ( !e.getUserEmail ().equals ( mye.getUserEmail () ) ) {
			mye = result.get ( 1 ); mye1 = result.get ( 0 );
		}
		assertTrue ( "e is not in the find() result!", 
			e.getUserEmail ().equals ( mye.getUserEmail () )
			&& e.getOperation ().equals ( mye.getOperation () )
			&& e.getParameters ().equals ( mye.getParameters () )
		);
		assertTrue ( "e1 is not in the find() result!", 
			e1.getUserEmail ().equals ( mye1.getUserEmail () )
			&& e1.getOperation ().equals ( mye1.getOperation () )
			&& e1.getParameters ().equals ( mye1.getParameters () )
		);
		
		
		String resultStr = regMgr.findAs ( "xml", "foo.user%", "foo.op%", null, null, Arrays.asList ( p ( "foo.entity", "acc%" ) ) );
		log.info ( "---- XML Result -----\n" + resultStr );
		
		assertTrue ( "Wrong XML result!", resultStr.contains ( "<entry operation=\"foo.op\"" ) );
		assertTrue ( "Wrong XML result!", resultStr.contains ( "<parameter" ) );
		assertTrue ( "Wrong XML result!", resultStr.contains ( "value=\"acc2\"") );
		assertTrue ( "Wrong XML result!", resultStr.contains ( "value-type=\"foo.entity\"/>" ) );
		assertTrue ( "Wrong XML result!", resultStr.contains ( "timestamp" ) );
		
		// WARNING: this might be dangerous, if you test against a production server, which is running for days and 
		// contains entries you don't want to purge
		regMgr.purge ( new DateTime ( mye.getTimestamp () ).minusMinutes ( 1 ).toDate (), null );
		assertEquals ( "purge() didn't work!", 1, regMgr.find ( "foo.user%", null, null, null, null ).size () );
	}
}
