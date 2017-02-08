package uk.ac.ebi.fg.myequivalents.webservices.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import uk.ac.ebi.fg.myequivalents.exceptions.UnsupportedFormatException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.FormatHandler;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.XmlFormatHandler;
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;

import com.gc.iotools.stream.os.OutputStreamToInputStream;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * Client implementation of the web service for {@link BackupManager}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>11 Mar 2015</dd>
 *
 */
public class BackupWSClient extends MyEquivalentsWSClient implements BackupManager
{
	public BackupWSClient ()
	{
		super ();
	}

	public BackupWSClient ( String baseUrl )
	{
		super ( baseUrl );
	}

	@Override
	public Stream<MyEquivalentsModelMember> dump ( Integer offset, Integer limit )
	{
		try
		{
			Form req = prepareReq ();
			if ( offset != null ) req.add ( "offset", offset );
			if ( limit != null ) req.add ( "limit", limit );
	
			XmlFormatHandler formatReader = new XmlFormatHandler ();
			HttpResponse response = getRawResponse ( "/dump", req, formatReader );
	
			HttpEntity entity = response.getEntity ();
			if ( entity == null ) throw new IllegalStateException ( "No answer from the HTTP request" ); 
			
		  InputStream wsIn = entity.getContent ();			
		  return formatReader.read ( wsIn );
		}
		catch ( IOException ex ) {
			throw new RuntimeException ( "Internal error while dumping myEquivalents: " + ex.getMessage (), ex );
		}		
	}

	
	
	@Override
	public void dump ( OutputStream out, FormatHandler serializer, Integer offset, Integer limit )
	{
		try
		{
			// Get the dump via REST
			Form req = prepareReq ();
			if ( offset != null ) req.add ( "offset", offset );
			if ( limit != null ) req.add ( "limit", limit );
			
			HttpResponse response = null;
			try {
				response = getRawResponse ( "/dump", req, serializer );
			}
			catch ( UnsupportedFormatException ex ) {
				// This means the server can't handle this format, so so we have to first get objects loaded from the server 
				// output in default format, and then send them to the current serialiser.	
				Stream<MyEquivalentsModelMember> dumpStrm = this.dump ( offset, limit );
				serializer.serialize ( dumpStrm, out );
			}
						
			// Yes, it does, so we just need to pipe the server output into the output stream. 
			HttpEntity entity = response.getEntity ();
			if ( entity == null ) throw new IllegalStateException ( "No answer from the HTTP request" ); 
			
		  InputStream wsIn = entity.getContent ();			
			IOUtils.copyLarge ( wsIn, out );			
		}
		catch ( IOException ex ) {
			throw new RuntimeException ( "Internal error while dumping myEquivalents: " + ex.getMessage (), ex );
		}
	}

	
	
	@Override	
	public int upload ( InputStream in, FormatHandler formatReader )
	{
		// Courtesy of: http://neopatel.blogspot.de/2011/04/jersey-posting-multipart-data.html
		Client cli = Client.create ();
		WebResource wres = cli.resource ( this.baseUrl + this.getServicePath () + "/upload" );
    
		FormDataMultiPart req = new FormDataMultiPart();
		if ( this.email != null ) req.field ( "login", this.email );
		if ( this.apiPassword != null ) req.field ( "login-secret", this.apiPassword );
		req.bodyPart (  
			new FormDataBodyPart ( "dump", in, MediaType.APPLICATION_OCTET_STREAM_TYPE )
		);
				
		ClientResponse response = wres
			.type ( MediaType.MULTIPART_FORM_DATA )
			.accept ( formatReader.getContentTypes ().toArray ( new String [ 0 ] ) )
			.post ( ClientResponse.class, req );
		StatusType statusInfo = response.getStatusInfo ();
		handleHttpResultStatus ( statusInfo.getStatusCode (), statusInfo.getReasonPhrase () );
		
		return Integer.parseInt ( response.getEntity ( String.class ) );
	}

	
	@Override
	public int upload ( Stream<MyEquivalentsModelMember> in )
	{
		try
		{
			final FormatHandler handler = FormatHandler.of ( "xml" );
			
			final OutputStreamToInputStream<Integer> outPipe = new OutputStreamToInputStream<Integer>() 
			{
			  @Override
			  protected Integer doRead ( final InputStream in ) throws Exception {
			  	return upload ( in, handler );
			  }
			}; 		

			// While this writes onto outPipe, its internal thread pipes the same data to the server
			handler.serialize ( in, outPipe );
			int result = outPipe.getResult ();
			outPipe.close ();
			return result;
		}
		catch ( Exception ex )
		{
			throw new RuntimeException ( 
				"Internal error while uploading data via myEquivalents Web service: " + ex.getMessage (), ex 
			);
		}
	}
	
	
	@Override
	public int countEntities ()
	{
		Form req = prepareReq ();
	  return invokeIntWsReq ( "/count-entities", req );
	}

	
	@Override
	protected String getServicePath ()
	{
		return "/backup";
	}

}
