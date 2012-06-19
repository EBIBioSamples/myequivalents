package uk.ac.ebi.fg.myequivalents.managers;

import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.managers.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.test.TestEntityMgrProvider;

public class EntityMappingManagerTest
{	
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider (
		Resources.getInstance ().getEntityManagerFactory ()
	);

	private EntityMappingManager emMgr;
	private ServiceDAO serviceDao;

	private Service service1, service2, service3, service4, service5;
	private ServiceCollection sc1;
	private Repository repo1;
	
	@Before
	public void init ()
	{
		EntityManager em = emProvider.getEntityManager ();
		serviceDao = new ServiceDAO ( em );
		emMgr = new EntityMappingManager ();
		
		service1 = new Service ( "test.testemsrv.service1", "testemsrv.someType1", "A Test Service 1", "The Description of a Test Service 1" );
		service1.setUriPrefix ( "http://somewhere.in.the.net/testemsrv/service1/" );
		service1.setUriPattern ( "http://somewhere.in.the.net/testemsrv/service1/someType1/${accession}" );
				
		sc1 = new ServiceCollection ( 
			"test.testemsrv.serviceColl1", service1.getEntityType (), "Test Service Collection 1", "The Description of the SC 1" 
		);
		service1.setServiceCollection ( sc1 );
		
		repo1 = new Repository ( "test.testemsrv.repo1", "Test Repo 1", "The Description of Repo1" );
		service1.setRepository ( repo1 );

		service2 = new Service ( "test.testemsrv.service2", "testemsrv.someType1", "A Test Service 2", "The Description of a Test Service 2" );
		service1.setUriPrefix ( "http://somewhere.in.the.net/testemsrv/service2/" );

		service3 = new Service ( "test.testemsrv.service3", "testemsrv.someType2", "A Test Service 3", "The Description of a Test Service 3" );
		service3.setUriPrefix ( "http://somewhere-else.in.the.net/testemsrv/service3/" );

		service4 = new Service ( "test.testemsrv.service4", "testemsrv.someType2", "A Test Service 4", "The Description of a Test Service 4" );
		service4.setUriPrefix ( "http://somewhere-else.in.the.net/testemsrv/service4/" );

		service5 = new Service ( "test.testemsrv.service5", "testemsrv.someType2", "A Test Service 5", "The Description of a Test Service 5" );
		service5.setUriPrefix ( "http://somewhere-else.in.the.net/testemsrv/service5/" );

		EntityTransaction ts = em.getTransaction ();

		ts.begin ();
		emMgr.deleteEntities ( service1.getName (), "acc1", service2.getName (), "acc2" );
		serviceDao.delete ( service1 );
		serviceDao.delete ( service2 );
		serviceDao.delete ( service3 );
		serviceDao.delete ( service4 );
		serviceDao.delete ( service5 );
		
		assertEquals ( "Entities not deleted!", 0, emMgr.getMappings ( 
				false, false, false, service1.getName (), "acc1", service2.getName (), "acc2" 
			).getBundles ().size () 
		);
		// TODO: more 
		
		serviceDao.store ( service1 );
		serviceDao.store ( service2 );
		serviceDao.store ( service3 );
		serviceDao.store ( service4 );
		serviceDao.store ( service5 );
		ts.commit ();
	}
	
	@Test
	public void testBasicSearch ()
	{
		// b1 ( (s1, b1.1) (s2, b1.2) (s1, b1.3) (s1, b1.4)
	  // b2 ( (s2, b2.1) (s3, b2.2) )
	  emMgr.storeMappingBundle ( 
	  	service1.getName (), "b1.1", service2.getName (), "b1.2", service1.getName (), "b1.3" );
	  emMgr.storeMappings ( 
	  	service1.getName (), "b1.4", service1.getName (), "b1.1",
	  	service2.getName (), "b2.1", service3.getName (), "b2.2"
	  );

		EntityMappingSearchResult result = emMgr.getMappings ( 
			true, true, true, service1.getName (), "b1.3", service3.getName (), "b2.2" 
		);
		
		out.println ( "\nResult:" );
		out.println ( result );
		
		assertEquals ( "Wrong no. of services returned!", 3, result.getServices ().size () );
		
		{
			boolean found1 = false, found2 = false, found3 = false;
			for ( Service service: result.getServices () )
				if ( service1.equals ( service ) ) found1 = true;
				else if ( service2.equals ( service ) ) found2 = true;
				else if ( service3.equals ( service ) ) found3 = true;
			
			assertTrue ( "Service 1 not found in search results!", found1 );
			assertTrue ( "Service 2 not found in search results!", found2 );
			assertTrue ( "Service 3 not found in search results!", found3 );
		}

		Collection<Bundle> bsets = result.getBundles ();
		assertEquals ( "Wrong no of bundles in search result!", 2, bsets.size () );
		
		{
			// b1 ( (s1, b1.1) (s2, b1.2) (s1, b1.3) (s1, b1.4)
		  // b2 ( (s2, b2.1) (s3, b2.2) )
			Bundle bundle1 = null, bundle2 = null;
			Entity b13 = null;
			for ( Bundle bset: bsets )
				for ( Entity entity: bset.getEntities () )
					if ( service1.equals ( entity.getService () ) && "b1.1".equals ( entity.getAccession () ) )
						bundle1 = bset;
					else if ( service3.equals ( entity.getService () ) && "b2.2".equals ( entity.getAccession () ) )
						bundle2 = bset;
					else if ( service1.getName().equals ( entity.getServiceName () ) && "b1.3".equals ( entity.getAccession () ))
						b13 = entity;
			
			assertNotNull ( "b1.3 not found in search results!", b13 );
			assertEquals ( "Bad URI returned for b13!", 
				"http://somewhere.in.the.net/testemsrv/service1/someType1/b1.3", b13.getURI () 
			);
			
			assertNotNull ( "Bundle 1 not found in search result!", bundle1 );
			assertNotNull ( "Bundle 2 not found in search result!", bundle2 );

			assertEquals ( "Wrong size for set 1", 4, bundle1.getEntities ().size () );
			assertEquals ( "Wrong size for set 2", 2, bundle2.getEntities ().size () );
			
			assertTrue ( "b1.1 is not in set1!", bundle1.getEntities ().contains ( new Entity ( service1, "b1.1" ) ) );
			assertTrue ( "b1.2 is not in set1!", bundle1.getEntities ().contains ( new Entity ( service2, "b1.2" ) ) );
			assertTrue ( "b1.3 is not in set1!", bundle1.getEntities ().contains ( new Entity ( service1, "b1.3" ) ) );
			assertTrue ( "b1.4 is not in set1!", bundle1.getEntities ().contains ( new Entity ( service1, "b1.4" ) ) );
			
			assertTrue ( "b2.1 is not in set2!", bundle2.getEntities ().contains ( new Entity ( service2, "b2.1" ) ) );
			assertTrue ( "b2.2 is not in set2!", bundle2.getEntities ().contains ( new Entity ( service3, "b2.2" ) ) );
		}
		
		Set<ServiceCollection> scs = result.getServiceCollections ();
		assertEquals ( "Wrong no of repo returned by the search!", 1, scs.size () );
		assertTrue ( "Repo1 not found in the search result!", scs.contains ( sc1 ) );

		Set<Repository> repos = result.getRepositories ();
		assertEquals ( "Wrong no of repo returned by the search!", 1, repos.size () );
		assertTrue ( "Repo1 not found in the search result!", repos.contains ( repo1 ) );
		
		out.println ( "\n\nXML Result:\n" + emMgr.getMappingsAs ( 
			"xml", true, true, true, service1.getName (), "b1.3", service3.getName (), "b2.2"  
		));

		// TODO Use XPath to test the XML
	}
}
