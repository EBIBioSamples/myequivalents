package uk.ac.ebi.fg.myequivalents.webservices.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.MyEquivalentsManager;
import uk.ac.ebi.utils.io.IOUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>30 Aug 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
abstract class MyEquivalentsWSClient implements MyEquivalentsManager
{
	protected final String baseUrl;
	protected String email = null, apiPassword = null;

	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * All the invocations provided by this client will be routed at the web service base address passed here. The default
	 * it 'http://localhost:8080/ws', /ws is usually the path where the web service package locates its implementation. 
	 */
	public MyEquivalentsWSClient ( String baseUrl )
	{
		super ();
		baseUrl = StringUtils.trimToNull ( baseUrl );
		if ( baseUrl == null ) baseUrl = "http://localhost:8080/myequivalents/ws";
		else if ( baseUrl.charAt ( baseUrl.length () - 1 ) == '/' ) baseUrl = baseUrl.substring ( 0, baseUrl.length () - 2 );
		
		this.baseUrl = baseUrl;
	}

	public MyEquivalentsWSClient () {
		this ( null );
	}

	protected abstract String getServicePath ();
	
	
	@Override
	public User setAuthenticationCredentials ( String email, String apiPassword ) throws SecurityException
	{
		return setAuthenticationCredentials ( email, apiPassword, false );
	}

	public User setAuthenticationCredentials ( String email, String apiPassword, boolean connectServer ) throws SecurityException
	{
		this.email = StringUtils.trimToNull ( email );
		this.apiPassword = StringUtils.trimToNull ( apiPassword );
		if ( !connectServer ) return null;
					
		Form req = prepareReq ();
		return invokeWsReq ( "/perms", "/login", req, User.class );
	}

	protected Form prepareReq ()
	{
		Form req = new Form ();
		
		if ( this.email != null ) req.add ( "login", this.email );
		if ( this.apiPassword != null ) req.add ( "login-secret", this.apiPassword );

		return req;
	}
	
	private <T> T invokeWsReq ( String servicePath, String reqPath, Form req, Class<T> targetClass )
	{
		try
		{
		  if ( log.isTraceEnabled () ) 
		  {
		  	Map<String, Object> debugReq = new HashMap<String, Object> ( req );
		  	if ( debugReq.get ( "login-secret" ) != null ) debugReq.put ( "login-secret", "***" );
		  	if ( debugReq.get ( "login-pwd" ) != null ) debugReq.put ( "login-pwd", "***" );
		  	if ( debugReq.get ( "secret" ) != null ) debugReq.put ( "secret", "***" );
		  	if ( debugReq.get ( "password" ) != null ) debugReq.put ( "password", "***" );
		  	log.trace ( "Requested web service: {}\n: {}", reqPath, debugReq );
		  }

			Client cli = Client.create ();
			WebResource wr = cli.resource ( this.baseUrl + servicePath + reqPath );
			WebResource.Builder builder = wr.accept ( MediaType.APPLICATION_XML_TYPE );
			
			if ( targetClass == null ) 
			{
				builder.post ( req );
				return null;
			}
			else
				return builder.post ( targetClass, req );
		} 
		catch ( UniformInterfaceException ex ) 
		{
			ClientResponse.Status status = ex.getResponse ().getClientResponseStatus ();
			String msg = status.getReasonPhrase () + " [" + status.getStatusCode () + "]";
			throw new SecurityException ( "Security problem with the myEquivalents web service: " + msg, ex );
		} 
	}

	protected <T> T invokeWsReq ( String reqPath, Form req, Class<T> targetClass )
	{
		return invokeWsReq ( getServicePath (), reqPath, req, targetClass );
	}
	
	
	protected void invokeVoidWsReq ( String reqPath, Form req ) {
		invokeWsReq ( reqPath, req, null );
	}

	protected int invokeIntWsReq ( String reqPath, Form req ) 
	{
		String sresult = invokeWsReq ( reqPath, req, String.class );
		return Integer.parseInt ( sresult );
	}
	
	protected boolean invokeBooleanWsReq ( String reqPath, Form req ) 
	{
		String sresult = invokeWsReq ( reqPath, req, String.class );
		return Boolean.parseBoolean ( sresult );
	}
	
	protected String getRawResult ( String reqPath, Form req, String outputFormat ) 
	{
		outputFormat = StringUtils.trimToNull ( outputFormat );
		if ( outputFormat == null ) outputFormat = "xml";
		if ( !"xml".equalsIgnoreCase ( outputFormat ) ) throw new IllegalArgumentException ( 
			"Unsopported output format '" + outputFormat + "'" 
		);
		String acceptValue = MediaType.APPLICATION_XML; // TODO: more options in future
		
		Throwable theEx = null;
		String result = null;
		
		try
		{
			// Request via straight POST request
			HttpClient client = new DefaultHttpClient ();
			HttpPost post = new HttpPost ( this.baseUrl + getServicePath () + reqPath );
			List<NameValuePair> params = new ArrayList<NameValuePair> ();
			for ( String pname: req.keySet () ) 
				for ( String val: req.get ( pname ) ) 
					params.add ( new BasicNameValuePair ( pname, val ) );
			post.setEntity ( new UrlEncodedFormEntity ( params, "UTF-8" ) );
			post.setHeader ( "Accept", acceptValue );
			HttpResponse response = client.execute ( post );
			
			StatusLine statusLine = response.getStatusLine ();
			if ( statusLine.getStatusCode () == Response.Status.FORBIDDEN.getStatusCode () ) throw new SecurityException ( 
				"Security problem with the myEquivalents web service: " + statusLine.getReasonPhrase () 
			);
			
			HttpEntity entity = response.getEntity ();
			if ( entity == null ) throw new IllegalStateException ( 
				"No answer from the HTTP request while executing '" + post.getURI () + "'" );
			
		  result = IOUtils.readInputFully ( new InputStreamReader ( entity.getContent () ) );
		  
//			// It get back as ugly XML, reformat
//			TransformerFactory tf = TransformerFactory.newInstance ();
//			Transformer transformer = tf.newTransformer ();
//			transformer.setOutputProperty ( OutputKeys.OMIT_XML_DECLARATION, "no" );
//			transformer.setOutputProperty ( OutputKeys.METHOD, "xml" );
//			transformer.setOutputProperty ( OutputKeys.INDENT, "yes" );
//			transformer.setOutputProperty ( OutputKeys.ENCODING, "UTF-8" );
//			transformer.setOutputProperty ( "{http://xml.apache.org/xslt}indent-amount", "2" );
//
//			transformer.transform ( new StreamSource ( entity.getContent () ), new StreamResult ( sw ) );
		} 
		catch ( UnsupportedEncodingException ex ) { theEx = ex; }
		catch ( ClientProtocolException ex ) { theEx = ex; }
		//catch ( TransformerConfigurationException ex ) { theEx = ex; }
		catch ( IllegalArgumentException ex ) { theEx = ex; }
		catch ( IOException ex ) { theEx = ex; }
		catch ( TransformerFactoryConfigurationError ex ) { theEx = ex; }
		//catch ( TransformerException ex )  { theEx = ex; }
		
		if ( theEx != null ) throw new RuntimeException ( 
			String.format ( 
				"Error while executing the web request: '%s': %s",  
				this.baseUrl + getServicePath () + reqPath, theEx.getMessage () 
			), 
			theEx 
		);
		
		return result;
  }
	
	/**
	 * Resets user's email and password to null.
	 */
	@Override
	public void close ()
	{
		email = apiPassword = null;
	}

//protected static void throwUnsupportedException () 
//{
//	throw new UnsupportedOperationException ( 
//		"This operation from the WS client is not implemented yet. Please ask developers" );
//}

}
