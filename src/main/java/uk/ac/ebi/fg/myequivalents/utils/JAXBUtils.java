package uk.ac.ebi.fg.myequivalents.utils;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

public class JAXBUtils
{
	private JAXBUtils () {}
	
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
			// TODO: Return error XML back
			throw new RuntimeException ( "Internal error while generating the XML search result: " + ex.getMessage () );
		} 
		catch ( JAXBException ex ) {
			throw new RuntimeException ( "Internal error while generating the XML search result: " + ex.getMessage () );
		}

		return sw.toString ();
	}
}
