package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;

/**
 * Common functionality for the myEquivalents managers.
 * 
 * <p>In general, you should assume that implementations of manager interfaces are not thread-safe. The idea is that you 
 * create a new instance per thread, do some operations, release, all within the same thread.</p> 
 * 
 * <dl><dt>date</dt><dd>Mar 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface MyEquivalentsManager
{
	/** 
	 * <p>This should prepare the credentials to be used with every service call that needs authentication, which possibly
	 * means immediate verification of such credentials and {@link SecurityException} raising in case they're not valid.
	 * Such credentials are then used for all the requests of the manager, during its life span.</p>
	 * 
	 * <p>This version performs authentication based on API password (see documentation).</p>
	 *  
	 * <p>email = null is a special case, which should return a user named 'anonymous'.</p>
	 * 
	 * <p>Note that 'possibly' makes only the manager a stateful component, not necessarily the communication protocol it is based on, or its 
	 * server side. For instance, the client instance of a manager might be stateful and keep this information in 
	 * memory, as a facility, while the same information is passed through RESTful calls all the time, to keep the web 
	 * service protocol and the server stateless. This kind of implementation is preferable.</p>
	 * 
	 * @return the User object that was successfully authenticated with these credentials. Should such authentication 
	 * fail, a {@link SecurityException} is raised instead. Note that when the implementation doesn't actually verify
	 * the credentials, it might still return the User object that will be authenticated in subsequent requests (which 
	 * might trigger {@link SecurityException}).
	 */
	public User setAuthenticationCredentials ( String email, String apiPassword ) throws SecurityException;

	/**
	 * Does close/clean-up operations. There is no guarantee that a manager can be used after the invocation to this method.
	 * You may want to invoke this call in {@link Object#finalize()} in the implementation of this interface.
	 */
	public void close ();
}
