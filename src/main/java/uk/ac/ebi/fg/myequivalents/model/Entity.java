package uk.ac.ebi.fg.myequivalents.model;

import java.io.Serializable;

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

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 25, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Embeddable
@XmlRootElement ( name = "entity" )
@XmlAccessorType ( XmlAccessType.NONE )
public class Entity implements Serializable
{
	private static final long serialVersionUID = -3887901613707959679L;

	@ManyToOne( optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinColumn ( name = "service_name" )
	private Service service;
	
	@NotBlank
	@Column( length = 50 )
	@XmlAttribute
	private String accession;
	
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
		return this.service.getName ();
	}
	
	@Transient
	@XmlAttribute ( name = "uri" )
	public String getURI()
	{
		String uriPattern = this.service.getUriPattern ();
		if ( uriPattern == null ) return null;
		return uriPattern.replaceAll ( "\\$\\{accession\\}", this.getAccession () );
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
			"EntityMapping { service.name: '%s', accession: '%s' }", this.getService ().getName (), this.getAccession ()  
		);
	}

}
