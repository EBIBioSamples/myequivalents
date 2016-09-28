package uk.ac.ebi.fg.myequivalents.webservices.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.exceptions.UnsupportedFormatException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.FormatHandler;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

import com.sun.jersey.multipart.FormDataParam;

/**
 * Server side for the web sevice that implements {@link BackupManager}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>10 Mar 2015</dd>
 *
 */
@Path ( "/backup" )
public class BackupWebService
{
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	@POST
	@Path( "/dump" )
	public Response dump (
		@FormParam ( "login" ) final String email, 
		@FormParam ( "login-secret" ) final String apiPassword,
		@FormParam ( "offset" ) final Integer offset, 
		@FormParam ( "limit" ) final Integer limit,
		@Context HttpHeaders headers
	)
	{
		FormatHandler handler = getFormatHandlerFromAccept ( headers );
		
		StreamingOutput stream = new StreamingOutput() 
		{
			@Override
			public void write ( OutputStream out ) throws IOException, WebApplicationException
			{
				BackupManager bkpMgr = getBackupManager ( email, apiPassword );
				bkpMgr.dump ( out, handler, offset, limit );
				out.close ();
			}
		};
		return Response.ok ( stream ).build ();
	}
	
	/**
	 * Code taken from
	 * <a href = 'http://neopatel.blogspot.de/2011/04/jersey-posting-multipart-data.html'>this post</a>.
   *
	 */
	@POST
	@Path( "/upload" )
	@Produces ( MediaType.APPLICATION_XML )
  @Consumes ( MediaType.MULTIPART_FORM_DATA )
	public String upload (
		@FormDataParam ( "login" ) String email, 
		@FormDataParam ( "login-secret" ) String apiPassword,
		@FormDataParam ( "dump" ) InputStream dumpIn,
		@Context HttpHeaders headers
	)
	{
		FormatHandler handler = getFormatHandlerFromAccept ( headers );
		BackupManager bkpMgr = getBackupManager ( email, apiPassword );
		return Integer.toString ( bkpMgr.upload ( dumpIn, handler ) );
	}
	
	/**
	 * TODO: AOP
	 */
	private BackupManager getBackupManager ( String authEmail, String authApiPassword ) 
	{
		log.trace ( "Returning backup manager for the user {}, {}", authEmail, authApiPassword == null ? null: "***" );
		return Resources.getInstance ().getMyEqManagerFactory ().newBackupManager ( authEmail, authApiPassword );
	}

	
	/**
	 * TODO: comment me!
	 * @param headers
	 * @return
	 */
	public static FormatHandler getFormatHandlerFromAccept ( HttpHeaders headers )
	{
		String outFmts = Optional
		.of ( headers.getRequestHeader ( "Accept" ) )
		.orElse ( Collections.emptyList () )
		.stream ()
		.findFirst ()
		.orElse ( "xml" );
		
		FormatHandler handler = (FormatHandler) Arrays
		.stream ( outFmts.split ( "," ) )
		.map ( fmt -> FormatHandler.of ( StringUtils.trimToEmpty ( fmt ) ) )
		.filter ( h -> h != null )
		.findFirst ()
		.orElse ( null );
		
		if ( handler == null ) throw new UnsupportedFormatException (
			"Cannot support the 'Accept:' content type '" + outFmts + "'"
		);
		
		return handler;
	}
	
}
