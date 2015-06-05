package uk.ac.ebi.fg.myequivalents.managers.impl.db;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.MyEquivalentsManager;

/**
 * <h2>An implementation of {@link MyEquivalentsManager} for the relational back end</h2>
 *
 * <p>This is a base class, which is extended by all the DB-based managers in this package.</p>
 * 
 * <p>The relational database implementations of myEquivalents managers are run in a JVM which can connect to a JDBC-based
 * back end. This is further mediated by the object model in the core package, JPA/Hibernate mapping and DAO objects.</p>
 * 
 * <p>Note that the DB-based managers instantiate a new {@link EntityManager Hibernate EntityManager} in their constructors. 
 * This makes them one-entity-manager-per-request in many cases (e.g., when accessed by a web service). 
 * This should be the best transactional model to use in such cases. You might decide a different approach, by keeping 
 * an instance of this class the time you wish.</p>
 * 
 * <p>However, DB-based thread managers are not thread-safe, the idea is that you instantiate a manager within one thread,
 * do some operations from the same thread and then release the manager.</p>
 * 
 * <p>The persistence-related invocations in this manager does the transaction management automatically 
 * (i.e., they commit all implied changes upon operation invocation).</p>
 * 
 * <dl><dt>date</dt><dd>Mar 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class DbMyEquivalentsManager implements MyEquivalentsManager
{
	protected final EntityManager entityManager;
	protected UserDao userDao;
  protected Logger log = LoggerFactory.getLogger ( this.getClass () );

  /**
   * The default doesn't do any authentication, it's up to the subclasses to decide whether to login 'anonymous' 
   * automatically or not. 
   */
	protected DbMyEquivalentsManager ( EntityManager entityManager )
	{
		this ( entityManager, null, null );
	}
	
	/**
	 * This authenticates the user via {@link #setAuthenticationCredentials(String, String)}.
	 */
	public DbMyEquivalentsManager ( EntityManager entityManager, String email, String apiPassword )
	{
		this.entityManager = entityManager;
		setAuthenticationCredentials ( email, apiPassword );
	}

	/**
	 * This performs user authentication, i.e., verifies the user credentials against the database and throws an exception
	 * if they're invalid. That happens via {@link UserDao#login(String, String, boolean)}, which also means the anonymous
	 * user is returned if email is null. 
	 */
	protected User setAuthenticationCredentials ( String email, String password, boolean isUserPass ) throws SecurityException
	{
		this.userDao = new UserDao ( entityManager, email, password, isUserPass );
		return this.userDao.getLoggedInUser ();
	}

	/**
	 * Authenticates the user immediately, via {@link #setAuthenticationCredentials(String, String, boolean)}, using
	 * the user API password (see documentation). The credentials are immediately verified against the database and a 
	 * {@link SecurityException} is thrown in case they aren't invalid.
	 */
	@Override
	public User setAuthenticationCredentials ( String email, String apiPassword ) throws SecurityException {
		return setAuthenticationCredentials ( email, apiPassword, false );
	}

	/**
	 * A facility internally useful
	 */
	protected String getUserEmail ()
	{
		if ( this.userDao == null ) return null;
		User usr = this.userDao.getLoggedInUser ();
		return usr == null ? null : usr.getEmail ();
	}
	
	
	/**
	 * Invokes {@link #userDao userDao.logOff()} and closes the {@link #entityManager}.
	 */
	@Override
	public void close () 
	{
		userDao.logOff ();
		if ( entityManager.isOpen () ) entityManager.close ();
	}

	/**
	 * Invokes {@link #close()}.
	 */
	@Override
	protected void finalize () throws Throwable
	{
		super.finalize ();
		this.close();
	}
	
}
