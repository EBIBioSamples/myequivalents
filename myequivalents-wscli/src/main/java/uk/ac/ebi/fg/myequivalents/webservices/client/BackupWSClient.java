package uk.ac.ebi.fg.myequivalents.webservices.client;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * TODO: comment me!
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
	public int dump ( OutputStream out, Integer offset, Integer limit )
	{
		try
		{
			Form req = prepareReq ();
			if ( offset != null ) req.add ( "offset", offset );
			if ( limit != null ) req.add ( "limit", limit );
			
			InputStream wsIn = getRawResultAsStream ( "/dump", req, "xml" );
			
			final int [] result = new int[] { -1 };
			
			FilterWriter ctw = new FilterWriter( new OutputStreamWriter ( out, Charsets.UTF_8 ) ) 
			{
				private int itracked = 0;
				private StringBuffer ctStr = null;
				private char csingle[] = new char [ 1 ];

				@Override
				public void write ( int c ) throws IOException
				{
					super.write ( c );
					csingle [ 0 ] = (char) c;
					interceptCountComment ( csingle, 0, 1 );
				}

				@Override
				public void write ( char[] cbuf, int off, int len ) throws IOException
				{
					super.write ( cbuf, off, len );
					interceptCountComment ( cbuf, off, len );
				}

				@Override
				public void write ( String str, int off, int len ) throws IOException
				{
					super.write ( str, off, len );
					char[] buf = new char [ len ];
					str.getChars ( off, off + len, new char [ len ], 0 );
					interceptCountComment ( buf, 0, len );
				}
				
				private void interceptCountComment ( char[] c, int off, int len )
				{
					int end = off + len;
					for ( int i = off; i < end; i++ )
					{
						if ( result [ 0 ] != -1 ) return; // result already fully tracked
						
						if ( ctStr != null ) // result being built
						{
							if ( c [ i ] == ' ' )
							{
								if ( ctStr.length () > 0 ) 
									// second space after '=', the end
									result [ 0 ] = Integer.valueOf ( ctStr.toString () );
							}
							else 
								// keep collecting result digits
								ctStr.append ( c [ i ] );
						}
						else 
						{
							// We have to search the result comment, or we are inside it
							if ( "<!-- dumped-items-count =".charAt ( itracked ) == c [ i ] )
							{
								// We just started, or we inside it, maybe
								itracked++;
								if ( c [ i ] == '=' )
									ctStr = new StringBuffer ();
							}
							else 
								// Either it's not the right start point, or it was another comment
								itracked = 0;
						}
					} // for i
				} // interceptCountComment ()
			};
			
			IOUtils.copy ( new InputStreamReader ( wsIn, Charsets.UTF_8 ), ctw );
			ctw.flush ();

			if ( result [ 0 ] == -1 ) throw new RuntimeException ( 
				"Internal error while dumping myEquivalents: the server didn't send any result count" 
			);
			return result [ 0 ];
		}
		catch ( IOException ex )
		{
			// TODO Auto-generated catch block
			throw new RuntimeException ( "Internal error while dumping myEquivalents: " + ex.getMessage (), ex );
		}
	}

	@Override
	public int upload ( InputStream in )
	{
		// Courtesy of: http://neopatel.blogspot.de/2011/04/jersey-posting-multipart-data.html
		Client cli = Client.create ();
		WebResource wres = cli.resource ( this.baseUrl + this.getServicePath () + "/upload" );
    
		FormDataMultiPart req = new FormDataMultiPart();
		if ( this.email != null ) req.field ( "login", this.email );
		if ( this.apiPassword != null ) req.field ( "login-secret", this.apiPassword );
		req.bodyPart (  
			new FormDataBodyPart ( "dump-xml", in, MediaType.APPLICATION_OCTET_STREAM_TYPE )
		);
		
		return Integer.valueOf ( wres.type ( MediaType.MULTIPART_FORM_DATA ).post ( String.class, req ));
	}

	@Override
	protected String getServicePath ()
	{
		return "/backup";
	}

}
