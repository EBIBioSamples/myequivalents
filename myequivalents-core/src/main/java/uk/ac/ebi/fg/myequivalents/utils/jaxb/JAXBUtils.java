package uk.ac.ebi.fg.myequivalents.utils.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.spi.CharsetProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;


/**
 * 
 * JAXB-related utils.
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 * TODO: move it to the core module.
 */
public class JAXBUtils
{
	private JAXBUtils () {}
	
	/**
	 * Converts source into XML, using the JAXB annotations defined in targetClass.
	 * propPairs is a list of String/Object pairs, which are passed to {@link Marshaller#setProperty(String, Object)}
	 */
	public static <T, S extends T> void marshal ( S source, Class<T> targetClass, OutputStream out, Object... propPairs )
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance ( targetClass );

			Marshaller m = context.createMarshaller ();
			m.setProperty ( Marshaller.JAXB_FORMATTED_OUTPUT, true );
			m.setProperty ( Marshaller.JAXB_ENCODING, "UTF-8" );
			if ( propPairs != null ) 
				for ( int i = 0; i < propPairs.length; i++ )
					m.setProperty ( (String) propPairs [ i ], propPairs [ ++i ] );
			
			m.marshal ( source, out );
		} 
		catch ( PropertyException ex ) {
			throw new RuntimeException ( "Internal error while generating the XML search result: " + ex.getMessage (), ex );
		} 
		catch ( JAXBException ex ) {
			throw new RuntimeException ( "Internal error while generating the XML search result: " + ex.getMessage (), ex );
		}		
		
	}

	/**
	 * Converts source into XML, using the JAXB annotations defined in targetClass.
	 *  
	 */
	public static <T, S extends T> String marshal ( S source, Class<T> targetClass )
	{
		try
		{
			StringWriter sw = new StringWriter ();
			OutputStream out = new WriterOutputStream ( sw, Charsets.UTF_8 );
			marshal ( source, targetClass, out, (Object[]) null );
			out.close ();
			return sw.toString ();
		}
		catch ( IOException ex ) {
			throw new RuntimeException ( "Internal error while converting object to XML: " + ex.getMessage (), ex );
		}
	}
	
	
	@SuppressWarnings ( "unchecked" )
	public static <T> T unmarshal ( InputStream in, Class<T> targetClass, Object... propPairs )
	{
		JAXBContext context;
		try
		{
			context = JAXBContext.newInstance ( targetClass );
			Unmarshaller um = context.createUnmarshaller ();

			if ( propPairs != null ) 
			{
				for ( int i = 0; i < propPairs.length; i++ )
					um.setProperty ( (String) propPairs [ i ], propPairs [ ++i ] );
			}
			
			return (T) um.unmarshal ( in );
			
		}
		catch ( JAXBException ex ) {
			throw new RuntimeException ( "Internal error while reading XML: " + ex.getMessage (), ex );
		}
	}
	
	public static <T> T unmarshal ( String xml, Class<? extends T> targetClass )
	{
		return unmarshal ( new ReaderInputStream ( new StringReader ( xml ), Charsets.UTF_8 ), targetClass, (Object[]) null );
	}

}
