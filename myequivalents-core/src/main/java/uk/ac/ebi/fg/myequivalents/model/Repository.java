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
 * A repository is a collection of {@link Service services}. For instance, ArrayExpress is a repository, able to
 * provide an experiment service (i.e., accessions and URIs about experiments) and an array design service. 
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
		@Index ( name = "repo_t", columnNames = "title" ),
		@Index ( name = "repo_pub_flag", columnNames = "public_flag" ), // TODO: needed?! Do we need a combined one?
		@Index ( name = "repo_rel_date", columnNames = "release_date" )
	}
)
@XmlRootElement ( name = "repository" )
@XmlAccessorType ( XmlAccessType.NONE )
public class Repository extends Describeable
{
	@Index( name = "repo_url" )
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

	@XmlAttribute ( name = "url" )
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
			"Repository { name: '%s', title: '%s', url: '%s', description: '%.15s', public-flag: %s, release-date: %s }", 
			this.getName (), this.getTitle (), this.getUrl (), this.getDescription (), this.getPublicFlag (), this.getReleaseDate ()
		);
	}

}
