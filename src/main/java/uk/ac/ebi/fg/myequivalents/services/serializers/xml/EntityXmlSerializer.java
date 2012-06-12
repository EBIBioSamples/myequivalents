package uk.ac.ebi.fg.myequivalents.services.serializers.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.ebi.fg.myequivalents.model.Entity;

final class EntityXmlSerializer
{
	private EntityXmlSerializer () {
	}

	public static Element convert2XML ( Entity entity, Document doc ) 
	{
		Element xentity = doc.createElement ( "entity" );
		xentity.setAttribute ( "service-name", entity.getService ().getName () );
		xentity.setAttribute ( "accession", entity.getAccession () );
		xentity.setAttribute ( "uri", entity.getURI () );
		return xentity;
	}
}
