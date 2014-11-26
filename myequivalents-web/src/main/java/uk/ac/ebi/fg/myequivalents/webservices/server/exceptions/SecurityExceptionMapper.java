package uk.ac.ebi.fg.myequivalents.webservices.server.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;

/**
 * <p>This is an {@link ExceptionMapper} that makes Jersey to generate a {@link Status#FORBIDDEN} HTTP status as a response
 * to a request, whenever A {@link SecurityException} is thrown. Client should behave accordingly and the Java-based
 * client we make available re-throw a {@link SecurityException} locally.</p>
 * 
 * <p>The {@link SecurityException}'s message is included in the HTTP response too (and out Java client unwraps it).</p>
 *   
 *
 * <dl><dt>date</dt><dd>6 Sep 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@Provider
public class SecurityExceptionMapper implements ExceptionMapper<SecurityException>
{
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	@Override
	public Response toResponse ( SecurityException ex )
	{
		log.error ( "myEquivalents Web Service SecurityException: " + ex.getMessage (), ex );
		return Response.status ( Response.Status.FORBIDDEN ).entity ( ex.getMessage () ).type ( MediaType.TEXT_PLAIN ).build ();
	}
}
