package uk.ac.ebi.fg.myequivalents.model;

import static uk.ac.ebi.fg.myequivalents.resources.Const.COL_LENGTH_S;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Index;

import uk.ac.ebi.fg.myequivalents.resources.Const;

/**
 * 
 * A service collection is a collection of {@link Service}s that share the same accessions. For instance, PUBMED and 
 * CiteXPlore both point at the same scientific publications with the same accession. Wikipedia and DPPedia is another
 * example. Service collections are used to serve implicit mappings, i.e., if (pubmed, 123) is linked to (wikipedia, 456), 
 * then (citeXplore, 123) and (dbpedia, 456) will belong to this same equivalence class (bundle) without the need to 
 * store these further relations.
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
		@Index ( name = "service_coll_t", columnNames = "title" ),
		@Index ( name = "service_coll_pub_flag", columnNames = "public_flag" ), // TODO: needed?! Do we need a combined one?
		@Index ( name = "service_coll_rel_date", columnNames = "release_date" )
	}
)
@XmlRootElement ( name = "service-collection" )
@XmlAccessorType ( XmlAccessType.NONE )
public class ServiceCollection extends Describeable
{
	@Column ( name = "entity_type", length = COL_LENGTH_S )
	@Index( name = "service_coll_et" )
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

	@XmlAttribute ( name = "entity-type" )
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
			"ServiceCollection { name: '%s', title: '%s', entity-type: '%s', description: '%.15s', public-flag: %s, release-date: %s }", 
			this.getName (), this.getTitle (), this.getEntityType (), getDescription (), this.getPublicFlag (), this.getReleaseDate ()
		);
	}

}
