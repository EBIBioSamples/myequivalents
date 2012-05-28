package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table( name = "service_collection" )
@org.hibernate.annotations.Table ( 
	appliesTo = "service_collection", 
	indexes = {
		@Index ( name = "service_coll_t", columnNames = "title" )
	}
)
public class ServiceCollection extends Describeable
{
	@NotBlank
	@Column ( name = "entity_type" )
	@Index( name = "service_coll_et" )
	private String entityType;
	
	@ManyToOne
	@JoinColumn( name = "entity_collection_name" )
	@Index( name = "service_coll_ec" )
	private EntityCollection entityCollection;
	
	public ServiceCollection () {
		super ();
	}

	public ServiceCollection ( String name, String entityType )
	{
		super ( name );
		this.setEntityType ( entityType );
	}

	public ServiceCollection ( String name, String entityType, String title, String description )
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

	public EntityCollection getEntityCollection ()
	{
		return entityCollection;
	}

	public void setEntityCollection ( EntityCollection entityCollection )
	{
		this.entityCollection = entityCollection;
	}

}
