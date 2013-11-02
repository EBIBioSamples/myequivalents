package uk.ac.ebi.fg.myequivalents.webservices.client;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.joda.time.DateMidnight;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter;


/**
 * TODO: Comment me!
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
	//static final String WS_BASE_URL = "http://localhost:8080/ws";
	
	// TODO Make them final and upper case throughout all the code base
	private static String adminPass = "test.password";
	private static String adminSecret = User.generateSecret ();
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
	
	@Test @Ignore ( "not ready yet (TODO)" )
	public void testPermssionCommands ()
	{
		Service service = new Service ( "test.perms.service1", "someType", "A Test Service", "The Description of a Test Service" );
		// TODO: store via the service manager
		
		EntityMappingManager emMgr = new EntityMappingWSClient ( WS_BASE_URL );
		emMgr.setAuthenticationCredentials ( EDITOR_USER.getEmail (), EDITOR_SECRET );
		emMgr.storeMappings ( service.getName () + ":e1", service.getName () + ":e2" );
		
		accMgr.setAuthenticationCredentials ( adminUser.getEmail (), adminPass );
		accMgr.setUserRole ( user.getEmail (), User.Role.EDITOR );
		
		Date testDate = new DateMidnight ( 2013, 4, 25 ).toDate ();
		accMgr.setAuthenticationCredentials ( EDITOR_USER.getEmail (), EDITOR_SECRET );
		accMgr.setServicesVisibility ( "false", DateJaxbXmlAdapter.STR2DATE.marshal ( testDate ), true, service.getName () );

		ServiceManager servMgr = new ServiceWSClient ( WS_BASE_URL );
		servMgr.setAuthenticationCredentials ( EDITOR_USER.getEmail (), EDITOR_SECRET );
		
		Service serviceDB = servMgr.getServices ( service.getName () ).getServices ().iterator ().next ();
		
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
	
	
	@Test @Ignore ( "not ready yet (TODO)" )
	public void testServicePermissionCascading ()
	{
		Repository repo = new Repository ( "test.perms.repo1", "A test repo 1", "Descr about A test Repo 1" );
		repo.setPublicFlag ( true );
		Service service = new Service ( "test.perms.service1", "someType", "A Test Service", "The Description of a Test Service" );
		service.setPublicFlag ( null );
		service.setReleaseDate ( null );
		service.setRepository ( repo );
		
		// TODO ServiceManager servMgr = mgrFactory.newServiceManager ( EDITOR_USER.getEmail (), EDITOR_SECRET );
		ServiceManager servMgr = new ServiceWSClient ( WS_BASE_URL );
		servMgr.setAuthenticationCredentials ( EDITOR_USER.getEmail (), EDITOR_SECRET );

		servMgr.storeServices ( service );

		Service serviceDB = servMgr.getServices ( service.getName () ).getServices ().iterator ().next ();
		
		assertNotNull ( "ServiceDAO doesn't return a cascade-public service!", serviceDB );
		assertTrue ( "serviceDB.isPublic() is not true!", serviceDB.isPublic () );
	}	
	
}
