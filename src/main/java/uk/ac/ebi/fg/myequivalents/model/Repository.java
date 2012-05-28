package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table( name = "repository" )
@org.hibernate.annotations.Table ( 
	appliesTo = "repository", 
	indexes = {
		@Index ( name = "repo_t", columnNames = "title" )
	}
)
public class Repository extends Describeable
{
	@Index( name = "repo_uri" )
	private String url;

	protected Repository () {
	}
	
	public Repository ( String name )
	{
		super ( name );
	}

	public Repository ( String name, String title, String description )
	{
		super ( name, title, description );
	}

	public String getUrl ()
	{
		return url;
	}

	public void setUrl ( String url )
	{
		this.url = url;
	}

}
