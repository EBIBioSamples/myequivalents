/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.test.scaling;

import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * Perform some scaling tests. 
 * 
 * <dl><dt>date</dt><dd>Mar 1, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class RandomMappingsTest
{
	
	/** No of services that are generated for the test */
	public final int NSERVICES = 20;
	  
	/** No of types generated for the test */
	public final int NTYPES = 5; 
	  
	/** No of random bundles generated for the test */
	public final int NBUNDLES = 1000; 
	  
	/** Bundles have a random size between 2 and this value */
	public final int MAX_BUNDLE_SIZE = 200;
	  
  /** Sometimes pair of entities mappings are stored as mappings, in addition to storing a whole bundle. this is 
   * done to cover the performance of all operations. This is the ration (range is 0-100) with which this operation 
   * is done. 
   */
	public final int SINGLE_MAPPING_RATIO = 30;
	
	private Logger log = LoggerFactory.getLogger ( Resources.class );

	/**
	 * Creates a bunch of services, numbered from 1 to {@link #NSERVICES}.  
	 */
	@Before
	public void init ()
	{
		ServiceManager serviceMgr = Resources.getInstance ().getMyEqManagerFactory ().newServiceManager ();
		Service[] services = new Service [ NSERVICES ];
		
		for ( int i = 1; i <= NSERVICES; i++ )
		{
			int typeIdx = (i % NTYPES + 1);
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
	@Test @Ignore ( "Not a proper JUnit test, very time-consuming" )
	public void generateRandomMappings ()
	{
		Random rnd = new Random ( System.currentTimeMillis () );
		int entIdx = 0;
		EntityMappingManager mapMgr = Resources.getInstance ().getMyEqManagerFactory ().newEntityMappingManager ();
		
		// bundle loop
		for ( int ibundle = 1; ibundle <= NBUNDLES ; ibundle++ )
		{
			int bundleSize = rnd.nextInt ( MAX_BUNDLE_SIZE - 2 + 1) + 2;
			String[] entityIds = new String [ bundleSize ];
			
			// entity loop
			for ( int ient = 0; ient < bundleSize; ient++ )
			{
				int iserv = rnd.nextInt ( NSERVICES ) + 1;
				String entityId = "test.scaling.service" + iserv + ":test.scaling.entity" + entIdx;
				entityIds [ ient ] = entityId;
				
				// From time to time, use this kind of storage too, just to be sure it's fast enough
				boolean mappingStored = ient > 0 && rnd.nextInt ( 100 ) < SINGLE_MAPPING_RATIO;
				if ( mappingStored )
				{
					int prevEntId = entIdx - ( rnd.nextInt ( ient ) + 1 );
					String entityPrevId = "test.scaling.service" + iserv + ":test.scaling.entity" + prevEntId;
					mapMgr.storeMappings ( entityPrevId, entityId );
				}
				
				log.trace ( "(" + ibundle + ", " + ient + ") done " + (mappingStored ? " (stored immediately)" : "" ) );
				entIdx++;
			}
			mapMgr.storeMappingBundle ( entityIds );
			log.debug ( "" + ibundle + " done" );
		}
	} // generateRandomMappings()
	
}
