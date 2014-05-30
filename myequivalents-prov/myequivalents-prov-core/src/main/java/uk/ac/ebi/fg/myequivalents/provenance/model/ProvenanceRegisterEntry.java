package uk.ac.ebi.fg.myequivalents.provenance.model;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

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
	public static enum Operation { DELETE, STORE };
	public static enum EntryType
	{ 
		REPOSITORY ( Repository.class.getSimpleName () ),
		SERVICE ( Service.class.getSimpleName () ),
		SERVICE_COLLECTION ( ServiceCollection.class.getSimpleName () ),
		ENTITY_MAPPING ( EntityMapping.class.getSimpleName () );
		
		private String className;
		private EntryType ( String className ) { this.className = className; }
		public String toString () { return this.className; }
		
		public static EntryType fromClass ( Class<?> clazz )
		{
			if ( Repository.class.isAssignableFrom ( clazz ) ) return REPOSITORY;
			else if ( Service.class.isAssignableFrom ( clazz ) ) return SERVICE;
			else if ( ServiceCollection.class.isAssignableFrom ( clazz ) ) return SERVICE_COLLECTION;
			else if ( EntityMapping.class.isAssignableFrom ( clazz ) ) return ENTITY_MAPPING;
			else throw new IllegalArgumentException ( String.format ( 
				"%s can only accept %s", EntryType.class.getSimpleName (), Arrays.asList ( EntryType.values () ) ));
		}
	};
	
  private Long id;
	private String entryId;
	private EntryType entryType;
	private Operation operation;
	private String userEmail;
	private Date timestamp;
	private String topOperation;
	
	protected ProvenanceRegisterEntry () {
		super ();
	}

	public ProvenanceRegisterEntry ( String entryId, EntryType entryType, Operation operation, String userEmail, Date timestamp )
	{
		super ();
		this.entryId = entryId;
		this.entryType = entryType;
		this.operation = operation;
		this.userEmail = userEmail;
		this.timestamp = timestamp;
	}

	/**
	 * Uses the current time as timestamp.
	 */
	public ProvenanceRegisterEntry ( String entryId, EntryType entryType, Operation operation, String userEmail )
	{
		this ( entryId, entryType, operation, userEmail, new Date () );
	}


	public ProvenanceRegisterEntry ( Describeable entry, Operation operation, String userEmail, Date timestamp )
	{
		super ();
		if ( entry != null ) 
		{
			this.entryId = entry.getName ();
			this.entryType = EntryType.fromClass ( entry.getClass () );
		}
		this.operation = operation;
		this.userEmail = userEmail;
		this.timestamp = timestamp;
	}
	
	public ProvenanceRegisterEntry ( Describeable entry, Operation operation, String userEmail )
	{
		this ( entry, operation, userEmail, new Date () );
	}

	public ProvenanceRegisterEntry ( EntityMapping entry, Operation operation, String userEmail, Date timestamp )
	{
		super ();
		if ( entry != null ) 
		{
			// TODO: check nulls?
			this.entryId = entry.getService ().getName () + ":" + entry.getAccession ();
			this.entryType = EntryType.fromClass ( entry.getClass () );
		}
		this.operation = operation;
		this.userEmail = userEmail;
		this.timestamp = timestamp;
	}

	public ProvenanceRegisterEntry ( EntityMapping entry, Operation operation, String userEmail )
	{
		this ( entry, operation, userEmail, new Date () );
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
	
	
	/**
	 * A string tag to identify the entity that was deleted. We recommend you use {@link Class#getSimpleName()} for this.
	 * The biosd model has no two classes with the same name and from different packages at the moment and we commit to avoid this
	 * in future too.
	 */
	@Index ( name = "prov_entry_type" )
	@Column ( name = "entry_type" )
	@Enumerated ( EnumType.STRING )
	public EntryType getEntryType ()
	{
		return this.entryType;
	}

	protected void setEntryType ( EntryType entryType )
	{
		this.entryType = entryType;
	}

	@Index ( name = "prov_entry_id" )
	public String getEntryId ()
	{
		return this.entryId;
	}

	protected void setEntryId ( String entryId )
	{
		this.entryId = entryId;
	}

	@NotNull
	@Index( name = "prov_op")
	@Enumerated ( EnumType.STRING )
	public Operation getOperation () {
		return operation;
	}

	
	protected void setOperation ( Operation operation ) {
		this.operation = operation;
	}
	
	@NotNull
	@Index ( name = "prov_email" )
	@Column ( name = "user_email" )
	public String getUserEmail ()
	{
		return userEmail;
	}

	public void setUserEmail ( String userEmail )
	{
		this.userEmail = userEmail;
	}
	
	@Index ( name = "prov_top_op" )
	@Column ( name = "top_operation", length = 1000 )
	public String getTopOperation ()
	{
		return topOperation;
	}

	public void setTopOperation ( String topOperation )
	{
		this.topOperation = topOperation;
	}


	@NotNull
	@Index ( name = "prov_ts" )
	public Date getTimestamp () {
		return timestamp;
	}
	
	protected void setTimestamp ( Date timestamp ) {
		this.timestamp = timestamp;
	}
	
	
	
  @Override
  public boolean equals ( Object o ) 
  {
  	if ( o == null ) return false;
  	if ( this == o ) return true;
  	if ( this.getClass () != o.getClass () ) return false;
  	
    // The entity type
  	ProvenanceRegisterEntry that = (ProvenanceRegisterEntry) o;
    if ( this.getEntryType () == null || that.getEntryType () == null || !this.entryType.equals ( that.entryType ) ) return false; 
    if ( this.getEntryId () == null || that.getEntryId () == null || !this.entryId.equals ( that.entryId ) ) return false; 
    if ( this.getOperation () == null || that.getOperation () == null || !this.operation.equals ( that.operation ) ) return false; 
    if ( this.getUserEmail () == null || that.getUserEmail () == null || !this.userEmail.equals ( that.userEmail ) ) return false; 
    if ( this.getTimestamp () == null || that.getTimestamp () == null || !this.timestamp.equals ( that.timestamp ) ) return false;
    return true;
  }
	
	@Override
	public int hashCode ()
	{
		int result = 1;
		result = 31 * result + ( ( this.getEntryType () == null ) ? 0 : entryType.hashCode () );
		result = 31 * result + ( ( this.getEntryId () == null ) ? 0 : entryId.hashCode () );
		result = 31 * result + ( ( this.getOperation () == null ) ? 0 : operation.hashCode () );
		result = 31 * result + ( ( this.getUserEmail () == null ) ? 0 : userEmail.hashCode () );
		result = 31 * result + ( ( this.getTimestamp () == null ) ? 0 : timestamp.hashCode () );
		return result;
	}
  
  @Override
  public String toString() {
  	return String.format ( 
  		"%s { id: %d, entryType: '%s', entryId: '%s', operation: %s, userEmail: '%s', timestamp: %6$tF/%6$tT.%6$tL, topOperation: '%s'", 
  		this.getClass ().getSimpleName (), this.getId (), this.getEntryType (), this.getEntryId (), 
  		this.getOperation (), this.getUserEmail (),	this.getTimestamp () 
  	);
  }

}
