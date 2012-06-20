package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Index;

/**
 * 
 * TODO: Comment me! For the moment see {@link Service}.
 *
 * <dl><dt>date</dt><dd>Jun 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@Table( name = "repository" )
@org.hibernate.annotations.Table ( 
	appliesTo = "repository", 
	indexes = {
		@Index ( name = "repo_t", columnNames = "title" )
	}
)
@XmlRootElement ( name = "repository" )
@XmlAccessorType ( XmlAccessType.NONE )
public class Repository extends Describeable
{
	@Index( name = "repo_url" )
	@XmlAttribute ( name = "url" )
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
	
	@Override
	public String toString ()
	{
		return String.format ( 
			"Service { name: '%s', title: '%s', url: '%s', description: '%.15s' }", 
			this.getName (), this.getTitle (), this.getUrl (), getDescription ()
		);
	}

}
