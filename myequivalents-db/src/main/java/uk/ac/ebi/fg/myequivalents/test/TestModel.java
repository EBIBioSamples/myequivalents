package uk.ac.ebi.fg.myequivalents.test;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.RepositoryDAO;
import uk.ac.ebi.fg.myequivalents.dao.ServiceCollectionDAO;
import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Feb 2016</dd></dl>
 *
 */
public class TestModel
{
	public String editorPass = "test.password";
	public String editorSecret = User.generateSecret ();
	public User editorUser;

	public Service service1, service2, service3, service4, service5;
	public ServiceCollection sc1;
	public Repository repo1;
	
	public EntityMapping b11;
	public EntityMapping b12;
	public EntityMapping b13;
	public EntityMapping b14;
	public EntityMapping b21;
	public EntityMapping b22;
	
	Map<String, Bundle> bundlesMap = new HashMap<> ();
	public EntityMappingSearchResult mappings;

	public TestModel ()
	{
		this ( "test.", "http://test.somewhere.net" );
	}

	public TestModel ( String prefix, String uriPrefix )
	{
		editorUser = new User ( 
			prefix + "editor", "Test Editor", "User", editorPass, "test editor notes", Role.EDITOR, editorSecret
		);
		
		service1 = new Service ( prefix + "service1", prefix + "someType1", "A Test Service 1", "The Description of a Test Service 1" );
		service1.setUriPattern ( uriPrefix + "service1#$id" );
				
		sc1 = new ServiceCollection ( 
			prefix + "serviceColl1", service1.getEntityType (), "Test Service Collection 1", "The Description of the SC 1" 
		);
		service1.setServiceCollection ( sc1 );
		
		repo1 = new Repository ( prefix + "repo1", "Test Repo 1", "The Description of Repo1" );
		service1.setRepository ( repo1 );

		service2 = new Service ( prefix + "service2", prefix + "someType1", "A Test Service 2", "The Description of a Test Service 2" );
		service2.setUriPattern ( uriPrefix + "service2#$id" );
		
		service3 = new Service ( prefix + "service3", prefix + "someType2", "A Test Service 3", "The Description of a Test Service 3" );
		service4 = new Service ( prefix + "service4", prefix + "someType2", "A Test Service 4", "The Description of a Test Service 4" );
		service5 = new Service ( prefix + "service5", prefix + "someType2", "A Test Service 5", "The Description of a Test Service 5" );
		
		mappings = new EntityMappingSearchResult ( false );
		
		mappings.addEntityMapping ( b11 = new EntityMapping ( service1, "b1.1", "b1" ) );
		mappings.addEntityMapping ( b12 = new EntityMapping ( service2, "b1.2", "b1" ) );
		mappings.addEntityMapping ( b13 = new EntityMapping ( service1, "b1.3", "b1" ) );
		mappings.addEntityMapping ( b14 = new EntityMapping ( service1, "b1.4", "b1" ) );

		mappings.addEntityMapping ( b21 = new EntityMapping ( service2, "b2.1", "b2" ) );
		mappings.addEntityMapping ( b22 = new EntityMapping ( service3, "b2.2", "b2" ) );
		
		Map<String, Bundle> bundlesMap = new HashMap<> ();
		
		for ( Bundle b: mappings.getBundles () )
		{
			String bid = b.getEntities ().iterator ().next ().getAccession ();
			bid = bid.replaceAll ( "\\..*", "" );
			bundlesMap.put ( bid, b );
		}
	}
	
	public void store ()
	{
		DbManagerFactory managerFactory = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		EntityManagerFactory emf = managerFactory.getEntityManagerFactory ();
		EntityManager em = emf.createEntityManager ();

		// An editor is needed for writing operations.
		UserDao userDao = new UserDao ( em );
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.storeUnauthorized ( editorUser );
		ts.commit ();

		// The services
		ServiceDAO serviceDao = new ServiceDAO ( em );
		
		ts = em.getTransaction ();
		ts.begin ();
		serviceDao.store ( service1 );
		serviceDao.store ( service2 );
		serviceDao.store ( service3 );
		serviceDao.store ( service4 );
		serviceDao.store ( service5 );
		ts.commit ();
		
		// The mappings
		// This is how you should obtain a manager from a factory
		EntityMappingManager emMgr = managerFactory.newEntityMappingManager ( editorUser.getEmail (), editorSecret );
		emMgr.storeMappingBundles ( mappings );
	}
	
	
	public void unload ()
	{
		DbManagerFactory managerFactory = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		EntityManagerFactory emf = managerFactory.getEntityManagerFactory ();
		EntityManager em = emf.createEntityManager ();

		// The mappings
		EntityMappingManager emMgr = managerFactory.newEntityMappingManager ( editorUser.getEmail (), editorSecret );
		for ( Bundle b: mappings.getBundles () ) 
		{
			Entity e = b.getEntities ().iterator ().next ();
			emMgr.deleteMappings ( e.getServiceName () + ":" + e.getAccession () );
		}

		// The upper items
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();

		ServiceDAO serviceDao = new ServiceDAO ( em );
		for ( Service s: mappings.getServices () )
			serviceDao.delete ( s );
		
		new RepositoryDAO ( em ).delete ( repo1 );
		new ServiceCollectionDAO ( em ).delete ( sc1 );
		
		// The users
		UserDao userDao = new UserDao ( em );
		
		userDao.deleteUnauthorized ( editorUser.getEmail () );
		ts.commit ();
	} 
}
