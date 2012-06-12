package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table( name = "entity_collection" )
@org.hibernate.annotations.Table ( 
	appliesTo = "entity_collection", 
	indexes = {
		@Index ( name = "entity_coll_t", columnNames = "title" )
	}
)
public class EntityCollection extends Describeable
{
	@NotBlank
	@Column ( name = "entity_type" )
	@Index( name = "entity_coll_et" )
	private String entityType;

	protected EntityCollection () {
		super();
	}

	public EntityCollection ( String name, String entityType )
	{
		super ( name );
		this.setEntityType ( entityType );
	}

	public EntityCollection ( String name, String entityType, String title, String description )
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
	
}
