package uk.ac.ebi.fg.myequivalents.model;

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

import static java.lang.System.out;

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
    "  <service uri-pattern='http://somewhere.in.the.net/testemdao/service1/someType1/${accession}'\n" + 
		"           uri-prefix='http://somewhere.in.the.net/testemdao/service2/'\n" + 
    "           entity-type='testemdao.someType1' title='A Test Service 1' name='test.testemdao.service1'>\n" +
    "    <description>The Description of a Test Service 1</description>\n" + 
    "  </service>\n" + 
    "  <service entity-type='testemdao.someType1' title='A Test Service 2' name='test.testemdao.service2'" +
    "           repository-name = 'repo1'>\n" +
    "    <description>The Description of a Test Service 2</description>\n" +
    "  </service>\n" +
    "  <service uri-prefix='http://somewhere-else.in.the.net/testemdao/service3/'\n" +
    "           entity-type='testemdao.someType2' title='A Test Service 3' name='test.testemdao.service3'>\n" + 
    "    <description>The Description of a Test Service 3</description>\n" + 
    "  </service>\n" + 
    "</services>";

		/*xml =
    "<service uri-pattern='http://somewhere.in.the.net/testemdao/service1/someType1/${accession}'\n" + 
		"         uri-prefix='http://somewhere.in.the.net/testemdao/service2/'\n" + 
    "         entity-type='testemdao.someType1' title='A Test Service 1' name='test.testemdao.service1'>\n" +
    "  <description>The Description of a Test Service 1</description>\n" + 
    "</service>\n";*/
		
		
		JAXBContext context = JAXBContext.newInstance ( ServiceCollection.class );
		Unmarshaller u = context.createUnmarshaller ();
		ServiceCollection sset = (ServiceCollection) u.unmarshal ( new StringReader ( xml ) );
		out.println ( "size = " + sset.services.size () + ", Values:\n" );
		for ( Service s: sset.services )
			out.println ( s + "\n" );
	}
}
