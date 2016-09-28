package uk.ac.ebi.fg.myequivalents.model;

import static uk.ac.ebi.fg.myequivalents.resources.Const.COL_LENGTH_S;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.validator.constraints.NotBlank;

import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.NullBooleanJaxbXmlAdapter;

/**
 * 
 * Entities are single units of information that can cross-reference to other entities. Examples of entities are: 
 * a biological sample, a scientific paper, the web page about a person. Entities are identified by an accession and
 * a service in the context of which the accession is unique. Another way to identify entities is by means of URIs, 
 * which are assumed to be composed by binding a place-holder in a URI pattern. The URI pattern is provided with by the    
 * service. 
 * 
 * <p/>An entity is effectively a resource in the RDF realm. We avoid the term resource in order to avoid confusion with
 * the same term used differently in the context of <a href = 'http://www.ebi.ac.uk/miriam/main/'>MIRAM</a> 
 * (entities are equivalent to MIRIAM's physical locations, while they use the word 'resource' to mean what we call 
 * services).    
 * 
 * <dl><dt>date</dt><dd>May 25, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Embeddable
@XmlRootElement ( name = "entity" )
@XmlAccessorType ( XmlAccessType.NONE )
public class Entity implements Serializable, MyEquivalentsModelMember
{
	private static final long serialVersionUID = -3887901613707959679L;

	@ManyToOne( optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH } )
	@JoinColumn ( name = "service_name" )
	private Service service;
	
	@NotBlank
	@Column( length = COL_LENGTH_S )
	private String accession;

	/**
	 * This must be transient, cause a specific implementation has to be given in {@link EntityMapping}. 
	 */
	@Transient
	private Boolean publicFlag = true;

	/**
	 * This must be transient, cause a specific implementation has to be given in {@link EntityMapping}. 
	 */
	@Transient
	private Date releaseDate = null;
	
	protected Entity () {
	}
	
	public Entity ( Service service, String accession )
	{
		this.service = service;
		this.accession = accession;
	}
	

	public Service getService ()
	{
		return service;
	}

	void setService ( Service service )
	{
		this.service = service;
	}

	@XmlAttribute
	public String getAccession ()
	{
		return accession;
	}

	void setAccession ( String accession )
	{
		this.accession = accession;
	}
	
	@Transient
	@XmlAttribute ( name = "service-name", required = true )
	public String getServiceName () {
		// We need the accessor in order to prevent this to be called before injection.
		return this.getService () == null ? null : this.service.getName ();
	}
	
  /**
   * This is only used with JAXB and implemented in specific sub-classes.
   */
	protected void setServiceName ( String serviceName ) 
	{
		throw new UnsupportedOperationException ( 
			"Internal error: you cannot call Entity.setServiceName(), this is here just to make JAXB annotations working. " +
			"You need to override this setter, if you have a reasonable semantics for it" );
	}
	
	/**
	 * Builds the URI by means of {@link #getAccession()} and 
	 * {@link #getService()}.{@link Service#getUriPattern() getUriPattern()}.
	 */
	@Transient
	@XmlAttribute ( name = "uri" )
	public String getURI()
	{
		return EntityIdResolver.buildUriFromAcc ( this.getAccession (), this.getService ().getUriPattern () );
	}

  /**
   * This is only used with JAXB and implemented in specific sub-classes.
   */
	protected void setURI ( String uri ) 
	{
		throw new UnsupportedOperationException ( 
			"Internal error: you cannot call Entity.setURI(), this is here just to make JAXB annotations working. " +
			"You need to override this setter, if you have a reasonable semantics for it" );
	}
	
	
	public void setPublicFlag ( Boolean publicFlag ) {
		this.publicFlag = publicFlag;
	}

	@XmlAttribute ( name = "public-flag" )
	@XmlJavaTypeAdapter ( NullBooleanJaxbXmlAdapter.class )	
	public Boolean getPublicFlag () {
		return this.publicFlag;
	}
	
	@XmlAttribute ( name = "release-date" )
	@XmlJavaTypeAdapter ( DateJaxbXmlAdapter.class )		
	public Date getReleaseDate ()
	{
		return releaseDate;
	}


	public void setReleaseDate ( Date releaseDate )
	{
		this.releaseDate = releaseDate;
	}

	/**
	 * @return {@link #getPublicFlag()}, if it's not null, else considers {@link #getReleaseDate()} if it's not null,
	 * finally, if no other access information is available returns {@link Service#isPublic()}. 
	 */
	@Transient
	public boolean isPublic ()
	{
		// Is it explicitly stated?
		if ( this.getPublicFlag () != null )
			return this.publicFlag;
		
		// Are you already released?
		if ( this.getReleaseDate () != null ) {
			Date now = new Date ();
			return this.releaseDate.before ( now ) || this.releaseDate.equals ( now );
		}
		
		// No info? Then refer to your container
		return this.getService () == null ? false : this.service.isPublic ();
	}

	
	@Override
	public boolean equals ( Object obj )
	{
		if ( this == obj ) return true;
		if ( !( obj instanceof Entity ) ) return false;
		Entity other = (Entity) obj;
		
		return 
			this.getServiceName ().equals ( other.getServiceName () ) 
			&& this.getAccession ().equals ( other.getAccession () );
	}
	
	@Override
	public int hashCode () {
		return 31 * this.getServiceName ().hashCode () + this.getAccession ().hashCode ();
	}

	@Override
	public String toString ()
	{
		return String.format ( 
			"%s { service.name: '%s', accession: '%s', public-flag: %s, release-date: %s, uri: '%s' }", 
				this.getClass ().getSimpleName (), 
				this.getServiceName (), this.getAccession (), this.getPublicFlag (), this.getReleaseDate (), this.getURI ()  
		);
	}

}
