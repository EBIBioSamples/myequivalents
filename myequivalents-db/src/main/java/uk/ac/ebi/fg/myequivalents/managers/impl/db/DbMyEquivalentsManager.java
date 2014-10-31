package uk.ac.ebi.fg.myequivalents.managers.impl.db;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.MyEquivalentsManager;

/**
 * An implementation of {@link MyEquivalentsManager} for the relational backend.
 *
 * TODO: comments!
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

	public DbMyEquivalentsManager ( EntityManager entityManager, String email, String apiPassword )
	{
		this.entityManager = entityManager;
		setAuthenticationCredentials ( email, apiPassword );
	}

	protected User setAuthenticationCredentials ( String email, String password, boolean isUserPass ) throws SecurityException
	{
		this.userDao = new UserDao ( entityManager, email, password, isUserPass );
		return this.userDao.getLoggedInUser ();
	}

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
		entityManager.close ();
	}

	@Override
	protected void finalize () throws Throwable
	{
		super.finalize ();
		this.close();
	}
	
}
