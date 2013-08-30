package uk.ac.ebi.fg.myequivalents.webservices.client;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.MyEquivalentsManager;

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

	public abstract User setAuthenticationCredentials ( String email, String apiPassword, boolean connectServer ) throws SecurityException;

	protected Form prepareReq ()
	{
		Form req = new Form ();
		
		if ( this.email != null ) req.add ( "email", this.email );
		if ( this.apiPassword != null ) req.add ( "secret", this.apiPassword );

	  if ( log.isTraceEnabled () ) log.trace ( "requested web service\n: " + req );
		return req;
	}
	
	protected <T> T invokeWsReq ( String reqPath, Form req, Class<T> targetClass )
	{
		Client cli = Client.create ();
		WebResource wr = cli.resource ( this.baseUrl + getServicePath () + reqPath );
		return wr.accept( MediaType.APPLICATION_XML_TYPE ).post ( targetClass, req );
	}
	
	@Override
	public void close () {}

}
