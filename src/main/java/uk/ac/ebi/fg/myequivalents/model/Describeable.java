package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

@XmlRootElement
@MappedSuperclass
@XmlAccessorType ( XmlAccessType.NONE )
public abstract class Describeable
{
	@Id
	@Column( length = 100 )
	@XmlAttribute
	private String name;
	@XmlAttribute
	private String title;
	@XmlElement
	private String description;
	
	protected Describeable () {
	}

	
	public Describeable ( String name )
	{
		super ();
		name = StringUtils.trimToNull ( name );
		
		if ( name == null )
			// TODO: proper exception
			throw new NullPointerException ( "Name cannot be empty" );
		this.setName ( name );
	}
	
	public Describeable ( String name, String title, String description )
	{
		this ( name );
		this.setTitle ( title );
		this.setDescription ( description );
	}



	public String getName ()
	{
		return name;
	}

	protected void setName ( String name )
	{
		this.name = name;
	}

	public String getTitle ()
	{
		return title;
	}

	public void setTitle ( String title )
	{
		this.title = title;
	}

	public String getDescription ()
	{
		return description;
	}

	public void setDescription ( String description )
	{
		this.description = description;
	}
	
	public boolean equals ( Object o)
	{
		if ( this == o ) return true;
		if ( !(o instanceof Describeable ) ) return false;

		Describeable that = (Describeable) this;
		return this.getName ().equals ( that.getName () );
 		
	}
	
	public int hashCode ()
	{
		return this.getName ().hashCode ();
	}
}
