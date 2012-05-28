package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang.StringUtils;

@MappedSuperclass
public abstract class Describeable
{
	@Id
	@Column( length = 100 )
	private String name;
	private String title;
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
	
}
