package uk.ac.ebi.fg.myequivalents.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;


/**
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
@org.hibernate.annotations.Table ( 
	appliesTo = "entity_mapping", 
	indexes = { 
		@Index ( name = "entity_mapping_s", columnNames = "service_name" ),
		@Index ( name = "entity_mapping_a", columnNames = "accession" )
	} 
)

/* Used in the DAO, improve speed dramatically, cause it saves a lot of parsing time */
@NamedQueries ({
	@NamedQuery ( name = "getPublicMappings", cacheable = true, query = 
	  "SELECT em FROM EntityMapping em, EntityMapping em1\n" +
		"WHERE em.bundle = em1.bundle\n" +
		"AND em1.service.name = :serviceName AND em1.accession = :accession\n" +
		// First of all the parameter entity must be public (or, transitively, one of its containers)
		"AND (\n" +
		"  ( em1.publicFlag = true OR em1.publicFlag IS NULL AND em1.releaseDate IS NOT NULL AND em1.releaseDate <= current_time() )\n" +
		"  OR em1.publicFlag IS NULL AND em1.releaseDate IS NULL\n" +
		"  AND em1.service IN (\n" +
		"    SELECT s FROM Service s WHERE ( s.publicFlag = true OR s.publicFlag IS NULL AND s.releaseDate IS NOT NULL AND s.releaseDate <= current_time() )\n" +
		"      OR s.publicFlag IS NULL AND s.releaseDate IS NULL AND s.repository IN (\n" +
		"        (SELECT r FROM Repository r WHERE ( r.publicFlag = true OR r.publicFlag IS NULL AND ( r.releaseDate IS NULL OR r.releaseDate <= current_time() ) ) )\n" +
		"      )\n" +
		"  )\n" +
		")\n" +
		// then, all the linked entities must be pub too 
		"AND (\n" +
		"  ( em.publicFlag = true OR em.publicFlag IS NULL AND em.releaseDate IS NOT NULL AND em.releaseDate <= current_time() )\n" +
		"  OR em.publicFlag IS NULL AND em.releaseDate IS NULL\n" +
		"  AND em.service IN (\n" +
		"    SELECT s FROM Service s WHERE ( s.publicFlag = true OR s.publicFlag IS NULL AND s.releaseDate IS NOT NULL AND s.releaseDate <= current_time() )\n" +
		"      OR s.publicFlag IS NULL AND s.releaseDate IS NULL AND s.repository IN (\n" +
		"        (SELECT r FROM Repository r WHERE ( r.publicFlag = true OR r.publicFlag IS NULL AND ( r.releaseDate IS NULL OR r.releaseDate <= current_time() ) ) )\n" +
		"      )\n" +
		"  )\n" +
		")" 
	),
	
	@NamedQuery ( name = "getAllMappings", cacheable = true, query = 
		"SELECT em FROM EntityMapping em, EntityMapping em1\n" +
		"WHERE em.bundle = em1.bundle\n" +
		"AND em1.service.name = :serviceName AND em1.accession = :accession"
	), 
	
	@NamedQuery ( name = "findPublicEntityMapping", cacheable = true, query =
		"SELECT em FROM EntityMapping em\n" +
		"WHERE em.service.name = :serviceName AND em.accession = :accession\n" +
		"AND (\n" +
		"  ( em.publicFlag = true OR em.publicFlag IS NULL AND em.releaseDate IS NOT NULL AND em.releaseDate <= current_time() )\n" +
		"  OR em.publicFlag IS NULL AND em.releaseDate IS NULL\n" +
		"  AND em.service IN (\n" +
		"    SELECT s FROM Service s WHERE ( s.publicFlag = true OR s.publicFlag IS NULL AND s.releaseDate IS NOT NULL AND s.releaseDate <= current_time() )\n" +
		"      OR s.publicFlag IS NULL AND s.releaseDate IS NULL AND s.repository IN (\n" +
		"        (SELECT r FROM Repository r WHERE ( r.publicFlag = true OR r.publicFlag IS NULL AND ( r.releaseDate IS NULL OR r.releaseDate <= current_time() ) ) )\n" +
		"      )\n" +
		"  )\n" +
		")"
	),
	
	@NamedQuery ( name = "findEntityMapping", cacheable = true, query =
		"SELECT em FROM EntityMapping em\n" +
		"WHERE em.service.name = :serviceName AND em.accession = :accession"
	),
	
	@NamedQuery ( name = "findPublicMappingsForTarget", cacheable = true, query =
		"SELECT DISTINCT em FROM EntityMapping em, EntityMapping em1\n" +
		"WHERE em.bundle = em1.bundle\n" + 
		"AND em1.service.name = :serviceName AND em1.accession = :accession\n" +
		"AND em.service.name = :targetServiceName\n" +
		// First of all the parameter entity must be public (or, transitively, one of its containers)
		"AND (\n" +
		"  ( em1.publicFlag = true OR em1.publicFlag IS NULL AND em1.releaseDate IS NOT NULL AND em1.releaseDate <= current_time() )\n" +
		"  OR em1.publicFlag IS NULL AND em1.releaseDate IS NULL\n" +
		"  AND em1.service IN (\n" +
		"    SELECT s FROM Service s WHERE ( s.publicFlag = true OR s.publicFlag IS NULL AND s.releaseDate IS NOT NULL AND s.releaseDate <= current_time() )\n" +
		"      OR s.publicFlag IS NULL AND s.releaseDate IS NULL AND s.repository IN (\n" +
		"        (SELECT r FROM Repository r WHERE ( r.publicFlag = true OR r.publicFlag IS NULL AND ( r.releaseDate IS NULL OR r.releaseDate <= current_time() ) ) )\n" +
		"      )\n" +
		"  )\n" +
		")\n" +
		// then, all the linked entities must be pub too 
		"AND (\n" +
		"  ( em.publicFlag = true OR em.publicFlag IS NULL AND em.releaseDate IS NOT NULL AND em.releaseDate <= current_time() )\n" +
		"  OR em.publicFlag IS NULL AND em.releaseDate IS NULL\n" +
		"  AND em.service IN (\n" +
		"    SELECT s FROM Service s WHERE ( s.publicFlag = true OR s.publicFlag IS NULL AND s.releaseDate IS NOT NULL AND s.releaseDate <= current_time() )\n" +
		"      OR s.publicFlag IS NULL AND s.releaseDate IS NULL AND s.repository IN (\n" +
		"        (SELECT r FROM Repository r WHERE ( r.publicFlag = true OR r.publicFlag IS NULL AND ( r.releaseDate IS NULL OR r.releaseDate <= current_time() ) ) )\n" +
		"      )\n" +
		"  )\n" +
		")"
	),
	
	@NamedQuery ( name = "findMappingsForTarget", cacheable = true, query = 
		"SELECT DISTINCT em FROM EntityMapping em, EntityMapping em1\n" +
		"WHERE em.bundle = em1.bundle\n" + 
		"AND em1.service.name = :serviceName AND em1.accession = :accession\n" +
		"AND em.service.name = :targetServiceName"
	)
})
public class EntityMapping
{
	@Id
	private Service service;
	
	@Id
	private String accession;
	
	/* TODO: Because we moved to UUIDs, this should be changed from 26 to 22, 
	 * but first we need a script to convert old hash-based IDs to UUIDs. 
	 */
	@Column ( columnDefinition = "char(26)", nullable = false, unique = false )
	@Index ( name = "entity_mapping_b" )
	private String bundle;

	@Column ( name = "public_flag", nullable = true, columnDefinition = "integer" )
	@Index ( name = "entity_mapping_pub_flag" )
	private Boolean publicFlag = true;
	
	@Column ( name = "release_date", nullable = true )
	@Index ( name = "entity_mapping_rel_date" )
	private Date releaseDate = null;
	
	
	protected EntityMapping () {
		super ();
	}

	public EntityMapping ( Service service, String accession, String bundle )
	{
		super ();
		if ( service == null )
			// TODO: proper exception
			throw new NullPointerException ( "service cannot be null" );
		if ( bundle == null )
			// TODO: proper exception
			throw new NullPointerException ( "bundle cannot be null" );
		
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

	/**
	 * @see {@link Entity#getURI()}. 
	 */
	@Transient
	public String getURI () {
		return this.getEntity ().getURI ();
	}
	
	public String getBundle ()
	{
		return this.bundle;
	}

	protected void setBundle ( String bundle )
	{
		this.bundle = bundle;
	}
	

	@XmlAttribute ( name = "public-flag" )
	public Boolean getPublicFlag ()
	{
		return publicFlag;
	}

	public void setPublicFlag ( Boolean publicFlag )
	{
		this.publicFlag = publicFlag;
	}

	@XmlAttribute ( name = "release-date" )
	public Date getReleaseDate ()
	{
		return releaseDate;
	}

	public void setReleaseDate ( Date releaseDate )
	{
		this.releaseDate = releaseDate;
	}

	@Transient
	public boolean isPublic ()
	{
		Date now = new Date ();
		return this.getPublicFlag () == null 
			? this.getReleaseDate ().before ( now ) || this.releaseDate.equals ( now ) 
			: this.publicFlag;
	}

	
	/**
	 * @return the entity mapping as an {@link Entity}, which is useful on occasions (mainly for implementing features, not
	 * much for myEquivalent clients). 
	 */
	@Transient
	public Entity getEntity ()
	{
		Entity result = new Entity ( this.getService (), this.getAccession () );
		result.setPublicFlag ( this.getPublicFlag () );
		result.setReleaseDate ( this.getReleaseDate () );
		return result;
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
