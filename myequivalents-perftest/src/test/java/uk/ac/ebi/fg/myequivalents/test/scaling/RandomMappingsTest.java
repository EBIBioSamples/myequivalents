package uk.ac.ebi.fg.myequivalents.test.scaling;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;
import uk.ac.ebi.utils.time.XStopWatch;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;

/**
 * Perform some scaling tests. 
 * 
 * <dl><dt>date</dt><dd>Mar 1, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class RandomMappingsTest
{
	// An editor is needed for writing operations.
	public static final String editorPass = "test.password";
	public static final String editorSecret = "test.secret";
	public static User editorUser = new User ( 
		"test.editor", "Test Editor", "User", editorPass, "test editor notes", Role.EDITOR, editorSecret );

	/**
	 * How many parallel threads are instantiated that run methods like {@link #readRandomMappings()}.
	 * Beware that {@link #generateRandomMappings()} will called before each reading thread 
	 * (hence {@link #NBUNDLES} * {@link #NREADING_THREADS} bundles will be generated).
	 */
	public static final int NREADING_THREADS = 1;
	
	/**
	 * How many random readings per thread are done by {@link #readRandomMappings()}.
	 * 
	 */
	public static final int NREADINGS = 5000;

	
	/** No of services that are generated for the test */
	public static final int NSERVICES = 20;
	  
	/** No of types generated for the test */
	public static final int NTYPES = 5; 
	  
	/** No of random bundles generated for the test 
	 *  This is overridden by the system property "myequivalents.test.scaling.nbundles"
	 */
	public static final int NBUNDLES = (int) 10000/NREADING_THREADS; 
	  
	/** Bundles have a random size between 2 and this value */
	public static final int MAX_BUNDLE_SIZE = 5;
	  
  /** Sometimes pair of entities mappings are stored as mappings, in addition to storing a whole bundle. this is 
   * done to cover the performance of all operations. This is the ratio (range is 0-100) with which this operation 
   * is done. 
   */
	public static final int SINGLE_MAPPING_RATIO = 0;
	
	
	public static final int URI_READING_RATIO = 50;
	
	/**
	 * A number of readings in methods like {@link #readRandomMappings()} are about non-existing mappings
	 */
	public static final int VOID_READING_RATIO = 50;

	
	/**
	 * Random entities are generated and the IDs put here.
	 */
	public static final String TEST_ENTITIES_FILE_PATH = "target/random_generated_entity_ids.lst";
	
	@Rule
	public ConcurrentRule concurrentRule = new ConcurrentRule ();
	
	private int executionIdx = -1;

	private Service[] services;
	
	
	private Logger log = LoggerFactory.getLogger ( Resources.class );
	

	/**
	 * Creates a bunch of services, numbered from 1 to {@link #NSERVICES}.
	 * This is only called when {@link #NREADING_THREADS} is 1. Cannot work in multi-thread mode. TODO: fix.
	 */
	private synchronized void initRandomGeneration () throws Exception
	{
		// Do it only once in case of multi-threading
		if ( ++this.executionIdx > 0 ) return; 
			
		ManagerFactory mgrFact = (ManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		
		if ( mgrFact instanceof DbManagerFactory )
		{
			// Sorry I can only clean-up stuff if I'm directly connected to the DB
			//
			
			EntityManager em = ((DbManagerFactory) mgrFact).getEntityManagerFactory ().createEntityManager ();
	
			// Removes existing test data
			EntityTransaction ts = em.getTransaction ();
			ts.begin ();
			em.createNativeQuery ( "delete from ENTITY_MAPPING where SERVICE_NAME LIKE '%scaling%'" ).executeUpdate ();
			em.createNativeQuery ( "delete from service where NAME LIKE '%scaling%'" ).executeUpdate ();
			em.createNativeQuery ( "delete from repository where NAME LIKE '%scaling%'" ).executeUpdate ();
			em.createNativeQuery ( "delete from service_collection where NAME LIKE '%scaling%'" ).executeUpdate ();
			
			ts.commit ();
	
			// Store an editor user
			//
			UserDao userDao = new UserDao ( em );
			ts.begin ();
			userDao.storeUnauthorized ( editorUser );
			ts.commit ();
			
			em.close ();
		
		} // if  mgrFact
		
		
		ServiceManager serviceMgr = Resources.getInstance ().getMyEqManagerFactory ().newServiceManager ( 
			editorUser.getEmail (), editorSecret 
		);
		services = new Service [ NSERVICES ];
		
		for ( int i = 1; i <= NSERVICES; i++ )
		{
			int typeIdx = ( i % NTYPES + 1 );
			Service service = new Service ( 
				"test.scaling.service" + i, "test.scaling.someType" + typeIdx, "A Test Service " + i, "The Description of a Test Service " + i );
			service.setUriPattern ( "http://somewhere.in.the.net/test/scaling/service" + i + "/someType" + typeIdx + "/$id" );
			services [ i - 1 ] = service;
		}
		
		serviceMgr.storeServices ( services );
	}
	
	
	/**
	 * Generates {@link #NBUNDLES} bundles of sizes ranging from 2 to {@link #MAX_BUNDLE_SIZE}, uses the services 
	 * defined in {@link #init()}.
	 *  
	 */
	private void generateRandomMappings () throws Exception
	{
		int execId = 0;
		synchronized ( this )
		{
			initRandomGeneration ();
			execId = this.executionIdx;
		}
		
		Random rnd = new Random ( System.currentTimeMillis () );
		int entIdx = 0;
		
		int nExecutedOperations = 0;
		
		ManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager (
			editorUser.getEmail (), editorSecret 
		);
		
		PrintStream out = new PrintStream ( new BufferedOutputStream ( new FileOutputStream ( new File ( 
			TEST_ENTITIES_FILE_PATH + ( execId == 0 ? "" : "." + execId )
		) )) );
		
		// This causes the same entities to be stored from different threads, allows you to test trnasaction
		// isolation
		//execId = 0;
		
		int nbundles = Integer.parseInt ( System.getProperty ( "myequivalents.test.scaling.nbundles", "" + NBUNDLES ) );

		// bundle loop
		XStopWatch stopw = new XStopWatch ();
		for ( int ibundle = 1; ibundle <= nbundles ; ibundle++ )
		{
			int bundleSize = rnd.nextInt ( MAX_BUNDLE_SIZE - 2 + 1) + 2;
			String[] entityIds = new String [ bundleSize ];
			
			// entity loop
			for ( int ient = 0; ient < bundleSize; ient++ )
			{
				int iserv = rnd.nextInt ( NSERVICES ) + 1;
				String entityId = "test.scaling.service" + iserv + ":test.scaling.entity_" + execId + "_" + entIdx;
				entityIds [ ient ] = entityId;
				out.println ( entityId );
				
				// From time to time, use this kind of storage too, just to be sure it's fast enough
				boolean mappingStored = ient > 0 && rnd.nextInt ( 100 ) < SINGLE_MAPPING_RATIO;
				if ( mappingStored )
				{
					int prevEntId = entIdx - ( rnd.nextInt ( ient ) + 1 );
					String entityPrevId = "test.scaling.service" + iserv + ":test.scaling.entity_" + execId + "_" + prevEntId;
					stopw.resumeOrStart ();
					mapMgr.storeMappings ( entityPrevId, entityId );
					stopw.suspend ();
					nExecutedOperations++;
				}
				
				log.trace ( "(" + ibundle + ", " + ient + ") done " + (mappingStored ? " (stored immediately)" : "" ) );
				entIdx++;
			}
			
			stopw.resumeOrStart ();
			mapMgr.storeMappingBundle ( entityIds );
			stopw.suspend ();
			nExecutedOperations++;
			
			String lmsg = ibundle + " bundles stored";
			if ( ibundle % 100 == 0 ) log.info ( lmsg ); else log.debug ( lmsg );
		
		} // for ibundle
		
		mapMgr.close ();
		
		if ( mgrFact instanceof DbManagerFactory )
		{
			// Count how many entities you actually saved
			EntityManager em = ((DbManagerFactory) mgrFact).getEntityManagerFactory ().createEntityManager ();
			long nents = ((Number) em.createNativeQuery ( 
				"select count(*) from ENTITY_MAPPING where SERVICE_NAME LIKE '%scaling%'" ).getSingleResult ()).longValue ();
			em.close ();
			
			log.info ( "-------- Initialisation done, I've stored {} entities in {} secs -------", nents, stopw.getTime () / 1000.0 );
		}
		else
			// Just count the operations, if you can't access datbase records
			log.info ( "-------- Initialisation done, I've run {} operations in {} secs -------", nExecutedOperations, stopw.getTime () / 1000.0 );
		
		
		out.close ();
		
	} // generateRandomMappings()
	
	
	/**
	 * Queries for mappings generated by {@link #generateRandomMappings()} and reports about performance.
	 */
	@Test // @Ignore ( "Not a proper JUnit test, very time-consuming" )
	@Concurrent ( count = NREADING_THREADS ) // Cause parallel runs of this test 
	public void readRandomMappings () throws Exception
	{
		generateRandomMappings ();
		
		ManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ();

		Random rnd = new Random ( System.currentTimeMillis () );

		List<String> entityIds = FileUtils.readLines ( new File ( 
			TEST_ENTITIES_FILE_PATH + ( this.executionIdx == 0 ? "" : "." + this.executionIdx 
		)));
		
		XStopWatch stopw = new XStopWatch ();
		for ( int nreading = 1; nreading <= NREADINGS; nreading++ )
		{
			String entityId = null;

			// Do a number of 'doesn't exist' readings
			boolean readVoid = rnd.nextInt ( 100 ) < VOID_READING_RATIO;
			
			if ( readVoid )
			{
				int iserv = rnd.nextInt ( 1000 );
				int entIdx = rnd.nextInt ( 1000 );
				entityId = "foo.service." + iserv + ":foo.entity." + entIdx;
			}
			else 
			{
				int entIdx = rnd.nextInt ( entityIds.size () );
				entityId = entityIds.get ( entIdx );

				// Use URI from time to time
				if ( rnd.nextInt ( 100 ) < URI_READING_RATIO ) entityId = toUriSyntax ( entityId );
			}
			
			boolean wantRawResult = rnd.nextBoolean ();
			
			stopw.resumeOrStart ();
			EntityMappingSearchResult mappings = mapMgr.getMappings ( wantRawResult, entityId );
			String payload = mappings.toString (); // simulate a reading operation
			stopw.suspend ();

			//System.out.println ( "---- Mappings for " + entityId + ":\n" + mappings );
			if ( nreading % 100 == 0 ) log.info ( "--- Done {} reads", nreading );
		
		} // for nreading
		
		mapMgr.close ();
		
		
		if ( mgrFact instanceof DbManagerFactory )
		{
			// How many mapping entities you worked with?
			EntityManager em = ((DbManagerFactory) mgrFact).getEntityManagerFactory ().createEntityManager ();
			long nents = ((Number) em.createNativeQuery ( 
					"select count(*) from ENTITY_MAPPING where SERVICE_NAME LIKE '%scaling%'" ).getSingleResult ()).longValue ();
			em.close ();
			
			log.info ( String.format ( "-------- Test finished, I've read %d mappings (from %d entities) in %f secs -------", 
				NREADINGS, nents, stopw.getTime () / 1000.0 ));
		}
		else
			// We have no means to get the no. of DB records, just skip it
			log.info ( String.format ( "-------- Test finished, I've read %d mappings in %f secs -------", 
				NREADINGS, stopw.getTime () / 1000.0 ));
		
	} // readRandomMappings()


	/**
	 * Reads all the mappings in a id list, stored in {@link #TEST_ENTITIES_FILE_PATH}.
	 */
	@Test @Ignore ( "Not a proper JUnit test, very time-consuming" )
	//@Concurrent ( count = NREADING_THREADS ) // Cause parallel runs of this test 
	public void readAllRandomMappings () throws Exception
	{
		generateRandomMappings ();

		ManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ();

		Random rnd = new Random ( System.currentTimeMillis () );

		List<String> entityIds = FileUtils.readLines ( new File (
			TEST_ENTITIES_FILE_PATH + ( this.executionIdx == 0 ? "" : "." + this.executionIdx  )
		));
		int nreads = entityIds.size ();
		
		XStopWatch stopw = new XStopWatch ();
		for ( int ireading = 0; ireading < nreads; ireading++ )
		{
			String entityId = entityIds.get ( ireading );

			// Use URI from time to time
			if ( rnd.nextInt ( 100 ) < URI_READING_RATIO ) entityId = toUriSyntax ( entityId );
			
			boolean wantRawResult = rnd.nextBoolean ();
			
			stopw.resumeOrStart ();
			EntityMappingSearchResult mappings = mapMgr.getMappings ( wantRawResult, entityId );
			String payload = mappings.toString (); // simulate a reading operation
			stopw.suspend ();
			// System.out.println ( "---- Mappings for " + entityId + ":\n" + mappings );
			
			if ( ireading % 100 == 0 ) log.info ( "--- Done {} reads", ireading );
		
		} // for ireading
		
		mapMgr.close ();
		
		if ( mgrFact instanceof DbManagerFactory )
		{
			// How many mapping entities you worked with?
			EntityManager em = ((DbManagerFactory) mgrFact).getEntityManagerFactory ().createEntityManager ();
			long nents = ((Number) em.createNativeQuery ( 
				"select count(*) from ENTITY_MAPPING" ).getSingleResult ()).longValue ();
			em.close ();
			
			log.info ( String.format ( "-------- Test finished, I've read %d mappings (from %d entities) in %f secs -------", 
				nreads, nents, stopw.getTime () / 1000.0 ));
		}
		// We have no means to get the no. of DB records, just skip it
		log.info ( String.format ( "-------- Test finished, I've read %d mappings in %f secs -------", 
			NREADINGS, stopw.getTime () / 1000.0 ));
		
	} // readAllRandomMappings()


	
	/**
	 * Queries about the provenance of mappings generated via {@link RandomMappingsTest#generateRandomMappings()} and
	 * reports about performance.
	 * 
	 * WARNING: this won't work unless you test with a Maven profile that it's using provenance extenstions. 
	 * 
	 */
	@Test @Ignore ( "Not a proper JUnit test, very time-consuming" )
	@Concurrent ( count = NREADING_THREADS ) // Cause parallel runs of this test 
	public void readRandomMappingProvs () throws Exception
	{
		generateRandomMappings ();

		ProvManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		ProvRegistryManager provMgr = mgrFact.newProvRegistryManager ( editorUser.getEmail(), editorSecret );
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ( editorUser.getEmail(), editorSecret );
		
		Random rnd = new Random ( System.currentTimeMillis () );

		List<String> entityIds = FileUtils.readLines ( new File ( 
			TEST_ENTITIES_FILE_PATH + ( this.executionIdx == 0 ? "" : "." + this.executionIdx  ) 
		));
		
		XStopWatch stopw = new XStopWatch ();
		for ( int nreading = 1; nreading <= NREADINGS; nreading++ )
		{
			String entityId1 = null, entityId2 = null;

			int entIdx = rnd.nextInt ( entityIds.size () );
			entityId1 = entityIds.get ( entIdx );
			
			// Get the second entity among those mapped by this
			EntityMappingSearchResult maps = mapMgr.getMappings ( true, entityId1 );
			Iterator<EntityMappingSearchResult.Bundle> mbsItr = maps.getBundles ().iterator ();
			if ( !mbsItr.hasNext () ) continue;
			for ( Entity ment: mbsItr.next ().getEntities () )
				if ( !( ment.getServiceName () + ":" + ment.getAccession () ).equals ( entityId1 ) )
				{
					entityId2 = ment.getServiceName () + ":" + ment.getAccession ();
					break;
			}
			
			if ( entityId2 == null ) continue;
			
			
			List<String> validUsers = null;
			boolean wantUserFilter = rnd.nextBoolean ();
			if ( wantUserFilter )
				validUsers = Arrays.asList ( editorUser.getEmail() );
			
			
			stopw.resumeOrStart ();
			Set<List<ProvenanceRegisterEntry>> provs = provMgr.findMappingProv ( entityId1, entityId2, validUsers );
			String payload = provs.toString (); // simulate a reading operation
			stopw.suspend ();
			
			//log.info ( MessageFormat.format ( "---- Provs for ({0}, {1}):\n{2}", entityId1, entityId2, payLoad ) );
			if ( nreading % 100 == 0 ) log.info ( "--- Done {} reads, elapsed time: {}s", nreading, stopw.getTime () / 1000d );
		
		} // for nreading
		

		mapMgr.close ();
		provMgr.close ();
		
		
		if ( mgrFact instanceof DbManagerFactory )
		{
			// How many mapping entities you worked with?
			EntityManager em = ((DbManagerFactory) mgrFact).getEntityManagerFactory ().createEntityManager ();
			long nents = ((Number) em.createNativeQuery ( 
				"select count(*) from ENTITY_MAPPING where SERVICE_NAME LIKE '%scaling%'" ).getSingleResult ()).longValue ();
			em.close ();
			
			log.info ( String.format ( "-------- Test finished, I've read provenance records from %d mappings (from %d entities) in %f secs -------", 
				NREADINGS, nents, stopw.getTime () / 1000.0 ));
		}
		else
			// We have no means to get the no. of DB records, just skip it
			log.info ( String.format ( "-------- Test finished, I've read provenance records from %d mappings in %f secs -------", 
				NREADINGS, stopw.getTime () / 1000.0 ));
		
	} // readRandomMappingProvs()

	
	/**
	 * Turns entityId into "&lt;uri&gt;", splitting entityId and using {@link #services}.
	 */
	private String toUriSyntax ( String entityId )
	{
		// "test.scaling.service" + iserv + ":test.scaling.entity_" + execId + "_" + entIdx
		String idchunks[] = entityId.split ( ":" );
		int iserv = Integer.parseInt ( idchunks [ 0 ].substring ( "test.scaling.service".length () ) ) - 1;
		
		Service service = services [ iserv ];
		entityId = EntityIdResolver.buildUriFromAcc ( idchunks [ 1 ], service.getUriPattern () );
		return "<" + entityId + ">";
	}
}
