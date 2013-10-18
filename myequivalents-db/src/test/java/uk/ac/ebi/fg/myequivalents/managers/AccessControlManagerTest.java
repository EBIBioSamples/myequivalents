package uk.ac.ebi.fg.myequivalents.managers;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Apr 22, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AccessControlManagerTest
{
	private ManagerFactory mgrFactory = Resources.getInstance ().getMyEqManagerFactory ();

	private EntityManagerFactory emf = ((DbManagerFactory) mgrFactory).getEntityManagerFactory ();
	private EntityManager em = emf.createEntityManager ();

	private String adminPass = "test.password";
	private String adminSecret = User.generateSecret ();
	private User adminUser = new User ( 
		"test.admin", "Test", "Admin", adminPass, "test notes", Role.ADMIN, adminSecret 
	);
	
	private String userPass = "test.password";
	private String userSecret = User.generateSecret ();
	private User user = new User ( 
		"test.user", "Test", "User", userPass, "test notes", Role.VIEWER, userSecret );

	private String editorPass = "test.password";
	private String editorSecret = User.generateSecret ();
	private User editorUser = new User ( 
		"test.editor", "Test Editor", "User", editorPass, "test editor notes", Role.EDITOR, editorSecret );

	
	private UserDao userDao = new UserDao ( em );

	@Before
	public void init ()
	{
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.storeUnauthorized ( adminUser );
		ts.commit ();
		
		ts = em.getTransaction ();
		ts.begin ();
		userDao.login ( adminUser.getEmail (), adminPass, true );
		userDao.store ( user );
		userDao.store ( editorUser );
		ts.commit ();
	}
	
	@After
	public void shutdown ()
	{
		// Delete the regular user
		userDao = new UserDao ( em = emf.createEntityManager () );
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.login ( adminUser.getEmail (), adminPass, true );
		userDao.delete ( user.getEmail () );
		userDao.delete ( editorUser.getEmail () );
		ts.commit ();
		
		userDao = new UserDao ( em = emf.createEntityManager () );
		userDao.login ( adminUser.getEmail (), adminSecret );
		assertNull ( "User not deleted!", userDao.findByEmail ( user.getEmail () ) );

		// Delete the admin itself and now we should have clened after ourselves
		ts = em.getTransaction ();
		ts.begin ();
		userDao.deleteUnauthorized ( adminUser.getEmail () );
		ts.commit ();
		
		userDao = new UserDao ( em = emf.createEntityManager () );
		assertNull ( "Admin User not deleted!", userDao.findByEmailUnauthorized ( adminUser.getEmail () ) );
	}
	
	@Test
	public void testUserDao ()
	{
		// Must login with pass to change yourself
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.login ( adminUser.getEmail (), adminSecret );
		catchException ( userDao ).store ( user );
		assertTrue ( "User modification with API password should fail!", caughtException () instanceof SecurityException );
		ts.rollback ();

		// Was the reg user saved?
		userDao = new UserDao ( em = emf.createEntityManager () );
		userDao.login ( user.getEmail (), userSecret );
		User userDB = userDao.findByEmail ( user.getEmail () );
		assertNotNull ( "User not stored!", userDB );
		
		out.println ( "Stored user: " + userDB );

		// You can change non-critical data about yourself
		userDB.setNotes ( "Modified User Notes" );
		ts = em.getTransaction ();
		ts.begin ();
		userDao.login ( user.getEmail (), userPass, true );
		userDao.store ( userDB );
		ts.commit ();
		
		userDB.setRole ( Role.ADMIN );

		// But not stuff like role.
		ts.begin ();
		catchException ( userDao ).store ( userDB );
		assertTrue ( "Unauthorised user role modification should fail!", caughtException () instanceof SecurityException );
		
		// Unless you're an admin
		userDao.login ( adminUser.getEmail (), adminPass, true );
		userDao.setRole ( userDB.getEmail (), Role.EDITOR );
		ts.commit ();
		em.close ();


		// Reload changes see if they went fine.
		//
		userDao = new UserDao ( em = emf.createEntityManager () );
		userDao.login ( user.getEmail (), userSecret );
		userDB = userDao.findByEmail ( user.getEmail () );
		assertNotNull ( "User not stored!", userDB );
		
		out.println ( "Modified user: " + userDB );
		
		assertNotNull( "user.notes not changed!", userDB.getName () );
		assertFalse ( "User role not changed!", user.getRole ().equals ( userDB.getRole () ) );
		
		// Same for deletion
		ts = em.getTransaction ();
		ts.begin ();
		catchException ( userDao ).delete ( adminUser.getEmail () );
		assertTrue ( "Unauthorised user removal should fail!", caughtException () instanceof SecurityException );
		
		// Deletion of yourself not possible
		userDao.login ( adminUser.getEmail (), adminPass, true );
		catchException ( userDao ).delete ( adminUser.getEmail () );
		Exception caught = caughtException ();
		if ( ! ( caught instanceof SecurityException ) )
			throw new IllegalStateException ( "Error while checking failure of self-removal!", caught );
	}
	
	
	@Test
	public void testAccessControlManagerForUser ()
	{
		// Must login with pass to change yourself
		AccessControlManager accMgr = mgrFactory.newAccessControlManager ( adminUser.getEmail (), adminSecret );
		catchException ( accMgr ).storeUser ( user );
		assertTrue ( "User modification with API password should fail!", caughtException () instanceof SecurityException );

		// Was the reg user saved?
		accMgr = mgrFactory.newAccessControlManager ( user.getEmail (), userSecret );
		User userDB = accMgr.getUser ( user.getEmail () );
		assertNotNull ( "User not stored!", userDB );
		
		out.println ( "Stored user: " + userDB );

		// You can change non-critical data about yourself
		userDB.setNotes ( "Modified User Notes" );
		
		accMgr = mgrFactory.newAccessControlManagerFullAuth ( user.getEmail (), userPass );
		accMgr.storeUser ( userDB );

		// But not this!
		userDB.setRole ( Role.ADMIN );

		// But not stuff like role.
		catchException ( accMgr ).storeUser ( userDB );
		assertTrue ( "Unauthorised user role modification should fail!", caughtException () instanceof SecurityException );
		
		// Unless you're an admin
		accMgr = mgrFactory.newAccessControlManagerFullAuth ( adminUser.getEmail (), adminPass );
		accMgr.setUserRole ( userDB.getEmail (), Role.EDITOR );

		// Reload changes see if they went fine.
		//
		accMgr = mgrFactory.newAccessControlManager ( user.getEmail (), userSecret );
		
		userDB = accMgr.getUser ( user.getEmail () );
		assertNotNull ( "User not stored!", userDB );
		
		out.println ( "Modified user: " + userDB );
		
		assertNotNull( "user.notes not changed!", userDB.getName () );
		assertFalse ( "User role not changed!", user.getRole ().equals ( userDB.getRole () ) );
		
		// Same for deletion
		catchException ( accMgr ).deleteUser ( adminUser.getEmail () );
		assertTrue ( "Unauthorised user removal should fail!", caughtException () instanceof SecurityException );
		
		// Deletion of yourself not possible
		accMgr = mgrFactory.newAccessControlManagerFullAuth ( adminUser.getEmail (), adminPass  );
		catchException ( accMgr ).deleteUser ( adminUser.getEmail () );
		Exception caught = caughtException ();
		if ( ! ( caught instanceof SecurityException ) )
			throw new IllegalStateException ( "Error while checking failure of self-removal!", caught );
	}
	
	
	
	@Test
	public void testPermssionCommands ()
	{
		Service service = new Service ( "test.perms.service1", "someType", "A Test Service", "The Description of a Test Service" );
		ServiceManager servMgr = mgrFactory.newServiceManager ( editorUser.getEmail (), editorSecret );
		servMgr.storeServices ( service );
		
		EntityMappingManager emMgr = mgrFactory.newEntityMappingManager ( editorUser.getEmail (), editorSecret );
		emMgr.storeMappings ( service.getName () + ":e1", service.getName () + ":e2" );
		
		AccessControlManager accMgr = mgrFactory.newAccessControlManagerFullAuth ( adminUser.getEmail (), adminPass );
		accMgr.setUserRole ( user.getEmail (), User.Role.EDITOR );
		
		Date testDate = new DateMidnight ( 2013, 4, 25 ).toDate ();
		accMgr.setAuthenticationCredentials ( editorUser.getEmail (), editorSecret );
		accMgr.setServicesVisibility ( "false", DateJaxbXmlAdapter.STR2DATE.marshal ( testDate ), true, service.getName () );

		ServiceDAO servDao = new ServiceDAO ( ((DbManagerFactory) mgrFactory ).getEntityManagerFactory ().createEntityManager () );
		Service serviceDB = servDao.findByName ( service.getName () );
		
		assertNull ( "ServiceDAO returns a private service!", serviceDB );
		serviceDB = servDao.findByName ( service.getName (), false );
		
		out.println ( "Reloaded service:" );
		out.println ( serviceDB );
		
		assertFalse ( "Public Flag not stored!", serviceDB.getPublicFlag () );
		assertEquals ( "Release date not stored!", testDate, serviceDB.getReleaseDate () );
		assertFalse ( "The service should be private!", serviceDB.isPublic () );

		Entity ent = emMgr.getMappings ( false, service.getName () + ":e1" ).getBundles ().iterator ().next ().getEntities ().iterator ().next ();
		assertFalse ( "setServicesVisibility() wasn't cascaded!", ent.getPublicFlag () );
		assertEquals ( "setServicesVisibility() wasn't cascaded!", testDate, ent.getReleaseDate () );
		
		servMgr.deleteServices ( service.getName () );
	}
	
	
	@Test
	public void testServicePermissionCascading ()
	{
		Repository repo = new Repository ( "test.perms.repo1", "A test repo 1", "Descr about A test Repo 1" );
		repo.setPublicFlag ( true );
		Service service = new Service ( "test.perms.service1", "someType", "A Test Service", "The Description of a Test Service" );
		service.setPublicFlag ( null );
		service.setReleaseDate ( null );
		service.setRepository ( repo );
		
		ServiceManager servMgr = mgrFactory.newServiceManager ( editorUser.getEmail (), editorSecret );
		servMgr.storeServices ( service );

		ServiceDAO servDao = new ServiceDAO ( ((DbManagerFactory) mgrFactory ).getEntityManagerFactory ().createEntityManager () );
		Service serviceDB = servDao.findByName ( service.getName () );

		assertNotNull ( "ServiceDAO doesn't return a cascade-public service!", serviceDB );
		assertTrue ( "serviceDB.isPublic() is not true!", serviceDB.isPublic () );
	}
	
}
