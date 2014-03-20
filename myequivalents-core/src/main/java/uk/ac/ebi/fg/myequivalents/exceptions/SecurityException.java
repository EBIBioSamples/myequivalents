package uk.ac.ebi.fg.myequivalents.exceptions;

/**
 * Security exceptions are used within myEquivalents to notify about authentication failures, non permitted access
 * attempts and alike.
 * 
 * More exceptions will be added in future, for the moment this is very necessary, in order to identify these kind
 * of problems while invoking a myEquivalents function.
 *
 * <dl><dt>date</dt><dd>Mar 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class SecurityException extends RuntimeException
{

	private static final long serialVersionUID = 4545702095847145883L;

	public SecurityException ( String message, Throwable cause ) {
		super ( message, cause );
	}

	public SecurityException ( String message ) {
		super ( message );
	}

}
