package uk.ac.ebi.fg.myequivalents.managers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.ebi.fg.myequivalents.model.Service;

/**
 * serviceName is a read-only and shortcut getter in the superclass. If we want it to become a changeable property, 
 * we need to use this extension for XML-loading purposes only.
 * 
 * You should not instantiate this class, it is automatically picked by the myequivalents managers.
 *  
 */
@XmlRootElement ( name = "service" )
@XmlAccessorType ( XmlAccessType.NONE )
public class ExposedService extends Service
{
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
	
	Service asService () 
	{
		Service result = new Service ( this.getName (), this.getEntityType (), this.getTitle (), this.getDescription () );
		result.setServiceCollection ( this.getServiceCollection () );
		result.setRepository ( this.getRepository () );
		result.setUriPattern ( this.getUriPattern () );
		result.setUriPrefix ( this.getUriPrefix () );
		return result;
	}
}
