package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
@XmlRootElement ( name = "service-collection" )
@XmlAccessorType ( XmlAccessType.NONE )
public class ServiceCollection extends Describeable
{
	@NotBlank
	@Column ( name = "entity_type" )
	@Index( name = "service_coll_et" )
	@XmlAttribute ( name = "entity-type" )
	private String entityType;
	
	@ManyToOne( cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE } )
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

	@Override
	public String toString ()
	{
		return String.format ( 
			"Service { name: '%s', title: '%s', entity-type: '%s', description: '%.15s' }", 
			this.getName (), this.getTitle (), this.getEntityType (), getDescription ()
		);
	}

}
