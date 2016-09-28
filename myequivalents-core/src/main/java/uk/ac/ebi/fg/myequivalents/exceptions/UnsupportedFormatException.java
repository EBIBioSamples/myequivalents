package uk.ac.ebi.fg.myequivalents.exceptions;

/**
 * This is used used within myEquivalents to notify that a requested format for some operation is not supported.
 * 
 * More exceptions will be added in future, for the moment this is very necessary, in order to identify these kind
 * of problems while invoking a myEquivalents function.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Sep 2016</dd></dl>
 *
 */
public class UnsupportedFormatException extends IllegalArgumentException
{
	private static final long serialVersionUID = 6015652492394766774L;
	
	public UnsupportedFormatException ( String message, Throwable cause ) {
		super ( message, cause );
	}

	public UnsupportedFormatException ( String message ) {
		super ( message );
	}
}
