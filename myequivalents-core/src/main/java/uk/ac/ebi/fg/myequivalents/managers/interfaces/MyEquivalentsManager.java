package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;

/**
 * Common functionality for the myEquivalents managers.
 * 
 * <dl><dt>date</dt><dd>Mar 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface MyEquivalentsManager
{
	/** 
	 * This should prepare the credentials to be used with every service call that needs authentication. Note that this 
	 * makes only the manager a stateful component, not necessarily the communication protocol it is based on, or its 
	 * server side. For instance, the client instance of a manager might be stateful and keep this information in 
	 * memory, as a facility, while the same information is passed through RESTful calls all the time, to keep the web 
	 * service protocol and the server stateless. This kind of implementation is highly preferable.
	 * 
	 * Note that the implementation should accept email = null as a special case and return a user named 'anonymous' in 
	 * such case.
	 * 
	 * @return the User object that was successfully authenticated with these credentials. Should such authentication 
	 * fail, a {@link SecurityException} is raised instead.
	 */
	public User setAuthenticationCredentials ( String email, String apiPassword ) throws SecurityException;

	/**
	 * Does close/clean-up operations. There is no guarantee that a manager can be used after the invocation to this method.
	 * You may want to invoke this call in {@link Object#finalize()} in the implementation of this interface.
	 */
	public void close ();
}
