package uk.ac.ebi.fg.myequivalents.managers;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.ebi.fg.myequivalents.model.Service;

/**
 * serviceName is a read-only and shortcut getter in the superclass. If we want it to become a changeable property, 
 * we need an extension and to use it for XML-loading purposes only.
 * 
 */
@XmlRootElement ( name = "service" )
@XmlAccessorType ( XmlAccessType.NONE )
public class ExposedService extends Service
{
	@XmlRootElement ( name = "services" )
	@XmlAccessorType ( XmlAccessType.FIELD )
	public static class ServiceSearchResult
	{
		@XmlElement ( name = "service" )
		private Set<Service> services = new HashSet<Service> ();

		protected ServiceSearchResult () {}
		
		ServiceSearchResult ( Set<Service> services ) {
			this.services = services;
		}
		
		public Set<Service> getServices ()
		{
			return services;
		}

		protected void setServices ( Set<Service> services )
		{
			this.services = services;
		}
	}

	private String repositoryName, serviceCollectionName;

	protected ExposedService () {
		super ();
	}

	public ExposedService ( String name, String entityType, String title, String description ) {
		super ( name, entityType, title, description );
	}

	public ExposedService ( String name, String entityType ) {
		super ( name, entityType );
	}

	public ExposedService ( String name ) {
		super ( name );
	}

	@Override
	public String getRepositoryName () {
		return repositoryName;
	}

	@Override
	protected void setRepositoryName ( String repositoryName ) {
		this.repositoryName = repositoryName;
	}

	@Override
	public String getServiceCollectionName () {
		return serviceCollectionName;
	}

	@Override
	protected void setServiceCollectionName ( String serviceCollectionName )
	{
		this.serviceCollectionName = serviceCollectionName;
	}
	
}
