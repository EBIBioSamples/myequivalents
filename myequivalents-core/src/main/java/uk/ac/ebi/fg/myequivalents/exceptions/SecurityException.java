package uk.ac.ebi.fg.myequivalents.exceptions;

/**
 * TODO: Comment me!
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
