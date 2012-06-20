package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * TODO: Comment me! For the moment see {@link Service}.
 *
 * <dl><dt>date</dt><dd>Jun 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
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

	@Override
	public String toString ()
	{
		return String.format ( 
			"Service { name: '%s', title: '%s', entity-type: '%s', description: '%.15s' }", 
			this.getName (), this.getTitle (), this.getEntityType (), getDescription ()
		);
	}

}
