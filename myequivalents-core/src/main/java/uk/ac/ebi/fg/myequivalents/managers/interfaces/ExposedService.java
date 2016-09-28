package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.ebi.fg.myequivalents.model.Service;

/**
 * This is used to return {@link Service} results via interfaces like the web service. 
 * 
 * This is necessary because serviceName is a read-only and has a shortcut getter in the superclass. If we want it to
 * become a modifiable property, we need to use this extension for XML-loading purposes only.
 * 
 * You should not use this class directly, it is automatically picked by the myequivalents managers.
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
	
	/**
	 * Used internally, to convert this exposed service into a {@link Service} object. This is used by 
	 * {@link DbServiceManager} and normally you should not need it.
	 */
	public Service asService () 
	{
		Service result = new Service ( this.getName (), this.getEntityType (), this.getTitle (), this.getDescription () );
		result.setServiceCollection ( this.getServiceCollection () );
		result.setRepository ( this.getRepository () );
		result.setUriPattern ( this.getUriPattern () );
		result.setPublicFlag ( this.getPublicFlag () );
		result.setReleaseDate ( this.getReleaseDate () );
		return result;
	}
}
