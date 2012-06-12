package uk.ac.ebi.fg.myequivalents.services.serializers.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import uk.ac.ebi.fg.myequivalents.model.Describeable;

final class XmlSerializingUtils
{
	
	private XmlSerializingUtils () {
	}

	static Element convert2XML ( Describeable describeable, Document doc, String elementName )
	{
		Element xdesc = doc.createElement ( elementName );
		
		String description = describeable.getDescription ();
		if ( description != null ) {
			Element xdescription = doc.createElement ( "description" );
			Text xdescriptionValue = doc.createTextNode ( description );
			xdescription.appendChild ( xdescriptionValue );
			xdesc.appendChild ( xdescription );
		}
		
		setAttribute ( xdesc, "name", describeable.getName () );
		setAttribute ( xdesc, "title", describeable.getTitle () );
		
		return xdesc;
	}
	
	static void setAttribute ( Element element, String attrName, String value )
	{
		if ( value == null ) return;
		element.setAttribute ( attrName, value );
	}
}
