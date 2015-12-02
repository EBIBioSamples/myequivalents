package uk.ac.ebi.fg.myequivalents.utils;

import static uk.ac.ebi.fg.myequivalents.model.Service.UNSPECIFIED_SERVICE_NAME;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.EntityId;
import uk.ac.ebi.fg.myequivalents.model.Service;

/**
 * <p>This resolves an entity ID string into a an {@link EntityId}, that is, a pair of service + accession (+ URI).
 * an entity ID string follow a simple syntax, as explained in {@link #parse(String)}, which allows one to specify
 * either a service name + accession, or a straight URI, with or without the service reference added to it.</p>
 * 
 * <p>Note that his base class is almost useless alone and it requires a backend-specific implementation, such as
 * DbEntityIdResolver in the DB package (see {@link #resolveUri(String, String, String)}).</p> 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 May 2015</dd>
 *
 */
public class EntityIdResolver
{
	/**
	 * Used to split an entity ID string into two substrings, separated by colon. This regular expression
	 * doesn't consider special sequences like '\:', '://', '::' as separators.  
	 */
	public static final Pattern ENT_ID_SPLIT_PATTERN = Pattern.compile ( "(?<!(\\\\|:+)):(?!//|:+)" );

	/**
	 * Used to identify the '$id' string, which is considered an accession placeholder in URI patterns 
	 * (like in <a href = 'http://www.ebi.ac.uk/miriam'>MIRIAM</a>). This regular expression ignores 
	 * the sequence '\$id'.
	 */
	public static final Pattern ID_PLACEHOLDER_PATTERN = Pattern.compile ( "(?<!\\\\)\\$id" );
	
	/**
	 * Used in {@link #breakUri(String, String)}, to find the last separator in a URI, so that what follows can be considered
	 * an accession. 
	 */
	public static final Pattern URI_FIND_PREFIX_PATTERN = Pattern.compile ( "[\\#/\\=\\\\]+" );

	/**
	 * An entity ID may have the forms:
	 * <ul>
	 * <li>serviceName:accession, URI is computed from {@link Entity#getURI()}, i.e., by using 
	 * {@link Service#getUriPattern() uri pattern} and accession.</li>
	 * <li>serviceName:&lt;uri&gt;, the URI is intended to refer an entity provided by the service (an error is raised
	 * if it isn't)</li>
	 * <li>&lt;uri&gt;, a straight URI, in this case {@link #resolveUri(String)} tries to see if the URI corresponds to
	 * some existing myEquivalents service to be associated to the URI (resolving this type of entityId is a bit slower
	 * than all other syntax formats, use :&lt;&gt;, _:&lt;&gt;, or service:&lt;&gt;, if you can.
	 * <li>:&lt;uri&gt; or _:&lt;uri&gt;, which means the URI is not linked to a real service, but to the special 
	 * {@link Service#UNSPECIFIED_SERVICE}.</li>
	 * </ul>
	 * 
	 * Note that both the initial string and its sub-parts are pre-processed to trim extra boundary spaces. 
	 */
	public EntityId parse ( String entityId )
	{
		if ( entityId == null ) return null;
		entityId = entityId.trim ();
		
		if ( entityId.startsWith ( "<" ) )
		{
			// A full URI, no service mentioned
			if ( !entityId.endsWith ( ">" ) ) throw new RuntimeException ( "Syntax error for entity ID '" + entityId + "'" );
			
			String uri = entityId.substring ( 1, entityId.length () - 1 );
			
			return new EntityId ( (String) null, null, uri );
		}
		else
		{
			// a service:acc or service:<uri> form
			
			String uri = null;
			
			// Try to extract the two chunks demarked by ':'
			Matcher matcher = ENT_ID_SPLIT_PATTERN.matcher ( entityId );
			if ( matcher.find () )
			{
				int idx = matcher.start ();
				String serviceName = StringUtils.trimToNull ( entityId.substring ( 0, idx ) );
				String acc = StringUtils.trimToNull ( entityId.substring ( idx + 1 ) );
				if ( acc == null ) throw new RuntimeException ( 
					"Syntax error (null entity accession) for entity ID '" + entityId + "'"
				);
				
				// Is this acc actually a URI (i.e., wrapped in <>)?
				if ( acc.startsWith ( "<" ) )
				{
					if ( !acc.endsWith ( ">" ) ) throw new RuntimeException ( 
						"Syntax error for entity ID '" + entityId + "'" 
					);
					
					// then we have a URI and no real acc 
					uri = acc.substring ( 1, acc.length () - 1 );
					acc = null; 
					
					// if the service name was empty in this *:<*> pattern, then you mean unspecified
					// because if you want us to resolve the service, you should omit colon.
					if ( serviceName == null ) serviceName = UNSPECIFIED_SERVICE_NAME;
				}
					
				return new EntityId ( serviceName, acc, uri );
			}
			
			// None of the above worked
			throw new RuntimeException ( 
				"Syntax error (null entity accession) for entity ID '" + entityId + "'"
			);
		}	
	}
	
	/**
	 * invokes {@link #parse(String)} and then, if the result is non-null, {@link #resolve(EntityId)}.
	 */
	public EntityId doall ( String entityId )
	{
		EntityId eid = parse ( entityId );
		return eid == null ? null : resolve ( eid );
	}
	
	
	/**
	 * if uri != null invokes some form of resolveUri() (see below), else uses {@link #resolve(String, String)}. 
	 */
	public EntityId resolve ( String serviceName, String acc, String uri )
	{
		serviceName = StringUtils.trimToNull ( serviceName );
		acc = StringUtils.trimToNull ( acc );
		uri = StringUtils.trimToNull ( uri );
		
		if ( uri != null )
		{
			EntityId result = serviceName == null
				? resolveUri ( uri ) // No service, try to get it from the URI
				: acc == null 
					? resolveUri ( serviceName, uri ) // service + URI, no acc, verify it corresponds to the URI pattern.
					:	resolveUri ( serviceName, acc, uri ); // verify that the URI rebuilt with pattern + acc matches the param 
							
			return result; 
		}
		else
			// No URI, so it's in the form serviceName:acc
			return resolve ( serviceName, acc );
	}
	
	/**
	 * Just a wrapper for {@link #resolve(String, String, String)}.
	 */
	public EntityId resolve ( EntityId eid )
	{
		if ( eid == null ) throw new RuntimeException ( "Cannot resolve a null entity ID" );
		return resolve ( eid.getServiceName (), eid.getAcc (), eid.getUri () );
	}
	

	/**
	 * <p>Assumes a null URI and returns a new {@link EntityId}, using the two received parameters.</p>
	 * 
	 * <p>So, here we don't actually 'resolve' anything here, ie, we don't lookup the service into 
	 * any storage backend. We suggest that you keep this method this way even in backend-specific
	 * implementation, since it's usually used when the invoker already knows to be dealing with
	 * a service:acc entity identifier and usually it doesn't need the service in such a situation.</p>
	 * 
	 * <p>See DbEntityIdResolver in the DB package for details.</p>
	 * 
	 */
	public EntityId resolve ( String serviceName, String acc )
	{
		if ( serviceName == null || acc == null ) throw new RuntimeException ( String.format (  
			"Syntax error for '%s:%s': cannot resolve a service:acc pair if either of them are null", 
			serviceName, acc
		));
				
		return new EntityId ( serviceName, acc );
	}
	
	
	/**
	 * Invokes {@link #resolveUri(String, String, String) resolveUri ( null, null, uri )}.
	 */
	public EntityId resolveUri ( String uri ) {
		return resolveUri ( null, null, uri );
	}
	
	/**
	 * Invokes {@link #resolveUri(String, String, String) resolveUri ( serviceName, null, uri )}.
	 */
	public EntityId resolveUri ( String serviceName, String uri ) {
		return resolveUri ( serviceName, null, uri );
	}
	
	/**
	 * <p>This default implementation just uses {@code 'new Service (serviceName)'}.</p>
	 * 
	 * <p>A real implementation should try to fetch the service (including {@link Service#UNSPECIFIED_SERVICE_NAME}) and, 
	 * if the acc != null, verify the URI.</p> 
	 * 
	 * <p>This method is not supposed to pre-process its parameters (eg, {@link String#trim()}), since that's
	 * usually done by {@link #parse(String)}.</p> 
	 * 
	 */
	public EntityId resolveUri ( String serviceName, String acc, String uri )
	{
		return new EntityId ( new Service ( serviceName ), acc, uri );
	}
	
	
	
	
	/**
	 * Tries to find the prefix in a URI having a form like {@code prefix + <acc>}. Eg, for
	 * http://www.somewhere.net/path/to/123 returns into http://www.somewhere.net/path/to/$id, even 
	 * when {@code acc} is null (uses the last slash as separator).
	 * 
	 * If this prefix-guessing doesn't work, tries to split the URI's after the domain 
	 * specification (eg, http://www.somewhere.net), so that this can be used to find one or more services. 
	 * 
	 * If acc != null, it simply returns uri.replaceAll ( acc, "\\$id" ), i.e., quickly rebuilds the URI pattern
	 * by replacing the accession it contains with the placeholder.
	 * 
	 */
	public static String breakUri ( String acc, String uri )
	{
		if ( acc != null ) return uri.replaceAll ( acc, "\\$id" );
		
		String ruri = StringUtils.reverse ( uri );
		Matcher matcher = URI_FIND_PREFIX_PATTERN.matcher ( ruri );
		if ( matcher.find () )
			return uri.substring ( 0, uri.length () - matcher.start () ) + "$id";
		
		// else, try to find the domain
		return getDomain ( uri );
	}
	
	/**
	 * Wraps {@link #breakUri(String, String) breakUri ( null, uri )}.
	 */
	public static String breakUri ( String uri )
	{
		return breakUri ( null, uri );
	}
	
	/**
	 * Tries to split the URI at the point where the domain ends. e.g., for http://www.somewhere.net/path/to/123
	 * returns http://www.somewhere.net. This uses methods from {@link URI}.
	 */
	public static String getDomain ( String uri )
	{
		try
		{			
			URI urio = new URI ( uri );
			String auth = urio.getRawAuthority ();
			if ( auth == null || auth.length () == 0 ) return uri;
			
			int idx = uri.indexOf ( auth );
			
			return uri.substring ( 0, idx + auth.length () );
		}
		catch ( URISyntaxException ex ) {
			return uri;
		}
	}
	
	/**
	 * Simply builds the a URI replacing '$id' in uriPattern with acc (actually uses {@link #ID_PLACEHOLDER_PATTERN}).
	 * Note that if uriPattern = "$id", it quickly returns acc.  
	 */
	public static String buildUriFromAcc ( String acc, String uriPattern )
	{
		if ( uriPattern == null ) return null;

		// Special case the URI is also the accession (ie, UNSPECIFIED_SERVICE), let's speed up things a bit
		if ( "$id".equals ( uriPattern ) ) return acc;
		
		// Replace $id, unless it's \\$id
		return ID_PLACEHOLDER_PATTERN.matcher ( uriPattern ).replaceAll ( acc );
	}

	/**
	 * Tries to extract the accession from a given URI, matching the  {@link #ID_PLACEHOLDER_PATTERN '$id'} pattern.  
	 */
	public static String extractAccession ( String uri, String uriPattern )
	{
		if ( uriPattern == null ) return null; 
		
		// First find '$id' in the pattern
		Matcher matcher = ID_PLACEHOLDER_PATTERN.matcher ( uriPattern );
		
		if ( !matcher.find () ) return null;
		
		int startIdx = matcher.start (), endIdx = matcher.end ();
		
		// Now split it into prefix and postfix around the ID placeholder
		String uriPatPrefx = uriPattern.substring ( 0, startIdx );
		
		// Get a pattern tail but only up to the next $id placeholder
		String uriPatTail = null;
		if ( endIdx < uriPattern.length () ) 
		{
			int nextEndIdx = matcher.find () ? matcher.start () : uriPattern.length (); 
			uriPatTail = uriPattern.substring ( endIdx, nextEndIdx );
		}
		
		// Does the URI's head match the extracted head?
		if ( !uri.startsWith ( uriPatPrefx ) ) return null;
		
		// Does the URI tail match the extracted tail?
		int uriPatPrefxLen = uriPatPrefx.length ();
		int endAccIdx = uriPatTail != null ? uri.indexOf ( uriPatTail, uriPatPrefxLen ) : uri.length ();
		if ( endAccIdx == -1 ) return null;
		
		// If both head and tail match, then the accession is the thing in between
		return uri.substring ( uriPatPrefxLen, endAccIdx );
	}

}
