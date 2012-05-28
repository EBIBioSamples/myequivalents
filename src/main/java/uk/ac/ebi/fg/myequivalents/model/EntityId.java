package uk.ac.ebi.fg.myequivalents.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 25, 2012</dd></dl>
 * @author brandizi
 *
 */
@Embeddable
class EntityId implements Serializable
{
	private static final long serialVersionUID = -3887901613707959679L;

	@ManyToOne( optional = false )
	@JoinColumn ( name = "service_name" )
	private Service service;
	
	@NotBlank
	@Column( length = 50 )
	private String accession;
	
	public EntityId ( Service service, String accession )
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
	
}
