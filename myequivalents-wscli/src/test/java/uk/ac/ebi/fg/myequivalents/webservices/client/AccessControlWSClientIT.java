package uk.ac.ebi.fg.myequivalents.webservices.client;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.joda.time.DateMidnight;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter;


/**
 * Integration tests for the web service client concerning the access control features, i.e. the 
 * {@link AccessControlManager} interface. Most of these tests are almost the same as the ones available in 
 * uk.ac.ebi.fg.myequivalents.managers.AccessControlManagerTest in the myequivalents-db package.
 *
 * <dl><dt>date</dt><dd>24 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AccessControlWSClientIT
{
	// Default is http://localhost:8080/myequivalents/ws
	// We use a non-standard port here cause 8080 is often already taken on EBI hosts
	//
  static final String WS_BASE_URL = "http://localhost:10973/ws";
	// static final String WS_BASE_URL = "http://localhost:8080/ws";
	
	// TODO Make them final and upper case throughout all the code base
	private static String adminPass = "test.password";
	private static String adminSecret = "test.secret";
	private static User adminUser = new User ( 
		"test.admin", "Test", "Admin", adminPass, "test notes", Role.ADMIN, adminSecret 
	);
	
	private String userPass = "test.password";
	private String userSecret = "test.secret";
	private User user = new User ( 
		"test.user", "Test", "User", userPass, "test notes", Role.VIEWER, userSecret );

	static final String EDITOR_PASS = "test.password";
	static final String EDITOR_SECRET = "test.secret";
	static final User EDITOR_USER = new User ( 
		"test.editor", "Test Editor", "User", EDITOR_PASS, "test editor notes", Role.EDITOR, EDITOR_SECRET );

	
	private AccessControlManager accMgr = new AccessControlWSClient ( WS_BASE_URL );

	/**
	 * Test users access features. 
	 */
	@Test
	public void testUserCommands ()
	{
		// Must login with pass to change these things
		accMgr.setAuthenticationCredentials ( adminUser.getEmail (), adminSecret );
		catchException ( accMgr ).storeUser ( user );
		Exception caught = caughtException ();
		if ( ! ( caught instanceof SecurityException ) ) throw new IllegalStateException ( 
			"User modification with API password should fail!" 
		);

		accMgr.setFullAuthenticationCredentials ( adminUser.getEmail (), adminPass );
		accMgr.storeUser ( user );
		
		// Was the reg user saved?
		accMgr.setAuthenticationCredentials ( user.getEmail (), userSecret );
		User userDB = accMgr.getUser ( user.getEmail () );
		assertNotNull ( "User not stored!", userDB );
		
		out.println ( "Stored user: " + userDB );

		// You can change non-critical data about yourself
		userDB.setNotes ( "Modified User Notes" );
		
		accMgr.setFullAuthenticationCredentials ( user.getEmail (), userPass );
		accMgr.storeUser ( userDB );

		// But not this!
		userDB.setRole ( Role.ADMIN );

		// But not stuff like role.
		catchException ( accMgr ).storeUser ( userDB );
		if ( ! ( (caught = caughtException ()) instanceof SecurityException ) ) throw new IllegalStateException ( 
			"Unauthorised user role modification should fail!"
		);
		
		// Unless you're an admin
		accMgr.setFullAuthenticationCredentials ( adminUser.getEmail (), adminPass );
		accMgr.setUserRole ( userDB.getEmail (), Role.EDITOR );

		// Reload changes see if they went fine.
		//
		accMgr.setAuthenticationCredentials ( user.getEmail (), userSecret );
		
		userDB = accMgr.getUser ( user.getEmail () );
		assertNotNull ( "User not stored!", userDB );
		
		out.println ( "Modified user: " + userDB );
		
		assertNotNull( "user.notes not changed!", userDB.getName () );
		assertFalse ( "User role not changed!", user.getRole ().equals ( userDB.getRole () ) );
		
		// Same for deletion
		catchException ( accMgr ).deleteUser ( adminUser.getEmail () );
		if ( ! ( (caught = caughtException ()) instanceof SecurityException ) ) throw new IllegalStateException ( 
			"Unauthorised user removal should fail!"
		);
		
		// Deletion of yourself not possible
		accMgr.setFullAuthenticationCredentials ( adminUser.getEmail (), adminPass  );
		catchException ( accMgr ).deleteUser ( adminUser.getEmail () );
		if ( ! ( ( caught = caughtException () ) instanceof SecurityException ) )
			throw new IllegalStateException ( "Error while checking failure of self-removal!", caught );
	}
	
	/**
	 * Test commands related to visibility permissions.
	 */
	@Test
	public void testPermssionCommands ()
	{
		Service service = new Service ( "test.perms.service1", "someType", "A Test Service", "The Description of a Test Service" );

		ServiceManager servMgr = new ServiceWSClient ( WS_BASE_URL );
		servMgr.setAuthenticationCredentials ( EDITOR_USER.getEmail (), EDITOR_SECRET );
		
		servMgr.storeServices ( service );
		
		EntityMappingManager emMgr = new EntityMappingWSClient ( WS_BASE_URL );
		emMgr.setAuthenticationCredentials ( EDITOR_USER.getEmail (), EDITOR_SECRET );
		emMgr.storeMappings ( service.getName () + ":e1", service.getName () + ":e2" );
		
		accMgr.setFullAuthenticationCredentials ( adminUser.getEmail (), adminPass );
		accMgr.storeUser ( user );
		accMgr.setUserRole ( user.getEmail (), User.Role.EDITOR );
		
		user = accMgr.getUser ( user.getEmail () );
		assertEquals ( "User role not changed!", Role.EDITOR, user.getRole () );
		
		accMgr.setAuthenticationCredentials ( adminUser.getEmail (), adminSecret );
		Date testDate = new DateMidnight ( 2013, 4, 25 ).toDate ();
		accMgr.setServicesVisibility ( "false", DateJaxbXmlAdapter.STR2DATE.marshal ( testDate ), true, service.getName () );
		
		Service serviceDB = servMgr.getServices ( service.getName () ).getServices ().iterator ().next ();
		
		out.println ( "Reloaded service:" );
		out.println ( serviceDB );
		
		assertFalse ( "Public Flag not stored!", serviceDB.getPublicFlag () );
		assertEquals ( "Release date not stored!", testDate, serviceDB.getReleaseDate () );
		assertFalse ( "The service should be private!", serviceDB.isPublic () );

		Entity ent = emMgr.getMappings ( false, service.getName () + ":e1" ).getBundles ().iterator ().next ().getEntities ().iterator ().next ();
		assertFalse ( "setServicesVisibility() wasn't cascaded!", ent.getPublicFlag () );
		assertEquals ( "setServicesVisibility() wasn't cascaded!", testDate, ent.getReleaseDate () );
		
		emMgr.deleteEntities ( service.getName () + ":e1", service.getName () + ":e2" );
		servMgr.deleteServices ( service.getName () );
	}
	
	/**
	 * Test the cascading of a permission change command, e.g., when a permission is changed on a service and this is 
	 * supposed to be cascaded to all its entities.
	 */
	@Test
	public void testServicePermissionCascading ()
	{
		Repository repo = new Repository ( "test.perms.repo1", "A test repo 1", "Descr about A test Repo 1" );
		repo.setPublicFlag ( true );
		Service service = new Service ( "test.perms.service1", "someType", "A Test Service", "The Description of a Test Service" );
		service.setPublicFlag ( null );
		service.setReleaseDate ( null );
		service.setRepository ( repo );
		
		ServiceManager servMgr = new ServiceWSClient ( WS_BASE_URL );
		servMgr.setAuthenticationCredentials ( EDITOR_USER.getEmail (), EDITOR_SECRET );

		servMgr.storeServices ( service );

		ServiceSearchResult sr = servMgr.getServices ( service.getName () );
		Service serviceDB = sr.getServices ().iterator ().next ();
		Repository repoDB = serviceDB.getRepository ();
		
		assertNotNull ( "ServiceMgr doesn't return a cascade-public service!", serviceDB );
		assertNotNull ( "ServiceMgr doesn't return the service's repo!", repoDB );
		assertTrue ( "serviceDB.isPublic() is not true!", serviceDB.isPublic () );
	}	
	
}
