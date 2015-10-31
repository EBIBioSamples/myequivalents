package uk.ac.ebi.fg.myequivalents.model;

import static java.lang.System.out;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ExposedEntity;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.JAXBUtils;

/**
 * 
 * Simple tests about JAXB translation from XML to Java objects.
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UnmarshallingTest
{	
	
	/**
	 * serviceName is a read-only and shortcut getter in the superclass. If we want it to become a changeable property, 
	 * we need an extension and to use it for XML-loading purposes only. This test was a start point for the development 
	 * of the service loader. 
	 *
	 */
	@XmlRootElement ( name = "service" )
	@XmlAccessorType ( XmlAccessType.NONE )
	public static class MyService extends Service
	{
		private String repositoryName;

		protected MyService () {
			super ();
		}

		public MyService ( String name, String entityType, String title, String description ) {
			super ( name, entityType, title, description );
		}

		public MyService ( String name, String entityType ) {
			super ( name, entityType );
		}

		public MyService ( String name ) {
			super ( name );
		}

		@Override
		public String getRepositoryName () {
			return repositoryName;
		}

		@Override
		public void setRepositoryName ( String repositoryName ) {
			this.repositoryName = repositoryName;
		}
	}
	
	@XmlRootElement ( name = "services" )
	@XmlAccessorType ( XmlAccessType.FIELD )
	public static class ServiceCollection
	{
		@XmlElement ( name = "service" )
		public Set<MyService> services = new HashSet<MyService> ();
	}
	
	@Test
	public void testServiceUnmarshalling () throws Exception
	{
		String xml =
		"<services>\n" +
    "  <service uri-pattern='http://somewhere.in.the.net/testemdao/service1/someType1/$id'\n" + 
    "           entity-type='testemdao.someType1' title='A Test Service 1' name='test.testemdao.service1'>\n" +
    "    <description>The Description of a Test Service 1</description>\n" + 
    "  </service>\n" + 
    "  <service entity-type='testemdao.someType1' title='A Test Service 2' name='test.testemdao.service2'" +
    "           repository-name = 'repo1'>\n" +
    "    <description>The Description of a Test Service 2</description>\n" +
    "  </service>\n" +
    "  <service\n" +
    "           entity-type='testemdao.someType2' title='A Test Service 3' name='test.testemdao.service3'>\n" + 
    "    <description>The Description of a Test Service 3</description>\n" + 
    "  </service>\n" + 
    "</services>";

		/*xml =
    "<service uri-pattern='http://somewhere.in.the.net/testemdao/service1/someType1/$id'\n" + 
    "         entity-type='testemdao.someType1' title='A Test Service 1' name='test.testemdao.service1'>\n" +
    "  <description>The Description of a Test Service 1</description>\n" + 
    "</service>\n";*/
		
		
		JAXBContext context = JAXBContext.newInstance ( ServiceCollection.class );
		Unmarshaller u = context.createUnmarshaller ();
		ServiceCollection sset = (ServiceCollection) u.unmarshal ( new StringReader ( xml ) );
		out.println ( "size = " + sset.services.size () + ", Values:\n" );
		for ( Service s: sset.services )
			out.println ( s + "\n" );
		
		// TODO: assertions
	}

	@Test
	public void testEntityUnMarshalEntity ()
	{
		String xml = 
			"<entity xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
			+ "			 xsi:type='exposedEntity' accession='acc3'"
			+ "			 service-name='test.testweb.service6' />";
		
		Entity e = JAXBUtils.unmarshal ( xml, ExposedEntity.class );
		out.println ( "entity = " + e );
		
		// TODO: assertions
	}
	
	@Test
	public void testEntityUnMarshalBundle ()
	{
		String
		xml = "<bundle>"
				 + "  <entity accession='acc3' service-name='test.testweb.service6' uri='http://somewhere.in.the.net/testweb/service6/someType1/acc3'/>\n"
				 + "  <entity accession='acc4' service-name='test.testweb.service6' uri='http://somewhere.in.the.net/testweb/service6/someType1/acc4'/>\n"
				 + "  <entity accession='acc2' service-name='test.testweb.service8' uri='http://somewhere.else.in.the.net/testweb/service8/someType1/acc2'/>\n"
				+ "</bundle>\n";
		Bundle b = JAXBUtils.unmarshal ( xml, Bundle.class );
		out.println ( "Bundle:" );
		for ( Entity ei: b.getEntities () ) out.println ( "Bundle entity: " + ei );
		
		// TODO: assertions
	}

	
	@Test
	public void testEntityUnMarshalSearchResult ()
	{
		String xml = 
			 "<mappings>\n"
		 + "    <services>\n"
		 + "        <service xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='service' entity-type='testweb.someType1' uri-pattern='http://somewhere.in.the.net/testweb/service6/someType1/$id' name='test.testweb.service6' public-flag='true' title='A Test Service 6'/>\n"
		 + "        <service xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='service' entity-type='testweb.someType2' repository-name='test.testweb.addedRepo1' uri-pattern='http://somewhere.else.in.the.net/testweb/service8/someType1/$id' name='test.testweb.service8' public-flag='true' title='A Test Service 8'/>\n"
		 + "        <service xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='service' entity-type='testweb.someType7' repository-name='test.testweb.repo1' service-collection-name='test.testweb.serviceColl1' uri-pattern='http://somewhere.in.the.net/testweb/service7/someType1/$id' name='test.testweb.service7' public-flag='true' title='A Test Service 7'/>\n"
		 + "    </services>\n"
		 + "    <repositories>\n"
		 + "        <repository name='test.testweb.repo1' public-flag='true' title='Test Repo 1'/>\n"
		 + "        <repository name='test.testweb.addedRepo1' public-flag='true'/>\n"
		 + "    </repositories>\n"
		 + "    <service-collections>\n"
		 + "        <service-collection name='test.testweb.serviceColl1' public-flag='true' title='Test Service Collection 1'/>\n"
		 + "    </service-collections>\n"
		 + "    <bundles>\n"
		 + "        <bundle>\n"
		 + "            <entity accession='acc3' service-name='test.testweb.service6' uri='http://somewhere.in.the.net/testweb/service6/someType1/acc3'/>\n"
		 + "            <entity accession='acc4' service-name='test.testweb.service6' uri='http://somewhere.in.the.net/testweb/service6/someType1/acc4'/>\n"
		 + "				</bundle>\n"	
		 + "				<bundle>\n"	
		 + "            <entity accession='acc2' service-name='test.testweb.service8' uri='http://somewhere.else.in.the.net/testweb/service8/someType1/acc2'/>\n"
		 + "            <entity accession='acc1' service-name='test.testweb.service7' uri='http://somewhere.in.the.net/testweb/service7/someType1/acc1'/>\n"
		 + "            <entity accession='acc1' service-name='test.testweb.service6' uri='http://somewhere.in.the.net/testweb/service6/someType1/acc1'/>\n"
		 + "        </bundle>\n"
		 + "    </bundles>\n"
		 + "</mappings>\n";
		
		EntityMappingSearchResult sr = JAXBUtils.unmarshal ( xml, EntityMappingSearchResult.class );
		out.println ( "Search Result:\n" + sr );
		
		// TODO: assertions
	}
}
