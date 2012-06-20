package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**
 *
 * Used to associate an {@link Entity} to its equivalence class, i.e. a set of entities that cross-reference each other. 
 * We call such equivalence classes 'bundles', inspired the same terminonoly used in <a href = 'http://www.sameas.org/'>sameAs</a>. 
 *  
 * <dl><dt>date</dt><dd>Jun 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@javax.persistence.Entity
@IdClass ( value = Entity.class )
@Table( name = "entity_mapping" )
public class EntityMapping
{
	@Id
	private Service service;
	
	@Id
	private String accession;
	
	@Index ( name = "entity_mapping_b" )
	@Column ( columnDefinition = "char(27)", nullable = false, unique = false )
	private String bundle;

	
	public EntityMapping () {
		super ();
	}

	public EntityMapping ( Service service, String accession, String bundle )
	{
		super ();
		this.service = service;
		this.accession = accession;
		this.bundle = bundle;
	}

	public Service getService ()
	{
		return service;
	}

	protected void setService ( Service service )
	{
		this.service = service;
	}

	public String getAccession ()
	{
		return this.accession;
	}

	protected void setAccession ( String accession )
	{
		this.accession = accession;
	}

	
	public String getBundle ()
	{
		return this.bundle;
	}

	protected void setBundle ( String bundle )
	{
		this.bundle = bundle;
	}

	@Transient
	public Entity getEntity ()
	{
		return new Entity ( this.getService (), this.getAccession () );
	}

	@Override
	public boolean equals ( Object o )
	{
		if ( o == null ) return false;
		if ( this == o ) return true;
		if ( this.getClass () != o.getClass () ) return false;
		EntityMapping that = (EntityMapping) o;
		
		return 
			this.getBundle ().equals ( that.getBundle () ) 
			&& this.getService ().getName ().equals ( that.getService ().getName () )
			&& this.getAccession ().equals ( that.getAccession () );
	}

	@Override
	public int hashCode ()
	{
		return 
			31^2 * this.getBundle ().hashCode () 
			+ 31 * this.getService ().getName ().hashCode () 
			+ this.getAccession ().hashCode ();
	}

	@Override
	public String toString ()
	{
		return String.format ( 
			"EntityMapping { service.name: '%s', accession: '%s', bundle: '%s' }", 
			this.getService ().getName (), this.getAccession (), this.getBundle () 
		);
	}
	
}
