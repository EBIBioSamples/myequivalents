package uk.ac.ebi.fg.myequivalents.services.serializers.xml;

import java.io.StringWriter;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.services.EntityMappingsResult;
import uk.ac.ebi.fg.myequivalents.services.EntityMappingsResult.BundleSet;

public final class EntityMappingResultsXmlSerializer
{

	public static String serialize2XML ( EntityMappingsResult result )
	{
		// TODO: wrap exceptions into XML
		// 
		
  	try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance ();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element mappings = doc.createElement ( "mappings" ); doc.appendChild ( mappings );
			Set<Service> services = result.getServices ();
			if ( services != null )
			{
				Element xservColl = doc.createElement ( "services" );
				
				for ( Service service: services )
					xservColl.appendChild ( ServiceXmlSerializer.convert2XML ( service, doc ) );
				mappings.appendChild ( xservColl );
			}
			
			Set<ServiceCollection> serviceCollections = result.getServiceCollections ();
			if ( serviceCollections != null ) 
			{
				Element xservCollColl = doc.createElement ( "service-collections" );
				
				for ( ServiceCollection serviceColl: serviceCollections )
					xservCollColl.appendChild ( ServiceCollectionXmlSerializer.convert2XML ( serviceColl, doc ) );
				mappings.appendChild ( xservCollColl );
			}
			
			Set<Repository> repositories = result.getRepositories ();
			if ( repositories != null )
			{
				Element xrepoColl = doc.createElement ( "repositories" );
				
				for ( Repository repo: repositories )
					xrepoColl.appendChild ( RepositoryXmlSerializer.convert2XML ( repo, doc ) );
				mappings.appendChild ( xrepoColl );
			}
			
			Element xbundles = doc.createElement ( "bundles" );
			for ( BundleSet bundleSet: result.getBundles () )
			{
				Element xbundle = doc.createElement ( "bundle" );
				for ( Entity entity: bundleSet.getBundle () )
					xbundle.appendChild ( EntityXmlSerializer.convert2XML ( entity, doc ) );
			}
			mappings.appendChild ( xbundles );
			
			// To String
			// 
			TransformerFactory transFactory = TransformerFactory.newInstance();
      Transformer trans = transFactory.newTransformer();
      trans.setOutputProperty ( OutputKeys.ENCODING, "UTF-8" );
      trans.setOutputProperty ( OutputKeys.CDATA_SECTION_ELEMENTS, "description" );
      
			StringWriter sw = new StringWriter ();
      StreamResult xStreamResult = new StreamResult ( sw );
      DOMSource xsource = new DOMSource ( doc );
      trans.transform ( xsource, xStreamResult );
      return sw.toString();			
		} 
  	catch ( ParserConfigurationException ex ) {
  		throw new RuntimeException ( "Internal error while generating a mapping result: " + ex.getMessage (), ex );
		} 
  	catch ( TransformerException ex ) {
  		throw new RuntimeException ( "Internal error while generating a mapping result: " + ex.getMessage (), ex );
		}
  }
}
