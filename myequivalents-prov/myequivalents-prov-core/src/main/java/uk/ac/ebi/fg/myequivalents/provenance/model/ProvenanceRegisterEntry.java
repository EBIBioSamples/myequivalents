package uk.ac.ebi.fg.myequivalents.provenance.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotEmpty;

import uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter;

/**
 * 
 * The provenance register keeps track of which user issued which change operation.
 *
 * <dl><dt>date</dt><dd>31 Mar 2014</dd></dl>
 * @author Marco Brandizi
 *
 * TODO: The log is written in a separate transaction, after the operation is successful. We should put all inside the
 * same transaction.
 * 
 * TODO: Several operations are recorded (by the managers), independently on the fact it physically really occurred or not.
 * For instance, if you delete a non-existing service, the deletion operation is recorded anyway. We might want to change
 * this in future.
 * 
 */
@Entity
@Table ( name = "provenance_register" )
@XmlRootElement ( name = "provenance-entry" )
@XmlAccessorType ( XmlAccessType.NONE )
public class ProvenanceRegisterEntry
{
  private Long id;
	private String userEmail;
	private Date timestamp;
	private String operation;
	private List<ProvenanceRegisterParameter> parameters;
	
	protected ProvenanceRegisterEntry () {
	}
	
	
  public ProvenanceRegisterEntry ( String userEmail, Date timestamp, String operation, List<ProvenanceRegisterParameter> parameters )
	{
		this.userEmail = userEmail;
		this.timestamp = timestamp;
		this.operation = operation;
		this.parameters = parameters;
	}

  public ProvenanceRegisterEntry ( String userEmail, String operation, List<ProvenanceRegisterParameter> parameters )
	{
  	this ( userEmail, new Date (), operation, parameters );
	}

  public ProvenanceRegisterEntry ( String userEmail, Date timestamp, String operation )
	{
  	this ( userEmail, timestamp, operation, null );
	}

  public ProvenanceRegisterEntry ( String userEmail, String operation )
	{
  	this ( userEmail, operation, null );
	}

  
	/**
   * A database primary key for job registry entries.
   */
  @Id
  @GeneratedValue ( strategy = GenerationType.TABLE )
  public Long getId () {
    return id;
  }

  /**
   * You should never explicitly set this, Hibernate will handle the creation of this ID whenever a new object is saved.
   * 
   */
  protected void setId ( Long id ) 
  {
    this.id = id;
  }
	


	@NotEmpty
	@Index ( name = "prov_email" )
	@Column ( name = "user_email" )
	@XmlAttribute ( name = "user-email" )
	public String getUserEmail ()
	{
		return userEmail;
	}

	protected void setUserEmail ( String userEmail )
	{
		this.userEmail = userEmail;
	}
	

	@NotNull
	@Index ( name = "prov_ts" )
	@XmlAttribute ( name = "timestamp" )
	@XmlJavaTypeAdapter ( DateJaxbXmlAdapter.class )	
	public Date getTimestamp () {
		return timestamp;
	}
	
	protected void setTimestamp ( Date timestamp ) {
		this.timestamp = timestamp;
	}


	
  @ElementCollection
  @CollectionTable( name = "provenance_register_parameter", joinColumns = @JoinColumn ( name="prov_entry_id" ) )
  @OrderColumn ( name = "index" )
	@XmlElementWrapper( name = "parameters" )
	@XmlElement ( name = "parameter" )
  public List<ProvenanceRegisterParameter> getParameters ()
	{
		return parameters;
	}

	public void setParameters ( List<ProvenanceRegisterParameter> parameters )
	{
		this.parameters = parameters;
	}


	@NotEmpty
	@Index ( name = "prov_op" )
	@Column ( name = "operation" )
	@XmlAttribute ( name = "operation" )
	public String getOperation ()
	{
		return operation;
	}

	protected void setOperation ( String operation )
	{
		this.operation = operation;
	}


	@Override
  public boolean equals ( Object o ) 
  {
  	if ( o == null ) return false;
  	if ( this == o ) return true;
  	if ( this.getClass () != o.getClass () ) return false;
  	
    // The entity type
  	ProvenanceRegisterEntry that = (ProvenanceRegisterEntry) o;
    if ( this.getId () == null && that.getId () != null || !this.id.equals ( that.id ) ) return false; 
    if ( this.getOperation () == null && that.getOperation () != null || !this.operation.equals ( that.operation ) ) return false; 
    if ( this.getUserEmail () == null && that.getUserEmail () != null || !this.userEmail.equals ( that.userEmail ) ) return false; 
    if ( this.getTimestamp () == null && that.getTimestamp () != null || !this.timestamp.equals ( that.timestamp ) ) return false;
    return true;
  }
	
	@Override
	public int hashCode ()
	{
		int result = 1;
		result = 31 * result + ( ( this.getId () == null ) ? 0 : id.hashCode () );
		result = 31 * result + ( ( this.getOperation () == null ) ? 0 : operation.hashCode () );
		result = 31 * result + ( ( this.getUserEmail () == null ) ? 0 : userEmail.hashCode () );
		result = 31 * result + ( ( this.getTimestamp () == null ) ? 0 : timestamp.hashCode () );
		return result;
	}
  
  @Override
  public String toString() {
  	// TODO
  	return String.format ( 
  		"%s { id: %d, userEmail: '%s', timestamp: %s, operation: %s, parameters: '%s'",
  		this.getClass ().getSimpleName (), this.getId (), this.getUserEmail (), this.getTimestamp (), this.getOperation (),
  		this.getParameters () == null ? "null" : ArrayUtils.toString ( this.parameters )
  	);
  }

  
  public Set<ProvenanceRegisterParameter> containsParameters ( List<ProvenanceRegisterParameter> params )
  {
  	if ( params == null || params.isEmpty () ) return Collections.emptySet ();

  	Set<ProvenanceRegisterParameter> result = new HashSet<> ();
  	for ( ProvenanceRegisterParameter psearch: params )
  	{
  		if ( result.contains ( psearch ) ) continue;
  		String ptype = psearch.getValueType (), val = psearch.getValue (), xval = psearch.getExtraValue ();
  		for ( ProvenanceRegisterParameter param: this.getParameters () )
  		{
  			if ( ptype != null && !ptype.equals ( param.getValueType () ) ) continue;
  			if ( val != null && !val.equals ( param.getValue () ) ) continue;
  			if ( xval != null && !xval.equals ( param.getExtraValue () ) ) continue;
  			result.add ( param );
  		}
  	}
  	
  	return result;
  }
}
