package uk.ac.ebi.fg.myequivalents.provenance.model;

import static uk.ac.ebi.fg.myequivalents.resources.Const.COL_LENGTH_M;
import static uk.ac.ebi.fg.myequivalents.resources.Const.COL_LENGTH_S;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
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
import uk.ac.ebi.utils.security.IdUtils;

/**
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
  private String id;
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
   * A universal ID for job registry entries.
   * This is automatically created using {@link IdUtils#createCompactUUID()}. We do not use numerical auto-IDs, cause
   * we want to ensure cross-DB identifiers, even before an entry is persisted. 
   */
  @Id
  @Column ( length = 22 )
  public String getId () 
  {
  	if ( id == null ) this.setId ( IdUtils.createCompactUUID () );
  	return id;
  }

  /**
   * You should never explicitly set this, Hibernate will handle the creation of this ID whenever a new object is saved.
   * 
   */
  protected void setId ( String id ) 
  {
    this.id = id;
  }
	


	@NotEmpty
	@Index ( name = "prov_email" )
	@Column ( name = "user_email", length = COL_LENGTH_S )
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
  @OrderColumn ( name = "idx" )
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
	@Column ( name = "operation", length = COL_LENGTH_M )
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
  	
  	return this.getId ().equals ( that.getId () );
  }
	
	@Override
	public int hashCode ()
	{
		return this.getId ().hashCode ();
	}
  
  @Override
  public String toString() 
  {
  	return String.format ( 
  		"%s { id: %s, userEmail: '%s', timestamp: %s, operation: %s, parameters: '%s'",
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
