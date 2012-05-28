package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
public class Service extends Describeable
{
	@NotBlank
	@Column ( name = "entity_type" )
	@Index( name = "service_et" )
	private String entityType;

	@Index( name = "service_up" )
	private String uriPrefix;
	
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

}
