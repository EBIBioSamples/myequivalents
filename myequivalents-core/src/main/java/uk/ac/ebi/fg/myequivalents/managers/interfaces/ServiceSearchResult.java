package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * This is the class used to format the responses returned by the {@link ServiceManager}. For instance, the REST-based
 * web service uses this class to format its output in XML (thanks to JAXB mappings).  
 *
 * This is also used in the {@link uk.ac.ebi.fg.myequivalents.webservices.server.ServiceWebService}, for requests 
 * that require authenticated users and multiple items as parameters. A trick like that is needed, because REST/Jersey
 * doesn't easily support multiple complex parameter in a service method.
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
	
	// TODO: remove
	private String authEmail, authApiPassword;

	
	public ServiceSearchResult () {
	}

	
	@XmlElementWrapper( name = "services" )
	@XmlElement ( name = "service", type = ExposedService.class )
	public Set<Service> getServices ()
	{
		return services;
	}

	public void setServices ( Set<Service> services )
	{
		if ( services == null ) this.services.clear (); else this.services = services;
	}

	
	/**
	 * Adds a service to this results and returns true if it was not already here. This is used by the managers that 
	 * returns a result (e.g., {@link DbServiceManager}) and should not be needed outside them.
	 */
	public boolean addService ( Service service )
	{
		return this.services.add ( service );
	}
	

	
	@XmlElementWrapper( name = "service-collections" )
	@XmlElement ( name = "service-collection" )
	public Set<ServiceCollection> getServiceCollections ()
	{
		return serviceCollections;
	}

	public void setServiceCollections ( Set<ServiceCollection> serviceCollections )
	{
		if ( serviceCollections == null ) this.serviceCollections.clear (); 
		else this.serviceCollections = serviceCollections;
	}

	/**
	 * Adds a service collection to this results and returns true if it was not already here. This is used by the 
	 * managers that returns a result (e.g., {@link DbServiceManager}) and should not be needed outside them.
	 */
	public boolean addServiceCollection ( ServiceCollection serviceCollection ) {
		return this.serviceCollections.add ( serviceCollection );
	}
	
	
	@XmlElementWrapper( name = "repositories" )
	@XmlElement ( name = "repository" )
	public Set<Repository> getRepositories ()
	{
		return repositories;
	}

	public void setRepositories ( Set<Repository> repositories )
	{
		if ( repositories == null ) this.repositories.clear ();
		else this.repositories = repositories;
	}
	
	
	/**
	 * Adds a repository to this results and returns true if it was not already here. This is used by the managers that 
	 * returns a result (e.g., {@link DbServiceManager}) and should not be needed outside them.
	 */
	public boolean addRepository ( Repository repository ) {
		return this.repositories.add ( repository );
	}

	/**
	 * When this class is used for web service requests (e.g., to store a set of {@link Service}), this contains the 
	 * credential of the user that is performing the request.
	 */
	@XmlAttribute ( name = "login" )
	public String getAuthEmail ()
	{
		return authEmail;
	}

	public void setAuthEmail ( String authEmail )
	{
		this.authEmail = authEmail;
	}

	/**
	 * When this class is used for web service requests (e.g., to store a set of {@link Service}), this contains the 
	 * credential of the user that is performing the request.
	 */
	@XmlAttribute ( name = "login-secret" )
	public String getAuthApiPassword ()
	{
		return authApiPassword;
	}

	public void setAuthApiPassword ( String authApiPassword )
	{
		this.authApiPassword = authApiPassword;
	}
	
	
	@Override
	public String toString ()
	{
		StringBuilder sb = new StringBuilder ( "ServiceSearchResult ( authEmail: '" );
		sb.append ( this.getAuthEmail () );
		sb.append ( "', authApiPassword: " );
		sb.append ( this.getAuthApiPassword () == null ? "'null'" : "***" );
		sb.append ( " {\n" );
		
		
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