package uk.ac.ebi.fg.myequivalents.dao;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * Various tests for {@link EntityMappingDAO}.
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class EntityMappingDAOTest
{	
	/** Normally you cast this to {@link ManagerFactory}, here we force the specific value cause we need it and we're sure of it*/
	private DbManagerFactory managerFactory = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
	
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( managerFactory.getEntityManagerFactory () );

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
		serviceDao.store ( service1 );
		serviceDao.store ( service2 );
		serviceDao.store ( service3 );
		serviceDao.store ( service4 );
		serviceDao.store ( service5 );
		ts.commit ();
	}
	
	@After
	public void cleanUpDB ()
	{
		EntityManager em = emProvider.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		emDao.deleteEntitites ( 
			service1.getName () + ":acc1", 
			service2.getName () + ":acc2",
			service1.getName () + ":acc10",
			service2.getName () + ":acc12",
			service3.getName () + ":acc12",
			service4.getName () + ":acc1",
			service5.getName () + ":acc1"
		);
		ts.commit ();

		assertEquals ( "Entities not deleted!", 0, emDao.findMappings ( service1.getName (), "acc1" ).size () );
		assertEquals ( "Entities not deleted!", 0, emDao.findMappings ( service2.getName (), "acc2" ).size () );

		ts.begin ();
		serviceDao.delete ( service1 );
		serviceDao.delete ( service2 );
		serviceDao.delete ( service3 );
		serviceDao.delete ( service4 );
		serviceDao.delete ( service5 );
		ts.commit ();
		

		// TODO: more checks 
	}

	@Test @Ignore
	public void testStoreMapping ()
	{
		EntityManager em = emProvider.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();

		ts.begin ();
		  emDao.storeMapping ( service1.getName (), "acc1", service2.getName (), "acc2" );
		ts.commit ();
		
		List<String> mappings = emDao.findMappings ( service1.getName (), "acc1" );
		assertNotNull ( "findMappings() returns null!", mappings );
		assertEquals ( "findMappings() returns a wrong-size result!", 4, mappings.size () );

		assertTrue ( "findMappings() finds the wrong service name!", 
			service2.getName ().equals ( mappings.get ( 0 ) ) && "acc2".equals ( mappings.get ( 1 ) )
			&& service1.getName ().equals ( mappings.get ( 2 ) ) && "acc1".equals ( mappings.get ( 3 ) )
			|| service2.getName ().equals ( mappings.get ( 2 ) ) && "acc2".equals ( mappings.get ( 3 ) )
			&& service1.getName ().equals ( mappings.get ( 0 ) ) && "acc1".equals ( mappings.get ( 1 ) )
		);
	}
	
	@Test
	public void testStoreAndFind ()
	{
		EntityManager em = emProvider.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();

		ts.begin ();
		emDao.storeMappingBundle ( service1.getName () + ":acc10", service2.getName () + ":acc11", service3.getName () + ":acc12" );
		ts.commit ();
		
		List<String> mappings = emDao.findMappings ( service1.getName (), "acc10" );
		assertEquals ( "Result size is wrong (1+2+3)!", 6, mappings.size () );

		ts.begin ();
		emDao.storeMappings ( service4.getName () + ":acc1", service5.getName () + ":acc1" );
		ts.commit ();
		
		mappings = emDao.findMappings ( service5.getName (), "acc1" );
		assertEquals ( "Result size is wrong (4+5)", 4, mappings.size () );
		
		ts.begin ();
		emDao.storeMapping ( service5.getName () + ":acc1", service2.getName () + ":acc11" );
		ts.commit ();
		
		mappings = emDao.findMappings ( service2.getName (), "acc11" );
		assertEquals ( "Result size is wrong (1+2+3+4+5)", 10, mappings.size () );

		ts.begin ();
		emDao.storeMapping ( service1.getName (), "acc10", service1.getName (), "acc30" );
		ts.commit ();
		
		mappings = emDao.findMappings ( service3.getName () + ":acc12" );
		assertEquals ( "Result size is wrong (1+2+3+4+5+30)", 12, mappings.size () );
		
		ts.begin ();
		assertEquals ( "Wrong deleteEntities() result!", 2, 
			emDao.deleteEntitites ( service1.getName () + ":acc10", service5.getName () + ":acc1" ) );
		ts.commit ();
		
		mappings = emDao.findMappings ( service3.getName (), "acc12" );
		assertEquals ( "Result size is wrong (2+3+4+30)", 8, mappings.size () );
		
		ts.begin ();
		assertEquals ( "Wrong deleteMappings() result!", 
			0, emDao.deleteMappingsForAllEntitites ( service1.getName () + ":acc10", service5.getName () + ":acc1" ) );
		ts.commit ();
		
		mappings = emDao.findMappings ( service3.getName () + ":acc12" );
		assertEquals ( "Result size is wrong (2+3+4+30), after null delete", 8, mappings.size () );

		ts.begin ();
		emDao.deleteMappingsForAllEntitites ( service2.getName () + ":acc11", service3.getName () + ":acc12", service4.getName () + ":acc1" );
		ts.commit ();
		
		mappings = emDao.findMappings ( service1.getName (), "acc30" );
		assertEquals ( "Result size is wrong (final delete-all)", 0, mappings.size () );
	}
	
	@Test @Ignore
	public void testStoreAndFindEntityMapping ()
	{
		EntityManager em = emProvider.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();

		ts.begin ();
		  emDao.storeMappingBundle ( service1.getName () + ":acc10", service2.getName () + ":acc11", service3.getName () + ":acc12" );
		ts.commit ();
		
		List<EntityMapping> mappings = emDao.findEntityMappings ( service1.getName (), "acc10" );
		assertEquals ( "Result size is wrong (1+2+3)!", 3, mappings.size () );

		ts.begin ();
		  emDao.storeMappings ( service4.getName () + ":acc1", service5.getName () + ":acc1" );
		ts.commit ();
		
		mappings = emDao.findEntityMappings ( service5.getName (), "acc1" );
		assertEquals ( "Result size is wrong (4+5)", 2, mappings.size () );
		
		ts.begin ();
		  emDao.storeMapping ( service5.getName (), "acc1", service2.getName (), "acc11" );
		ts.commit ();
		
		mappings = emDao.findEntityMappings ( service2.getName (), "acc11" );
		assertEquals ( "Result size is wrong (1+2+3+4+5)", 5, mappings.size () );

		ts.begin ();
		  emDao.storeMapping ( service1.getName () + ":acc10", service1.getName () + ":acc30" );
		ts.commit ();
		
		mappings = emDao.findEntityMappings ( service3.getName () + ":acc12" );
		assertEquals ( "Result size is wrong (1+2+3+4+5+30)", 6, mappings.size () );
		
		boolean isFound = false;
		for ( EntityMapping emap: mappings ) {
			if ( emap.getService ().equals ( service4 ) && emap.getAccession ().equals ( "acc1") ) {
				assertEquals ( "Wrong recovered service (1+2+3+4+5+30)", service4.getUriPrefix (), emap.getService ().getUriPrefix () );
				isFound = true;
				break;
			}
		}
		assertTrue ( "Service not found in the result (1+2+3+4+5+30)", isFound );
		
		ts.begin ();
		assertEquals ( "Wrong deleteEntities() result!", 2, 
			emDao.deleteEntitites ( service1.getName () + ":acc10", service5.getName () + ":acc1" ) );
		ts.commit ();
		
		mappings = emDao.findEntityMappings ( service3.getName (), "acc12" );
		assertEquals ( "Result size is wrong (2+3+4+30)", 4, mappings.size () );
		
		ts.begin ();
		assertEquals ( "Wrong deleteMappings() result!", 
			0, emDao.deleteMappingsForAllEntitites ( service1.getName () + ":acc10", service5.getName () + ":acc1" ) );
		ts.commit ();
		
		mappings = emDao.findEntityMappings ( service3.getName (), "acc12" );
		assertEquals ( "Result size is wrong (2+3+4+30), after null delete", 4, mappings.size () );
		
		ts.begin ();
		emDao.deleteMappingsForAllEntitites ( service2.getName () + ":acc11", service3.getName () + ":acc12", service4.getName () + ":acc1" );
		ts.commit ();
		
		mappings = emDao.findEntityMappings ( service1.getName (), "acc30" );
		assertEquals ( "Result size is wrong (final delete-all)", 0, mappings.size () );
	}
}
