package uk.ac.ebi.fg.myequivalents.services.serializers.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

final class ServiceCollectionXmlSerializer
{
	private ServiceCollectionXmlSerializer () {
	}

	public static Element convert2XML ( ServiceCollection serviceColl, Document doc )
	{
		Element xserviceColl = XmlSerializingUtils.convert2XML ( serviceColl, doc, "service-collection" );
		XmlSerializingUtils.setAttribute ( xserviceColl, "entity-type", serviceColl.getEntityType () );
		
		return xserviceColl;		
	}

}
