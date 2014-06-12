package uk.ac.ebi.fg.myequivalents.provenance.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.annotations.Index;

/*
 * TODO:
 * 
 * an operation is made of 
 * 
 * operation-name, including the main entity type, e.g., user.store, user.setRole, service.store
 * userEmail
 * timestamp
 * parameters
 * 
 * each parameter is made of
 * 
 * paramName, eg, cascade, service, entity
 * paramValue, eg, true, service1, service1:123
 * 
 */

/**
 * 
 * TODO: Comment me!
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
public class ProvenanceRegisterEntry
{
  private Long id;
	private String userEmail;
	private Date timestamp;
	private String operation;
	private List<ProvenanceRegistryParameter> parameters;
	
	protected ProvenanceRegisterEntry () {
	}
	
	
  public ProvenanceRegisterEntry ( String userEmail, Date timestamp, String operation, List<ProvenanceRegistryParameter> parameters )
	{
		this.userEmail = userEmail;
		this.timestamp = timestamp;
		this.operation = operation;
		this.parameters = parameters;
	}

  public ProvenanceRegisterEntry ( String userEmail, String operation, List<ProvenanceRegistryParameter> parameters )
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
	


	@NotNull
	@Index ( name = "prov_email" )
	@Column ( name = "user_email" )
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
	public Date getTimestamp () {
		return timestamp;
	}
	
	protected void setTimestamp ( Date timestamp ) {
		this.timestamp = timestamp;
	}
	


	
  @ElementCollection
  @CollectionTable( name = "provenance_register_parameter", joinColumns = @JoinColumn ( name="prov_entry_id" ) )
  public List<ProvenanceRegistryParameter> getParameters ()
	{
		return parameters;
	}



	public void setParameters ( List<ProvenanceRegistryParameter> parameters )
	{
		this.parameters = parameters;
	}


	@NotNull
	@Index ( name = "prov_op" )
	@Column ( name = "operation" )
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
    if ( this.getOperation () == null || that.getOperation () == null || !this.operation.equals ( that.operation ) ) return false; 
    if ( this.getUserEmail () == null || that.getUserEmail () == null || !this.userEmail.equals ( that.userEmail ) ) return false; 
    if ( this.getTimestamp () == null || that.getTimestamp () == null || !this.timestamp.equals ( that.timestamp ) ) return false;
    return true;
  }
	
	@Override
	public int hashCode ()
	{
		int result = 1;
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

}
