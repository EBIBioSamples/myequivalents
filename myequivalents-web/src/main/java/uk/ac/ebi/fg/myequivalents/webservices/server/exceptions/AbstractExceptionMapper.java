package uk.ac.ebi.fg.myequivalents.webservices.server.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Sep 2016</dd></dl>
 *
 */
public abstract class AbstractExceptionMapper<E extends Exception> implements ExceptionMapper<E> 
{

	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	protected final int returnedStatusCode;
	
	protected AbstractExceptionMapper ( Response.Status returnedStatus ) {
		this ( returnedStatus.getStatusCode () );
	}

	protected AbstractExceptionMapper ( int returnedStatusCode )
	{
		super ();
		this.returnedStatusCode = returnedStatusCode;
	}

	
	@Override
	public Response toResponse ( E ex )
	{
		log.error ( String.format ( 
			"myEquivalents Web Service %s: %s", ex.getClass ().getSimpleName (), ex.getMessage ()),
			ex
		);
		return Response
			.status ( this.returnedStatusCode )
			.entity ( ex.getMessage () )
			.type ( MediaType.TEXT_PLAIN )
			.build ();
	}
}