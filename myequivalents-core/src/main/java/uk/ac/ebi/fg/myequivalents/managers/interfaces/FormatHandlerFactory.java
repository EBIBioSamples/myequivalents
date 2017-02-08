package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import uk.ac.ebi.fg.myequivalents.exceptions.UnsupportedFormatException;


/**
 * Loads format handlers by means of the 
 * <a href = "https://docs.oracle.com/javase/tutorial/ext/basics/spi.html">SPI mechanism</a>. This means
 * the exact list of handlers that are available depends on the jars you have in the classpath.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Sep 2016</dd></dl>
 *
 */
public class FormatHandlerFactory
{
	private static Map<String, FormatHandler> handlers = new HashMap<> ();
	
	static 
	{		
		// Load all the FHs
		for ( FormatHandler h: ServiceLoader.load ( FormatHandler.class ) )
		{
			// Map each type/content-type that the FH supports to itself.
			Set<String> shortTypes = h.getShortTypes (), contentTypes = h.getContentTypes ();
			if ( ( shortTypes == null || shortTypes.isEmpty () ) && ( contentTypes == null || contentTypes.isEmpty () ) ) 
				throw new IllegalArgumentException ( 
					"Either one of shortTypes or one contentTypes must be defined for " + h.getClass ().getName () 
				);
			
			Stream
			.of ( h.getShortTypes (), h.getContentTypes () )
			.flatMap ( Collection::stream )
			.forEach ( tag -> 
			{					
				tag = Optional.ofNullable ( tag ).orElseThrow ( 
					() -> new IllegalArgumentException ( "Null FormatHandler type for " + h.getClass ().getName () ) 
				);
				
				FormatHandler h1 = handlers.get ( tag );
				if ( h1 != null && !h1.equals ( h ) )
					throw new IllegalArgumentException ( String.format ( 
					  "Internal error: the same type '%s' is associated to both %s and %s, " +
					  "each type must map to one FormatHandler only",
						tag, h.getClass ().getName (), h1.getClass ().getName ()
				));
				handlers.put ( tag, h );
			});
		}
	}
	
	/**
	 * Tells the handler associated to this {@link FormatHandler#getShortTypes() short type} or
	 * {@link FormatHandler#getContentTypes() content type}.
	 * 
	 * @param failOnNoMatch if true, triggers {@link UnsupportedFormatException} an exception when no handler exists for 
	 * the specified type.
	 */
	@SuppressWarnings ( "unchecked" )
	public static <H extends FormatHandler> H of ( String typeTag, boolean failOnNoMatch )
	{
		H result = (H) handlers.get ( typeTag );
		if ( failOnNoMatch && result == null ) throw new UnsupportedFormatException (
			"Unsupported format '" + typeTag + "'"
		);
		return result;
	}

	/**
	 * Defaults to false.
	 */
	public static <H extends FormatHandler> H of ( String typeTag ) {
		return of ( typeTag, false );
	}

	public static Set<FormatHandler> getAllHandlers () {
		return new HashSet<FormatHandler> ( handlers.values () );
	}
}
