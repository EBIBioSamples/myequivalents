package uk.ac.ebi.fg.myequivalents.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.myequivalents.utils.JaxbXmlAdapter;


/**
 * 
 * A top-level class for all classes having properties like 
 * {@link #getName() name} and {@link #getDescription() description}.
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
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
	
	@XmlAttribute ( name = "public-flag" )
	@Column ( name = "public_flag", nullable = true )
	private Boolean publicFlag = true;
	
	@XmlAttribute ( name = "release-date" )
	@XmlJavaTypeAdapter ( JaxbXmlAdapter.class )	
	@Column ( name = "release_date", nullable = true )
	private Date releaseDate = null;
	
	
	protected Describeable () {
	}

	
	/**
	 * name == null will generate an exception.
	 */
	public Describeable ( String name )
	{
		super ();
		name = StringUtils.trimToNull ( name );
		
		if ( name == null )
			// TODO: proper exception
			throw new NullPointerException ( "Name cannot be empty" );
		this.setName ( name );
	}
	
	
	/**
	 * name == null will generate an exception.
	 */
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
		
	
	public Boolean getPublicFlag () {
		return this.publicFlag;
	}

	public void setPublicFlag ( Boolean publicFlag ) {
		this.publicFlag = publicFlag;
	}
	
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
			? this.getReleaseDate () != null && ( this.releaseDate.before ( now ) || this.releaseDate.equals ( now ) ) 
			: this.publicFlag;
	}

	
	/**
	 * Equivalence is based on the name. Note that name == null is not supported, it should never be null
	 */
	public boolean equals ( Object o )
	{
		if ( o == null ) return false;
		if ( this == o ) return true;
		if ( this.getClass () != o.getClass () ) return false;

		Describeable that = (Describeable) o;
		return this.getName ().equals ( that.getName () );
	}
	
	/**
	 * Equivalence is based on the name. Note that name == null is not supported, it should never be null
	 */
	public int hashCode ()
	{
		return this.getName ().hashCode ();
	}
}
