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
import uk.ac.ebi.fg.myequivalents.utils.EntityMappingUtils;
import uk.ac.ebi.utils.time.XStopWatch;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;

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

	/** No of services that are generated for the test */
	public final static int NSERVICES = 20;
	  
	/** No of types generated for the test */
	public final int NTYPES = 5; 
	  
	/** No of random bundles generated for the test 
	 *  This is overridden by the system property "myequivalents.test.scaling.nbundles"
	 */
	public final static int NBUNDLES = 100; 
	  
	/** Bundles have a random size between 2 and this value */
	public final static int MAX_BUNDLE_SIZE = 5;
	  
  /** Sometimes pair of entities mappings are stored as mappings, in addition to storing a whole bundle. this is 
   * done to cover the performance of all operations. This is the ratio (range is 0-100) with which this operation 
   * is done. 
   */
	public final static int SINGLE_MAPPING_RATIO = 30;
	
	/**
	 * How many random readings per thread are done by {@link #readRandomMappings()}.
	 * 
	 */
	public final static int NREADINGS = 1000;
	
	/**
	 * A number of readings in methods like {@link #readRandomMappings()} are about non-existing mappings
	 */
	public final static int VOID_READING_RATIO = 25;

	/**
	 * How many parallel threads are instantiated that run methods like {@link #readRandomMappings()}.
	 */
	public final static int NREADING_THREADS = 100;
	
	/**
	 * Random entities are generated and the IDs put here.
	 */
	public final static String TEST_ENTITIES_FILE_PATH = "target/random_generated_entity_ids.lst";
	
	@Rule
	public ConcurrentRule concurrentRule = new ConcurrentRule ();
	
	
	private Logger log = LoggerFactory.getLogger ( Resources.class );

	/**
	 * Creates a bunch of services, numbered from 1 to {@link #NSERVICES}.  
	 */
	private void initRandomGeneration ()
	{
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
		
		} // if  mgrFact
		
		
		ServiceManager serviceMgr = Resources.getInstance ().getMyEqManagerFactory ().newServiceManager ( 
			editorUser.getEmail (), editorSecret 
		);
		Service[] services = new Service [ NSERVICES ];
		
		for ( int i = 1; i <= NSERVICES; i++ )
		{
			int typeIdx = ( i % NTYPES + 1 );
			Service service = new Service ( 
				"test.scaling.service" + i, "test.scaling.someType" + typeIdx, "A Test Service " + i, "The Description of a Test Service " + i );
			service.setUriPrefix ( "http://somewhere.in.the.net/test/scaling/service/" + i );
			service.setUriPattern ( "http://somewhere.in.the.net/test/scaling/service" + i + "/someType" + typeIdx + "/${accession}" );
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
		initRandomGeneration ();
		Random rnd = new Random ( System.currentTimeMillis () );
		int entIdx = 0;
		
		int nExecutedOperations = 0;
		
		ManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager (
			editorUser.getEmail (), editorSecret 
		);
		
		PrintStream out = new PrintStream ( new BufferedOutputStream ( new FileOutputStream ( new File ( 
			TEST_ENTITIES_FILE_PATH ) )) );
		
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
				String entityId = "test.scaling.service" + iserv + ":test.scaling.entity" + entIdx;
				entityIds [ ient ] = entityId;
				out.println ( entityId );
				
				// From time to time, use this kind of storage too, just to be sure it's fast enough
				boolean mappingStored = ient > 0 && rnd.nextInt ( 100 ) < SINGLE_MAPPING_RATIO;
				if ( mappingStored )
				{
					int prevEntId = entIdx - ( rnd.nextInt ( ient ) + 1 );
					String entityPrevId = "test.scaling.service" + iserv + ":test.scaling.entity" + prevEntId;
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
			log.debug ( "" + ibundle + " done" );
		
		} // for ibundle
		
		
		if ( mgrFact instanceof DbManagerFactory )
		{
			// Count how many entities you actually saved
			EntityManager em = ((DbManagerFactory) mgrFact).getEntityManagerFactory ().createEntityManager ();
			long nents = ((Number) em.createNativeQuery ( 
				"select count(*) from ENTITY_MAPPING where SERVICE_NAME LIKE '%scaling%'" ).getSingleResult ()).longValue ();
			
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
	@Test @Ignore ( "Not a proper JUnit test, very time-consuming" )
	//@Concurrent ( count = NREADING_THREADS ) // Cause parallel runs of this test 
	public void readRandomMappings () throws Exception
	{
		generateRandomMappings ();
		
		ManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ();

		Random rnd = new Random ( System.currentTimeMillis () );

		List<String> entityIds = FileUtils.readLines ( new File ( TEST_ENTITIES_FILE_PATH )  );
		
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
			}
			
			boolean wantRawResult = rnd.nextBoolean ();
			
			stopw.resumeOrStart ();
			EntityMappingSearchResult mappings = mapMgr.getMappings ( wantRawResult, entityId );
			String payLoad = mappings.toString (); // simulate a reading operation
			stopw.suspend ();

			//System.out.println ( "---- Mappings for " + entityId + ":\n" + mappings );
			if ( nreading % 100 == 0 ) log.info ( "--- Done {} reads", nreading );
		
		} // for nreading
		
		
		if ( mgrFact instanceof DbManagerFactory )
		{
			// How many mapping entities you worked with?
			EntityManager em = ((DbManagerFactory) mgrFact).getEntityManagerFactory ().createEntityManager ();
			long nents = ((Number) em.createNativeQuery ( 
					"select count(*) from ENTITY_MAPPING where SERVICE_NAME LIKE '%scaling%'" ).getSingleResult ()).longValue ();
			
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

		List<String> entityIds = FileUtils.readLines ( new File ( TEST_ENTITIES_FILE_PATH )  );
		int nreads = entityIds.size ();
		
		XStopWatch stopw = new XStopWatch ();
		for ( int ireading = 0; ireading < nreads; ireading++ )
		{
			String entityId = entityIds.get ( ireading );
			
			boolean wantRawResult = rnd.nextBoolean ();
			
			stopw.resumeOrStart ();
			EntityMappingSearchResult mappings = mapMgr.getMappings ( wantRawResult, entityId );
			String payLoad = mappings.toString (); // simulate a reading operation
			stopw.suspend ();
			// System.out.println ( "---- Mappings for " + entityId + ":\n" + mappings );
			
			if ( ireading % 100 == 0 ) log.info ( "--- Done {} reads", ireading );
		
		} // for ireading
		
		
		if ( mgrFact instanceof DbManagerFactory )
		{
			// How many mapping entities you worked with?
			EntityManager em = ((DbManagerFactory) mgrFact).getEntityManagerFactory ().createEntityManager ();
			long nents = ((Number) em.createNativeQuery ( 
				"select count(*) from ENTITY_MAPPING" ).getSingleResult ()).longValue ();
			
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
	//@Concurrent ( count = NREADING_THREADS ) // Cause parallel runs of this test 
	public void readRandomMappingProvs () throws Exception
	{
		generateRandomMappings ();

		ProvManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		ProvRegistryManager provMgr = mgrFact.newProvRegistryManager ( editorUser.getEmail(), editorSecret );
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ( editorUser.getEmail(), editorSecret );
		
		Random rnd = new Random ( System.currentTimeMillis () );

		List<String> entityIds = FileUtils.readLines ( new File ( TEST_ENTITIES_FILE_PATH )  );
		
		XStopWatch stopw = new XStopWatch ();
		for ( int nreading = 1; nreading <= NREADINGS; nreading++ )
		{
			String entityId1 = null, entityId2 = null;

			int entIdx = rnd.nextInt ( entityIds.size () );
			entityId1 = entityIds.get ( entIdx );
			String[] entityId1Chunks = EntityMappingUtils.parseEntityId ( entityId1 );

			// Get the second entity among those mapped by this
			EntityMappingSearchResult maps = mapMgr.getMappings ( true, entityId1 );
			Iterator<EntityMappingSearchResult.Bundle> mbsItr = maps.getBundles ().iterator ();
			if ( !mbsItr.hasNext () ) continue;
			for ( Entity ment: mbsItr.next ().getEntities () )
				if ( !( ment.getServiceName ().equals ( entityId1Chunks[ 0 ] ) && ment.getAccession ().equals ( entityId1Chunks[ 1 ] ) ) )
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
			String payLoad = provs.toString (); // simulate a reading operation
			stopw.suspend ();
			
			//log.info ( MessageFormat.format ( "---- Provs for ({0}, {1}):\n{2}", entityId1, entityId2, payLoad ) );
			if ( nreading % 100 == 0 ) log.info ( "--- Done {} reads", nreading );
		
		} // for nreading
		
		
		if ( mgrFact instanceof DbManagerFactory )
		{
			// How many mapping entities you worked with?
			EntityManager em = ((DbManagerFactory) mgrFact).getEntityManagerFactory ().createEntityManager ();
			long nents = ((Number) em.createNativeQuery ( 
					"select count(*) from ENTITY_MAPPING where SERVICE_NAME LIKE '%scaling%'" ).getSingleResult ()).longValue ();
			
			log.info ( String.format ( "-------- Test finished, I've read provenance records from %d mappings (from %d entities) in %f secs -------", 
				NREADINGS, nents, stopw.getTime () / 1000.0 ));
		}
		else
			// We have no means to get the no. of DB records, just skip it
			log.info ( String.format ( "-------- Test finished, I've read provenance records from %d mappings in %f secs -------", 
				NREADINGS, stopw.getTime () / 1000.0 ));
		
	} // readRandomMappingProvs()

}
