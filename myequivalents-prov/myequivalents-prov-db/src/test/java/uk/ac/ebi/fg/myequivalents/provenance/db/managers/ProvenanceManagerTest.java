package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.*;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>1 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceManagerTest
{
	@BeforeClass
	public static void init ()
	{
		ProvDbServiceManagerTest.init ();
	}
	
	@AfterClass
	public static void cleanUp ()
	{
		ProvDbServiceManagerTest.cleanUp ();
	}
	
	@Test
	public void testProvenanceManager ()
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
		
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager (
			ProvDbServiceManagerTest.adminUser.getEmail (), ProvDbServiceManagerTest.testSecret
		);
		
		List<ProvenanceRegisterEntry> result = regMgr.find ( "foo.user%", null, new DateTime ().minusDays ( 3 ).toDate (), null, null );

		assertEquals ( "find() doesn't work!", 2, result.size () );
		assertTrue ( "e is not in the find() result!", result.contains ( e ) );
		assertTrue ( "e1 is not in the find() result!", result.contains ( e1 ) );
		
		
		String resultStr = regMgr.findAs ( "xml", "foo.user%", "foo.op%", null, null, Arrays.asList ( p ( "foo.entity", "acc%" ) ) );
		out.println ( "---- XML Result -----\n" + resultStr );
		
		//assertTrue ( "Wrong XML result!", resultStr.contains ( s ) );
		
		regMgr.purge ( new DateTime ().minusMinutes ( 1 ).toDate (), null );
		em.close (); // flushes data for certain DBs (eg, H2)
		
		provDao = new ProvenanceRegisterEntryDAO ( em = mgrFact.getEntityManagerFactory ().createEntityManager () );
		assertEquals ( "purge() didn't work!", 0, provDao.find ( "foo.user%", null, null ).size () );
	}
}
