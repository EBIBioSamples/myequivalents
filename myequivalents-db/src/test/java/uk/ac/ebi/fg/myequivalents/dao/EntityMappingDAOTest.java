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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;
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
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Before
	public void init ()
	{
		EntityManager em = emProvider.getEntityManager ();
		serviceDao = new ServiceDAO ( em );
		emDao = new EntityMappingDAO ( em );
		
		service1 = new Service ( "test.testemdao.service1", "testemdao.someType1", "A Test Service 1", "The Description of a Test Service 1" );
		service1.setUriPattern ( "http://test.testemdao.com/service1/$id" );
		service2 = new Service ( "test.testemdao.service2", "testemdao.someType1", "A Test Service 2", "The Description of a Test Service 2" );
		service2.setUriPattern ( "http://test.testemdao.com/service2/$id" );
		service3 = new Service ( "test.testemdao.service3", "testemdao.someType2", "A Test Service 3", "The Description of a Test Service 3" );
		service4 = new Service ( "test.testemdao.service4", "testemdao.someType2", "A Test Service 4", "The Description of a Test Service 4" );
		service5 = new Service ( "test.testemdao.service5", "testemdao.someType2", "A Test Service 5", "The Description of a Test Service 5" );

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
	
	@Test
	public void testUris ()
	{
		EntityManager em = emProvider.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();

		ts.begin ();
		  emDao.storeMapping ( 
		  	"<" + EntityIdResolver.buildUriFromAcc ( "acc1", service1.getUriPattern () ) + ">",
		  	"<" + EntityIdResolver.buildUriFromAcc ( "acc2", service2.getUriPattern () ) + ">"
		  );
		ts.commit ();
		
		// Find by acc
		List<String> mappings = emDao.findMappings ( service1.getName (), "acc1" );
		assertNotNull ( "findMappings() returns null!", mappings );
		assertEquals ( "findMappings() returns a wrong-size result!", 4, mappings.size () );

		// And by URI
		mappings = emDao.findMappings ( "<" + EntityIdResolver.buildUriFromAcc ( "acc2", service2.getUriPattern () ) + ">" );
		assertNotNull ( "findMappings() returns null!", mappings );
		log.info ( "findMappings() result: {}", mappings.toString () );
		assertEquals ( "findMappings() returns a wrong-size result!", 4, mappings.size () );
		
		
		// Clean up
		ts.begin ();
		emDao.deleteMappings ( "<" + EntityIdResolver.buildUriFromAcc ( "acc1", service1.getUriPattern () ) + ">" );
		ts.commit ();
		
		mappings = emDao.findMappings ( "<" + EntityIdResolver.buildUriFromAcc ( "acc2", service2.getUriPattern () ) + ">" );
		assertEquals ( "Clean-up diddn't work!", 0, mappings.size () );
	}
	
	@Test
	public void testUriWithUnspecifiedService ()
	{
		String acc1 = "acc1", testUri1 = "http://test1.unknown.uri.pattern/foo-service/" + acc1;
		String acc2 = "acc2", testUri2 = "http://test2.unknown.uri.pattern/foo-service#" + acc2;
		String acc3 = "acc3", testUri3 = "http://test2.unknown.uri.pattern/foo-service#" + acc3;
		
		EntityManager em = emProvider.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		this.serviceDao.store ( Service.UNSPECIFIED_SERVICE );
		ts.commit ();

		ts.begin ();
		emDao.storeMappingBundle ( 
			":<" + testUri1 + ">", // Universal service selected explicitly
			"<" + EntityIdResolver.buildUriFromAcc ( "acc1", service1.getUriPattern () ) + ">",
			"<" + testUri2 + ">" // Universal service expected to be a fall-back case
		);
		ts.commit ();
		
		List<EntityMapping> maps = emDao.findEntityMappings ( ":<" + testUri1 + ">" );
		assertNotNull ( "findEntityMappings() returns null!", maps );
		log.info ( "findMappings() result: {}", maps.toString () );
		assertEquals ( "findEntityMappings() returns a wrong-size result!", 3, maps.size () );
		
		boolean found = false;
		for ( EntityMapping map: maps )
			if ( map.getService ().equals ( Service.UNSPECIFIED_SERVICE ) && map.getAccession ().equals ( map.getURI () ) ) { 
				found = true; break; 
		}
		
		assertTrue ( "Unspecified service-based mapping not found!", found );
		
		// Add up another URI
		ts.begin ();
		emDao.storeMapping ( ":<" + testUri3 + ">", "<" + testUri2 + ">" );
		ts.commit ();
		
		maps = emDao.findEntityMappings ( service1.getName () + ":" + acc1 );
		assertNotNull ( "findEntityMappings() returns null, after addition!", maps );
		log.info ( "findMappings() result: {}", maps.toString () );
		assertEquals ( "findEntityMappings() returns a wrong-size result, after addition!", 4, maps.size () );
		
		// Clean up
		ts.begin ();
		emDao.deleteMappings ( ":<" + testUri1 + ">" );
		ts.commit ();
		
		maps = emDao.findEntityMappings ( service1.getName () + ":" + acc1 );
		assertEquals ( "Clean-up diddn't work!", 0, maps.size () );
	}

}
