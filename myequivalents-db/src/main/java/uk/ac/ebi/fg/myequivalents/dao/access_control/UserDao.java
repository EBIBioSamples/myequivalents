package uk.ac.ebi.fg.myequivalents.dao.access_control;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;

/**
 * The DAO to manage {@link User} objects.
 * 
 * TODO: Make it thread-safe.
 *
 * <dl><dt>date</dt><dd>Mar 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UserDao
{
	private EntityManager entityManager;
	private User loggedInUser = null;
	private boolean loggedInViaAPI;

	/**
	 * Creates a user DAO and logins the (admin) user that will use it, by calling {@link #login(String, String, boolean)}.
	 */
	public UserDao ( EntityManager entityManager, String authEmail, String password, boolean isUserPassword )
	{
		super ();
		this.entityManager = entityManager;
		if ( authEmail != null ) login ( authEmail, password, isUserPassword );
	}

	/** 
	 * Wraps {@link #UserDao(EntityManager, String, String, boolean)} with isUserPassword = false (i.e., uses the API).
	 */
	public UserDao ( EntityManager entityManager, String authEmail, String apiPassword )
	{
		this ( entityManager, authEmail, apiPassword, false );
	}
	
	/**
	 * Creates a new DAO, which you need to login in, using {@link #login(String, String)}.
	 */
	public UserDao ( EntityManager entityManager ) {
		this ( entityManager, null, null );
	}
	
	
	/**
	 * Logins a user to allow it to use the DAO. This has to be called by either the DAO constructor or before any 
	 * administration operation occur. If isUserPassword is true, checks against {@link User#getPassword()}, else
	 * it uses {@link User#getApiPassword()}.
	 * 
	 * TODO: return ExposedUser
	 */
	public User login ( String email, String password, boolean isUserPassword ) 
	{
		email = StringUtils.trimToNull ( email );
		password = StringUtils.trimToNull ( password );
		
		if ( email == null || "[anonymous]".equalsIgnoreCase ( email ) )
		{
			this.loggedInViaAPI = true;
			return loggedInUser = new User ( 
				"[anonymous]", "Anonymous", "User", null, "The fictitious/unauthenticated user", Role.VIEWER, null );
		}
		
		if ( password == null ) throw new SecurityException ( 
			"Must provide a password to authenticate as '" + email + "'" 
		);
		
		User user = findByEmailUnauthorized ( email );
		if ( user == null ) throw new SecurityException ( "User '" + email + "' not found" );
		
		String passToCheck = isUserPassword ? user.getPassword () : user.getApiPassword ();
		
		if ( !User.hashPassword ( password ).equals ( passToCheck ) ) throw new SecurityException ( 
			"Wrong password for user '" + email + "', '" + user.getName () + " " + user.getSurname () + "'" );
		
		this.loggedInViaAPI = !isUserPassword;
		// It must not come back with permissions changed, so we return a clone.
		loggedInUser = new User ( user );
		loggedInUser.setPassword ( null );
		loggedInUser.setApiPassword ( null );
		return loggedInUser;
	}

	/**
	 * Authenticate with the {@link User#getApiPassword() API password}.
	 */
	public User login ( String email, String apiPassword )
	{
		return login ( email, apiPassword, false );
	}

	/**
	 * Checks that the {@link #getLoggedInUser() current logged} user has at least a given role. 
	 * A {@link SecurityException} is thrown in case not.
	 */
	public void enforceRole ( Role role, boolean needsFullAuthentication ) 
	{
		if ( loggedInUser == null ) login ( null, null ); // As anonymous
		
		// Just in case anonymous authentication didn't work for some reason.
		if ( loggedInUser == null ) 
			throw new SecurityException ( 
				"Security violation: the operation requires an authenticated user" 
			);
		if ( !loggedInUser.hasPowerOf ( role ) ) 
			throw new SecurityException ( String.format (
				"Security violation: the operation requires the %s access level and the user '%s' has only %s", 
				role, loggedInUser.getEmail (), loggedInUser.getRole ()
			));
		if ( needsFullAuthentication && isLoggedInViaAPI () )
			throw new SecurityException ( String.format (
				"Security violation: the operation requires the user '%s' to login via user password, not just the API password", 
				loggedInUser.getEmail ()
			));
	}

	/**
	 * Wraps {@link #enforceRole(Role, boolean) enforceRole ( role, false )}, i.e., requires authentication with 
	 * API password.
	 * 
	 */
	public void enforceRole ( Role role ) {
		enforceRole ( role, false );
	}

	
	/**
	 * Log off the currently logged user 
	 */
	public void logOff () {
		this.loggedInUser = null;
	}
	
	/**
	 * @return the current user that has logged in via {@link #login(String, String)}.  
	 * TODO: exposedUser 
	 */
	public User getLoggedInUser () {
		return this.loggedInUser;
	}
	
	/** 
	 * True if the current {@link #getLoggedInUser()} has authenticated using the password API, i.e., invoking 
	 * {@link #login(String, String) login ( mail, api-pass )}
	 * @return
	 */
	public boolean isLoggedInViaAPI () {
		return loggedInViaAPI;
	}

	/**
	 * Searches for a user, without any advance access-right control.
	 * This should be used for testing purposes only, or in situations like DB initialisation. 
	 */
	public User findByEmailUnauthorized ( String email )
	{
		email = StringUtils.trimToNull ( email );
		if ( email == null ) return null;
		
		Query q = entityManager.createQuery ( "FROM " + User.class.getName () + " WHERE lower(email) = '" + email + "'" );
		@SuppressWarnings ( "unchecked" )
		List<User> results = q.getResultList ();
		
		return results.isEmpty () ? null : results.iterator ().next ();
	}
	
	/**
	 * Find a user, but only after having checked that the current {@link #getLoggedInUser() logged user} has proper 
	 * rights to see the results, i.e., that it is at least a {@link Role#VIEWER} if the searched user is the same as
	 * {@link #getLoggedInUser()}, or {@link Role#ADMIN}.
	 */
	public User findByEmail ( String email )
	{
		email = StringUtils.trimToNull ( email );
		if ( email == null ) return null;
		
		enforceRole ( Role.VIEWER );
		
		if ( !loggedInUser.hasPowerOf ( Role.ADMIN ) ) 
		{
			// If you're not an admin, you can only know about yourself and we already have yourself
		  if ( !loggedInUser.getEmail ().toLowerCase ().equals ( email.toLowerCase () ) ) 
		  	throw new SecurityException ( "Security Violation for getUser( '" + email + "' ): you can only access your own user" );
	  	return loggedInUser;
		}
		
		User result = findByEmailUnauthorized ( email );
		
		if ( result != null ) {
			result.setApiPassword ( null );
			result.setPassword ( null );
		}
		
		return result;
	} 

	/**
	 * Changes the role of a user, after having checked that the current {@link #getLoggedInUser() logged user} is an ADMIN.
	 */
	public void setRole ( String email, Role role )
	{
		email = StringUtils.trimToNull ( email );
		Validate.notNull ( email, "Error for changeRole(): email cannot be null" );
		Validate.notNull ( role, String.format ( "Error for changeRole( '%s' ): role cannot be null", email ));
		enforceRole ( Role.ADMIN, true );

		User user = findByEmailUnauthorized ( email );
		if ( user.getRole () == role ) return;
		
		user.setRole ( role );
		entityManager.merge ( user );
	}

	/**
	 * Store a new user or, depending on {@link User#getName()}, saves changes to an existing one. Allows you to do that
	 * only if the current {@link #getLoggedInUser() logged user} is an ADMIN.
	 */
	private void store ( User user, boolean mustBeAutorized )
	{
		Validate.notNull ( user, "Cannot update a null object" );
		
		if ( mustBeAutorized )
			enforceRole ( Role.VIEWER, true );
		
		String 
			pwd = StringUtils.trimToNull ( user.getPassword () ),
			apiPwd = StringUtils.trimToNull ( user.getApiPassword () ); 
			
		User userDB = findByEmailUnauthorized ( user.getEmail () );

		if ( userDB == null ) 
		{
			if ( mustBeAutorized && !loggedInUser.hasPowerOf ( Role.ADMIN ) ) 
				throw new SecurityException ( String.format ( 
					"store ( '%s' ): this is a new user and you must be an admin to do that", user.getEmail ()
				));

			if ( pwd == null ) throw new SecurityException ( 
				"Cannot accept to save a new user with null user password"
			);
						
			if ( apiPwd == null ) throw new SecurityException ( 
				"Cannot accept to save a new user with null API secret"
			);
			
			user.setPassword ( User.hashPassword ( pwd ) );
			user.setApiPassword ( User.hashPassword ( apiPwd ) );
			
			entityManager.merge ( user );
			return;
		}
		
		if ( mustBeAutorized && !loggedInUser.hasPowerOf ( Role.ADMIN ) && !userDB.getEmail ().equals ( user.getEmail () ) ) 
			throw new SecurityException ( 
				"Security Violation for getUser( '" + user.getEmail () + "' ): you can only change your own user" 
		);

		user.setPassword ( pwd == null ? userDB.getPassword () : User.hashPassword ( pwd ) );
		user.setApiPassword ( apiPwd == null ? userDB.getApiPassword () : User.hashPassword ( apiPwd ));
				
		if ( mustBeAutorized )
		{
			Role newRole = user.getRole ();
			if ( newRole != null && !newRole.equals ( userDB.getRole () ) && !getLoggedInUser ().hasPowerOf ( Role.ADMIN ) ) 
				throw new SecurityException ( String.format ( 
					"store ( '%s' ): You must be an admin to change your role, ", user.getEmail ()
			));
		}

		entityManager.merge ( user );
	}
	
	public void store ( User user ) {
		store ( user, true );
	}
	
	/**
	 * Like {@link #store(User)}, but without any check on the current logged user. This should be used only for test
	 * purposes or in situations like DB configuration.
	 * 
	 */
	public void storeUnauthorized ( User user ) 
	{
		store ( user, false );
	}
	
	/**
	 * Deletes a user. Allows you to do that
	 * only if the current {@link #getLoggedInUser() logged user} is an ADMIN. 
	 */
	public boolean delete ( String email )
	{
		email = StringUtils.trimToNull ( email );
		Validate.notNull ( email, "Error for delete (): email cannot be null" );
		
		enforceRole ( Role.ADMIN, true );
		
		if ( email.equalsIgnoreCase ( getLoggedInUser ().getEmail () ) )
			throw new SecurityException ( "Cannot delete the authenticated user '" + email + "'" );
		
		return deleteUnauthorized ( email );
	}

	/**
	 * Deletes a user without any permission checking in advance. Useful for tests and alike.
	 */
	public boolean deleteUnauthorized ( String email )
	{
		email = StringUtils.trimToNull ( email );
		Validate.notNull ( email, "Error for delete (): email cannot be null" );
		
		Query q = entityManager.createQuery ( 
			"DELETE from " + User.class.getName () + " WHERE email = '" + email.toLowerCase () + "'" 
		);
		return q.executeUpdate () > 0;
	}

}
