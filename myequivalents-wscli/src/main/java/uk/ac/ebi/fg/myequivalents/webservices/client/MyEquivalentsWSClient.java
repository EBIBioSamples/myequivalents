package uk.ac.ebi.fg.myequivalents.webservices.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import uk.ac.ebi.fg.myequivalents.utils.ManagerUtils;
import uk.ac.ebi.utils.io.IOUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

/**
 * This is a base class to implement myEquivalents managers as web service manager clients.
 * All the stuff related to the web service are based on the Jersey library (and hence REST and JAX-RS). 
 *
 * <dl><dt>date</dt><dd>30 Aug 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class MyEquivalentsWSClient implements MyEquivalentsManager
{
	protected final String baseUrl;
	protected String email = null, apiPassword = null;

	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * <p>All the invocations provided by this client will be routed at the web service base address passed here. The default
	 * it 'https://localhost:8080/ws', /ws is usually the path where the web service package locates its implementation.</p>
	 * 
	 * <p>We recommend to user HTTPS connections and POST requests, which hide passwords from the request URL</p>
	 */
	public MyEquivalentsWSClient ( String baseUrl )
	{
		super ();
		baseUrl = StringUtils.trimToNull ( baseUrl );
		if ( baseUrl == null ) baseUrl = "https://localhost:8080/myequivalents/ws";
		else if ( baseUrl.charAt ( baseUrl.length () - 1 ) == '/' ) baseUrl = baseUrl.substring ( 0, baseUrl.length () - 2 );
		
		this.baseUrl = baseUrl;
	}

	public MyEquivalentsWSClient () {
		this ( null );
	}

	/**
	 * Usually every manager is bound to a URL which starts with {@link #baseUrl} (i.e., the URL set by the constructor, 
	 * plus this path.
	 */
	protected abstract String getServicePath ();
	
	/**
	 * @see #setAuthenticationCredentials(String, String, boolean).
	 */
	@Override
	public User setAuthenticationCredentials ( String email, String apiPassword ) throws SecurityException
	{
		return setAuthenticationCredentials ( email, apiPassword, false );
	}

	/**
	 * {@link #setAuthenticationCredentials(String, String)} doesn't verify the credentials immediately, but only 
	 * when a manager's operations are requested. If you wish, you can verify the user in advance, by using this method
	 * with connectServer = true. 
	 * 
	 */
	public User setAuthenticationCredentials ( String email, String apiPassword, boolean connectServer ) throws SecurityException
	{
		this.email = StringUtils.trimToNull ( email );
		this.apiPassword = StringUtils.trimToNull ( apiPassword );
		if ( !connectServer ) return null;
					
		Form req = prepareReq ();
		return invokeWsReq ( "/perms", "/login", req, User.class );
	}

	/**
	 * Prepares a request for the web service, by putting user credential parameters (which were set via manager 
	 * constructor, or {@link #setAuthenticationCredentials(String, String, boolean)}) into it. 
	 * 
	 * This is used by manager methods to initialise a web service request.  
	 */
	protected Form prepareReq ()
	{
		Form req = new Form ();
		
		if ( this.email != null ) req.add ( "login", this.email );
		if ( this.apiPassword != null ) req.add ( "login-secret", this.apiPassword );

		return req;
	}
	
	/**
	 * Invokes a myEquivalents operation from the myEq web service.
	 *  
	 * @param servicePath is added to {@link #baseUrl}, the web service base URL passed to the manager constructor 
	 * @param reqPath is added to {@link #baseUrl} and servicePath, to build the final service operation's URL 
	 * @param req	The request as specified by Jersey's {@link Form}
	 * @param targetClass service is supposed to return an instance of this. Null means 'void'  
	 * @return and instance of targetClass, based on what the web service operation yields back for the current request.
	 */
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
			// Check if we got security exception
			ClientResponse.Status status = ex.getResponse ().getClientResponseStatus ();
			String msg = status.getReasonPhrase () + " [" + status.getStatusCode () + "]";
			
			if ( status.getStatusCode () == Response.Status.FORBIDDEN.getStatusCode () )
				// Emulate the server-side triggering of a security error. This is apparently the only way to do that via HTTP
				throw new SecurityException ( "Security problem with the myEquivalents web service: " + msg, ex );
			else
				throw new RuntimeException ( "Problem with myEquivalents web service: " + msg, ex );
		} 
	}

	/**
	 * Uses {@link #getServicePath()}
	 */
	protected <T> T invokeWsReq ( String reqPath, Form req, Class<T> targetClass )
	{
		return invokeWsReq ( getServicePath (), reqPath, req, targetClass );
	}
	
	/**
	 * Invokes {@link #invokeWsReq(String, Form, Class)} with targetClass = null, i.e., assuming the server returns 'void'.
	 */
	protected void invokeVoidWsReq ( String reqPath, Form req ) {
		invokeWsReq ( reqPath, req, null );
	}

	/**
	 * Assumes a return value of type string, which can be parsed as an {@link Integer}.
	 */
	protected int invokeIntWsReq ( String reqPath, Form req ) 
	{
		String sresult = invokeWsReq ( reqPath, req, String.class );
		return Integer.parseInt ( sresult );
	}
	
	/**
	 * Assumes a return value of type string, which can be parsed as an {@link Boolean}.
	 */
	protected boolean invokeBooleanWsReq ( String reqPath, Form req ) 
	{
		String sresult = invokeWsReq ( reqPath, req, String.class );
		return Boolean.parseBoolean ( sresult );
	}
	
	/**
	 * This is used to get back a service operation's invocation in raw format (only XML is currently supported).  
	 */
	protected String getRawResult ( String reqPath, Form req, String outputFormat ) 
	{
		try
		{
			return IOUtils.readInputFully ( new InputStreamReader ( 
				this.getRawResultAsStream ( reqPath, req, outputFormat ) 
			));
		}
		catch ( IOException ex )
		{
			throw new RuntimeException ( String.format ( 
				"Error while executing the web request: '%s': %s",  
				this.baseUrl + getServicePath () + reqPath, ex.getMessage () 
			), 
			ex );
		}		
	}
	
	
	
	protected InputStream getRawResultAsStream ( String reqPath, Form req, String outputFormat ) 
	{
		try
		{
			outputFormat = StringUtils.trimToNull ( outputFormat );
			
			if ( outputFormat == null ) 
				outputFormat = "xml";
			else 
				ManagerUtils.checkOutputFormat ( outputFormat );
			
			String acceptValue = MediaType.APPLICATION_XML; // TODO: more options in future
			
			// Request via straight POST request
			// 
			HttpClient client = new DefaultHttpClient ();
			HttpPost post = new HttpPost ( this.baseUrl + getServicePath () + reqPath );
			// Params need to be converted this way
			List<NameValuePair> params = new ArrayList<NameValuePair> ();
			for ( Map.Entry<String, List<String>> entry: req.entrySet () )
			{
				String pname = entry.getKey ();
				for ( String val: entry.getValue () ) 
					params.add ( new BasicNameValuePair ( pname, val ) );
			}
			post.setEntity ( new UrlEncodedFormEntity ( params, "UTF-8" ) );
			post.setHeader ( "Accept", acceptValue );
			
			// GO!
			HttpResponse response = client.execute ( post );
			
			// Check if the result is a security exception, emulate that on client side, in case it is
			StatusLine statusLine = response.getStatusLine ();
			if ( statusLine.getStatusCode () == Response.Status.FORBIDDEN.getStatusCode () ) throw new SecurityException ( 
				"Security problem with the myEquivalents web service: " + statusLine.getReasonPhrase () 
			);
			
			// Check the result
			HttpEntity entity = response.getEntity ();
			if ( entity == null ) throw new IllegalStateException ( 
				"No answer from the HTTP request while executing '" + post.getURI () + "'" );
			
			// Read the result in XML format.
		  return entity.getContent ();
		  
		} 
		catch ( IllegalArgumentException | IOException | TransformerFactoryConfigurationError ex )
		{
			throw new RuntimeException ( 
				String.format ( 
					"Error while executing the web request: '%s': %s",  
					this.baseUrl + getServicePath () + reqPath, ex.getMessage () 
				), 
				ex 
			);
		}
  }
	
	/**
	 * Resets user's email and password to null.
	 */
	@Override
	public void close ()
	{
		email = apiPassword = null;
	}

}
