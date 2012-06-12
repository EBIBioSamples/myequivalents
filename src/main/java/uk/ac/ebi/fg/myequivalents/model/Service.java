package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table( name = "service" )
@org.hibernate.annotations.Table ( 
	appliesTo = "service", 
	indexes = {
		@Index ( name = "service_t", columnNames = "title" )
	}
)
@XmlRootElement ( name = "service" )
@XmlAccessorType ( XmlAccessType.NONE )
public class Service extends Describeable
{
	@NotBlank
	@Column ( name = "entity_type" )
	@Index( name = "service_et" )
	@XmlAttribute ( name = "entity-type" )
	private String entityType;

	@Index( name = "service_up" )
	@XmlAttribute ( name = "uri-prefix" )
	private String uriPrefix;
	
	@XmlAttribute ( name = "uri-pattern" )
	private String uriPattern;
	
	@ManyToOne
	@JoinColumn( name = "repository_name" )
	@Index( name = "service_r" )
	private Repository repository;
	
	@ManyToOne
	@JoinColumn( name = "service_collection_name" )
	@Index( name = "service_c" )
	private ServiceCollection serviceCollection;
	
	protected Service () {
		super();
	}
	
	
	public Service ( String name )
	{
		super ( name );
	}

	public Service ( String name, String entityType )
	{
		super ( name );
		this.setEntityType ( entityType );
	}

	public Service ( String name, String entityType, String title, String description )
	{
		super ( name, title, description );
		this.setEntityType ( entityType );
	}

	public String getEntityType ()
	{
		return entityType;
	}

	protected void setEntityType ( String entityType )
	{
		this.entityType = entityType;
	}

	public String getUriPrefix ()
	{
		return uriPrefix;
	}

	public void setUriPrefix ( String uriPrefix )
	{
		this.uriPrefix = uriPrefix;
	}

	public String getUriPattern ()
	{
		return uriPattern;
	}

	public void setUriPattern ( String uriPattern )
	{
		this.uriPattern = uriPattern;
	}

	public Repository getRepository ()
	{
		return repository;
	}

	public void setRepository ( Repository repository )
	{
		this.repository = repository;
	}

	public ServiceCollection getServiceCollection ()
	{
		return serviceCollection;
	}

	public void setServiceCollection ( ServiceCollection serviceCollection )
	{
		this.serviceCollection = serviceCollection;
	}

	@Transient
	@XmlAttribute ( name = "repository-name" )
	public String getRepositoryName () {
		return getRepository () == null ? null : this.repository.getName (); 
	}

  /**
   * TODO: comment me (special method for JAXB)!
   */
	protected void setRepositoryName ( String repositoryName ) 
	{
		throw new UnsupportedOperationException ( 
			"Internal error: you cannot call Service.setRepositoryName(), this is here just to make JAXB annotations working. " +
			"You need to override this setter, if you have a reasonable semantics for it" );
	}
	
	@Transient
	@XmlAttribute ( name = "service-collection-name" )
	public String getServiceCollectionName () {
		return getServiceCollection () == null ? null : this.serviceCollection.getName ();  
	}

  /**
   * TODO: comment me (special method for JAXB)!
   */
	protected void setServiceCollectionName ( String serviceCollectionName ) 
	{
		throw new UnsupportedOperationException ( 
			"Internal error: you cannot call Service.setServiceCollectionName(), this is here just to make JAXB annotations working. " +
			"You need to override this setter, if you have a reasonable semantics for it" );
	}

	@Override
	public String toString ()
	{
		return String.format ( 
			"Service { name: '%s', title: '%s', entity-type: '%s', uri-pattern: '%s', uri-prefix: '%s', " +
			"description: '%.15s', service-collection: '%s', repository: '%s' }", 
			this.getName (), this.getTitle (), this.getEntityType (), this.getUriPattern (),
			this.getUriPrefix (), this.getDescription (), this.getServiceCollectionName (), this.getRepositoryName () );
	}
	
	
}
