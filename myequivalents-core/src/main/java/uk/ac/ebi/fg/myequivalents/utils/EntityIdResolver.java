package uk.ac.ebi.fg.myequivalents.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.myequivalents.model.EntityId;
import uk.ac.ebi.fg.myequivalents.model.Service;
import static uk.ac.ebi.fg.myequivalents.model.Service.UNSPECIFIED_SERVICE_NAME;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 May 2015</dd>
 *
 */
public class EntityIdResolver
{
	public static final Pattern ENT_ID_SPLIT_PATTERN = Pattern.compile ( "(?<!(\\\\|:+)):(?!//|:+)" );

	// Replace $id, unless it's \\$id
	public static final Pattern ID_PLACEHOLDER_PATTERN = Pattern.compile ( "(?<!\\\\)\\$id" );
	
	public static final Pattern URI_FIND_PREFIX_PATTERN = Pattern.compile ( "[\\#/\\=\\\\]+" );

	
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
			
			Matcher matcher = ENT_ID_SPLIT_PATTERN.matcher ( entityId );
			if ( matcher.find () )
			{
				int idx = matcher.start ();
				String serviceName = StringUtils.trimToNull ( entityId.substring ( 0, idx ) );
				String acc = StringUtils.trimToNull ( entityId.substring ( idx + 1 ) );
				if ( acc == null ) throw new RuntimeException ( 
					"Syntax error (null entity accession) for entity ID '" + entityId + "'"
				);
				// Is this acc a URI?
				if ( acc.startsWith ( "<" ) )
				{
					if ( !acc.endsWith ( ">" ) ) throw new RuntimeException ( 
						"Syntax error for entity ID '" + entityId + "'" 
					);
					
					uri = acc.substring ( 1, acc.length () - 1 );
					acc = null;
					
					// if the service name was empty in this ***:*** pattern, then you mean unspecified
					if ( serviceName == null ) serviceName = UNSPECIFIED_SERVICE_NAME;
				}
					
				return new EntityId ( serviceName, acc, uri );
			}
			
			throw new RuntimeException ( 
				"Syntax error (null entity accession) for entity ID '" + entityId + "'"
			);
		}	
	}
	
	
	/**
	 *	if uri != null invokes some form of resolveUri() (see below), else uses {@link #resolve(String, String)}. 
	 */
	public EntityId resolve ( String serviceName, String acc, String uri )
	{
		serviceName = StringUtils.trimToNull ( serviceName );
		acc = StringUtils.trimToNull ( acc );
		uri = StringUtils.trimToNull ( uri );
		
		if ( uri != null )
		{
			EntityId result = serviceName == null
				? resolveUri ( uri )
				: acc == null 
					? resolveUri ( serviceName, uri ) 
					:	resolveUri ( serviceName, acc, uri );
				
			// TODO: in the DB implementation, verify that if acc != null => result.acc == acc 
			
			return result; 
		}
		else
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
	 * Assumes a null URI and returns a new {@link EntityId}, using the two received parameters.
	 * 
	 * So, here we don't actually 'resolve' anything, ie, we don't lookup the service into 
	 * any storage backend. We suggest that you keep this method this way even in backend-specific
	 * implementation, since it's usually used when the invoker already knows to be dealing with
	 * a service:acc entity identifier and usually it doesn't need the service in such situation.
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
	 * This default implementation just uses {@code 'new Service (serviceName)'} and calls 
	 * {@link #breakUri(String, String)}.
	 * 
	 * A real implementation should fetch the service (including {@link Service#UNSPECIFIED_SERVICE_NAME}) and, 
	 * if the acc != null, verify the URI. 
	 * 
	 * This method is not supposed to pre-process its parameters (eg, {@link String#trim()}), since that's
	 * usually done by {@link #parse(String)}. 
	 * 
	 */
	public EntityId resolveUri ( String serviceName, String acc, String uri )
	{
		return new EntityId ( new Service ( serviceName ), acc, breakUri ( acc, uri ) );
	}
	
	
	
	
	/**
	 * Tries to find the prefix in a URI having a form like {@code prefix + <acc>}. Eg, for
	 * http://www.somewhere.net/path/to/123 returns into http://www.somewhere.net/path/to/$id, even 
	 * when {@code acc} is null (uses the last slash as separator). 
	 * 
	 * If this prefix-guessing doesn't work, tries to cut the URI's after the domain 
	 * specification (eg, http://www.somewhere.net), so that this can be used to find one or more services. 
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
	
	public static String breakUri ( String uri )
	{
		return breakUri ( null, uri );
	}
	
	
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
	
	
	public static String buildUriFromAcc ( String acc, String uriPattern )
	{
		if ( uriPattern == null ) return null;

		// Special case the URI is also the accession (ie, UNSPECIFIED_SERVICE), let's speed up things a bit
		if ( "$id".equals ( uriPattern ) ) return acc;
		
		// Replace $id, unless it's \\$id
		return ID_PLACEHOLDER_PATTERN.matcher ( uriPattern ).replaceAll ( acc );
	}


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
		
		
		// Does the URI's begin match the pattern chunks?
		if ( !uri.startsWith ( uriPatPrefx ) ) return null;
		
		int uriPatPrefxLen = uriPatPrefx.length ();
				
		int endAccIdx = uriPatTail != null ? uri.indexOf ( uriPatTail, uriPatPrefxLen ) : uri.length ();
		if ( endAccIdx == -1 ) return null;
		
		// If yes, the accession is the thing in between
		return uri.substring ( uriPatPrefxLen, endAccIdx );
	}

}
