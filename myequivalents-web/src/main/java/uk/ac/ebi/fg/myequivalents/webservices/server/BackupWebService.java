package uk.ac.ebi.fg.myequivalents.webservices.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
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
	@Produces ( MediaType.APPLICATION_XML )
	public Response dump (
		@FormParam ( "login" ) final String email, 
		@FormParam ( "login-secret" ) final String apiPassword,
		@FormParam ( "offset" ) final Integer offset, 
		@FormParam ( "limit" ) final Integer limit 
	)
	{
		StreamingOutput stream = new StreamingOutput() 
		{
			@Override
			public void write ( OutputStream out ) throws IOException, WebApplicationException
			{
				BackupManager bkpMgr = getBackupManager ( email, apiPassword );
				int result = bkpMgr.dump ( out, offset, limit );
				out.write ( String.format ( "<!-- dumped-items-count = %d -->\n", result ).getBytes () );
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
		@FormDataParam ( "dump-xml" ) InputStream dumpXmlIn 
	)
	{
		BackupManager bkpMgr = getBackupManager ( email, apiPassword );
		return Integer.toString ( bkpMgr.upload ( dumpXmlIn ) );
	}
	
	/**
	 * TODO: AOP
	 */
	private BackupManager getBackupManager ( String authEmail, String authApiPassword ) 
	{
		log.trace ( "Returning backup manager for the user {}, {}", authEmail, authApiPassword == null ? null: "***" );
		return Resources.getInstance ().getMyEqManagerFactory ().newBackupManager ( authEmail, authApiPassword );
	}

}
