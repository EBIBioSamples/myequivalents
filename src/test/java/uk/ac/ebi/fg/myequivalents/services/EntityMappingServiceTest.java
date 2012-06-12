package uk.ac.ebi.fg.myequivalents.services;

import static junit.framework.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;
import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.services.EntityMappingService;
import uk.ac.ebi.fg.myequivalents.test.TestEntityMgrFactoryProvider;
import uk.ac.ebi.fg.myequivalents.test.TestEntityMgrProvider;

import static java.lang.System.out;

public class EntityMappingServiceTest
{
	@ClassRule
	public static TestEntityMgrFactoryProvider emfProvider = new TestEntityMgrFactoryProvider ();
	
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( emfProvider.getEntityManagerFactory () );

	private EntityMappingDAO emDao;
	private ServiceDAO serviceDao;

	private Service service1, service2, service3, service4, service5;
	
	@Before
	public void init ()
	{
		EntityManager em = emProvider.getEntityManager ();
		serviceDao = new ServiceDAO ( em );
		emDao = new EntityMappingDAO ( em );
		
		service1 = new Service ( "test.testemdao.service1", "testemdao.someType1", "A Test Service 1", "The Description of a Test Service 1" );
		service1.setUriPrefix ( "http://somewhere.in.the.net/testemdao/service1/" );
		service1.setUriPattern ( "http://somewhere.in.the.net/testemdao/service1/someType1/${accession}" );

		service2 = new Service ( "test.testemdao.service2", "testemdao.someType1", "A Test Service 2", "The Description of a Test Service 2" );
		service1.setUriPrefix ( "http://somewhere.in.the.net/testemdao/service2/" );

		service3 = new Service ( "test.testemdao.service3", "testemdao.someType2", "A Test Service 3", "The Description of a Test Service 3" );
		service3.setUriPrefix ( "http://somewhere-else.in.the.net/testemdao/service3/" );

		service4 = new Service ( "test.testemdao.service4", "testemdao.someType2", "A Test Service 4", "The Description of a Test Service 4" );
		service4.setUriPrefix ( "http://somewhere-else.in.the.net/testemdao/service4/" );

		service5 = new Service ( "test.testemdao.service5", "testemdao.someType2", "A Test Service 5", "The Description of a Test Service 5" );
		service5.setUriPrefix ( "http://somewhere-else.in.the.net/testemdao/service5/" );

		EntityTransaction ts = em.getTransaction ();

		ts.begin ();
		emDao.deleteEntitites ( service1.getName (), "acc1", service2.getName (), "acc2" );
		serviceDao.delete ( service1 );
		serviceDao.delete ( service2 );
		serviceDao.delete ( service3 );
		serviceDao.delete ( service4 );
		serviceDao.delete ( service5 );
		
		assertEquals ( "Entities not deleted!", 0, emDao.findMappings ( service1.getName (), "acc1" ).size () );
		assertEquals ( "Entities not deleted!", 0, emDao.findMappings ( service2.getName (), "acc2" ).size () );
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
		EntityManager em = emProvider.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();

		ts.begin ();
		emDao.storeMappingBundle ( service1.getName (), "acc10", service2.getName (), "acc11", service3.getName (), "acc12" );
		ts.commit ();
		
		EntityMappingService service = new EntityMappingService ( em );
		out.println ( service.getMappings ( true, true, true, service1.getName (), "acc10" ) );
		out.println ( service.getMappingsAs ( "xml", true, true, true, service1.getName (), "acc10" ) );
	}
}
