package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;


@Entity
@Table( name = "entity_mapping" )
public class EntityMapping
{
	/**
	 * All this indirection crap is needed to make Hibernate understand there is a composite primary key
	 */
	@Id
	private EntityId pk;

	@Index ( name = "entity_mapping_b" )
	@Column ( columnDefinition = "char(27)", nullable = false, unique = false )
	private String bundle;

	
	public EntityMapping () {
		super ();
	}

	public EntityMapping ( Service service, String accession, String bundle )
	{
		super ();
		pk = new EntityId ( service, accession );
		this.bundle = bundle;
	}

	public Service getService ()
	{
		return pk.getService ();
	}

	protected void setService ( Service service )
	{
		pk.setService ( service );
	}

	public String getAccession ()
	{
		return pk.getAccession ();
	}

	protected void setAccession ( String accession )
	{
		pk.setAccession ( accession );
	}

	
	public String getBundle ()
	{
		return this.bundle;
	}

	protected void setBundle ( String bundle )
	{
		this.bundle = bundle;
	}

}
