package uk.ac.ebi.fg.myequivalents.managers;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * This is the class used to format the responses returned by the {@link ServiceManager}. For instance, the SOAP-based
 * web service uses this class to format its output in XML (thanks to JAXB mappings).  
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@XmlRootElement ( name = "service-items" )
@XmlAccessorType ( XmlAccessType.NONE )
@XmlType ( name = "", propOrder = { "repositories", "serviceCollections", "services" } )
public class ServiceSearchResult
{
	private Set<Service> services = new HashSet<Service> ();
	private Set<ServiceCollection> serviceCollections = new HashSet<ServiceCollection> ();
	private Set<Repository> repositories = new HashSet<Repository> ();
	
	public ServiceSearchResult () {
	}

	
	@XmlElementWrapper( name = "services" )
	@XmlElement ( name = "service", type = ExposedService.class )
	public Set<Service> getServices ()
	{
		return services;
	}

	void setServices ( Set<Service> services )
	{
		this.services = services;
	}

	boolean addService ( Service service ) {
		return this.services.add ( service );
	}
	
	
	
	@XmlElementWrapper( name = "service-collections" )
	@XmlElement ( name = "service-collection" )
	public Set<ServiceCollection> getServiceCollections ()
	{
		return serviceCollections;
	}

	void setServiceCollections ( Set<ServiceCollection> serviceCollections )
	{
		this.serviceCollections = serviceCollections;
	}

	boolean addServiceCollection ( ServiceCollection serviceCollection ) {
		return this.serviceCollections.add ( serviceCollection );
	}
	
	
	@XmlElementWrapper( name = "repositories" )
	@XmlElement ( name = "repository" )
	public Set<Repository> getRepositories ()
	{
		return repositories;
	}

	void setRepositories ( Set<Repository> repositories )
	{
		this.repositories = repositories;
	}
	
	boolean addRepository ( Repository repository ) {
		return this.repositories.add ( repository );
	}

	
	@Override
	public String toString ()
	{
		StringBuilder sb = new StringBuilder ( "ServiceSearchResult {\n" );
		
		sb.append ( "  services: {\n" );
		for ( Service service: services )
			sb.append ( "    " ).append ( service.toString () + "\n");
		sb.append ( "  }\n" );
		
		sb.append ( "  repositories: {\n" );
		for ( Repository repo: repositories )
			sb.append ( "    " ).append ( repo.toString () );
		sb.append ( "  }\n" );

		sb.append ( "  service-collections: {\n" );
		for ( ServiceCollection sc: serviceCollections )
			sb.append ( "    " ).append ( sc.toString () );
		sb.append ( "  }\n" );

		sb.append ( "}\n" );
		
		return sb.toString ();		
	}

}