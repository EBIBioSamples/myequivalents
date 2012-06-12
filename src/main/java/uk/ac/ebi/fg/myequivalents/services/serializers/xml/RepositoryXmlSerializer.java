package uk.ac.ebi.fg.myequivalents.services.serializers.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.ebi.fg.myequivalents.model.Repository;

final class RepositoryXmlSerializer
{
	private RepositoryXmlSerializer () {
	}

	public static Element convert2XML ( Repository repo, Document doc )
	{
		Element xrepo = XmlSerializingUtils.convert2XML ( repo, doc, "repository" );
		XmlSerializingUtils.setAttribute ( xrepo, "url", repo.getUrl () );
		
		return xrepo;		
	}

}
