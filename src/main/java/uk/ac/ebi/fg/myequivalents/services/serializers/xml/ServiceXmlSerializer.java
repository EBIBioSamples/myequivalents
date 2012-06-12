package uk.ac.ebi.fg.myequivalents.services.serializers.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

final class ServiceXmlSerializer
{
	private ServiceXmlSerializer () {
	}

	public static Element convert2XML ( Service service, Document doc )
	{
		Element xservice = XmlSerializingUtils.convert2XML ( service, doc, "service" );
		XmlSerializingUtils.setAttribute ( xservice, "entity-type", service.getEntityType () );
		XmlSerializingUtils.setAttribute ( xservice, "uri-prefix", service.getUriPrefix () );
		XmlSerializingUtils.setAttribute ( xservice, "uri-pattern", service.getUriPattern () );
		
		Repository repo = service.getRepository ();
		if ( repo != null ) xservice.setAttribute ( "repository-ref", repo.getName () );

		ServiceCollection serviceColl = service.getServiceCollection ();
		if ( serviceColl != null )
			xservice.setAttribute ( "service-collection-ref", serviceColl.getName () );
		
		return xservice;		
	}
	
}
