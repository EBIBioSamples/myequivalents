package uk.ac.ebi.fg.myequivalents.utils;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

/**
 * 
 * JAXB-related utils.
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class JAXBUtils
{
	private JAXBUtils () {}
	
	/**
	 * Converts source into XML, using the JAXB annotations defined in targetClass.
	 *  
	 */
	public static <TC, SO> String marshal ( SO source, Class<TC> targetClass )
	{
		StringWriter sw = new StringWriter ();
		
		try
		{
			JAXBContext context = JAXBContext.newInstance ( targetClass );
			Marshaller m = context.createMarshaller ();
			m.setProperty ( Marshaller.JAXB_FORMATTED_OUTPUT, true );
			m.setProperty ( Marshaller.JAXB_ENCODING, "UTF-8" );
			m.marshal ( source, sw );
		} 
		catch ( PropertyException ex ) {
			throw new RuntimeException ( "Internal error while generating the XML search result: " + ex.getMessage (), ex );
		} 
		catch ( JAXBException ex ) {
			throw new RuntimeException ( "Internal error while generating the XML search result: " + ex.getMessage (), ex );
		}

		return sw.toString ();
	}
}
