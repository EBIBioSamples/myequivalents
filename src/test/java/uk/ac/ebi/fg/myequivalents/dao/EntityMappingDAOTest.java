package uk.ac.ebi.fg.myequivalents.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.test.TestEntityMgrFactoryProvider;
import uk.ac.ebi.fg.myequivalents.test.TestEntityMgrProvider;

import static junit.framework.Assert.*;

public class EntityMappingDAOTest
{
	@ClassRule
	public static TestEntityMgrFactoryProvider emfProvider = new TestEntityMgrFactoryProvider ();
	
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( emfProvider.getEntityManagerFactory () );

	private EntityMappingDAO emDao;
	private ServiceDAO serviceDao;
	
	@Before
	public void initDAO ()
	{
		EntityManager em = emProvider.getEntityManager ();
		serviceDao = new ServiceDAO ( em );
		emDao = new EntityMappingDAO ( em );
	}

	@Test
	public void testStoreMapping ()
	{
		Service service1 = new Service ( "test.testemdao.service1", "testemdao.someType1", "A Test Service 1", "The Description of a Test Service 1" );
		service1.setUriPrefix ( "http://somewhere.in.the.net/testemdao/service1/" );

		Service service2 = new Service ( "test.testemdao.service2", "testemdao.someType1", "A Test Service 2", "The Description of a Test Service 2" );
		service1.setUriPrefix ( "http://somewhere.in.the.net/testemdao/service2/" );
		
		EntityManager em = emProvider.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();

		ts.begin ();
		emDao.deleteEntitites ( service1.getName (), "acc1", service2.getName (), "acc2" );
		serviceDao.delete ( service1 );
		serviceDao.delete ( service2 );
		assertEquals ( "Entities not deleted!", 0, emDao.findMappings ( service1.getName (), "acc1" ).size () );
		assertEquals ( "Entities not deleted!", 0, emDao.findMappings ( service2.getName (), "acc2" ).size () );
		
		serviceDao.store ( service1 );
		serviceDao.store ( service2 );
		em.flush ();
		emDao.storeMapping ( service1.getName (), "acc1", service2.getName (), "acc2" );
		ts.commit ();
		
		List<String> mappings = emDao.findMappings ( service1.getName (), "acc1" );
		assertNotNull ( "findMappings() returns null!", mappings );
		assertEquals ( "findMappings() returns a wrong-size result!", 2, mappings.size () );

		assertEquals ( "findMappings() finds the wrong service name!", service2.getName (), mappings.get ( 0 ) );
		assertEquals ( "findMappings() finds the wrong service name!", "acc2", mappings.get ( 1 ) );
	}
}
