package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import uk.ac.ebi.fg.myequivalents.exceptions.UnsupportedFormatException;


/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Sep 2016</dd></dl>
 *
 */
class FormatHandlerFactory
{
	private static Map<String, FormatHandler> handlers = new HashMap<> ();
	
	static 
	{		
		for ( FormatHandler h: ServiceLoader.load ( FormatHandler.class ) )
		{
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
					tag = Optional.of ( tag ).orElseThrow ( 
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
				}  
			);
		}
	}
	
	@SuppressWarnings ( "unchecked" )
	public static <H extends FormatHandler> H of ( String typeTag, boolean failOnNoMatch )
	{
		H result = (H) handlers.get ( typeTag );
		if ( failOnNoMatch && result == null ) throw new UnsupportedFormatException (
			"Unsupported format '" + typeTag + "'"
		);
		return result;
	}

	public static <H extends FormatHandler> H of ( String typeTag ) {
		return of ( typeTag, false );
	}

}
