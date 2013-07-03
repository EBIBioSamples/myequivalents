package uk.ac.ebi.fg.myequivalents.access_control.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

/**
 * A system user. This is used to manage a minimum form of access control and provenance.
 *
 * <dl><dt>date</dt><dd>Mar 1, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@Table( name = "acc_ctrl_user" )
public class User
{
  public static enum Role 
  {
  	VIEWER ( 0 ), EDITOR( 100 ), ADMIN ( 1000 );
  	
  	/**
  	 * The higher the level, the more rights you have. 
  	 */
  	private final int level;
  	private Role ( int level ) { this.level = level; }

  	/**
  	 * It tells you if this role is is equal to or higher than the one passed as parameter.
  	 */
  	public boolean hasPowerOf ( Role role ) { return this.level >= role.level; }
  }

	private String name;
	private String surname;
	private String email;
	private String passwordHash;
	private String notes;
  private Role role;
  private String apiPasswordHash;
	
	private static MessageDigest messageDigest = null;

	
	protected User () {
		super ();
	}

	public User ( String email )
	{
		super ();
		email = StringUtils.trimToNull ( email );
		
		if ( email == null )
			// TODO: proper exception
			throw new NullPointerException ( "Name cannot be empty" );
		this.setEmail ( email );
	}
	
	public User ( String email, String name, String surname, String passwordHash, String notes, Role role, String apiPasswordHash )
	{
		super ();
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.passwordHash = passwordHash;
		this.notes = notes;
		this.role = role;
		this.apiPasswordHash = apiPasswordHash;
	}
	
	public User ( User original ) {
		this ( 
			original.getEmail (), 
			original.getName (),
			original.getSurname (),
			original.getPasswordHash (), 
			original.getNotes (), 
			original.getRole (), 
			original.getApiPasswordHash () 
		);
	}
	
	@Index ( name = "user_n" )
	public String getName ()
	{
		return name;
	}

	public void setName ( String name )
	{
		this.name = name;
	}

	@Index ( name = "user_s" )
	public String getSurname ()
	{
		return surname;
	}

	public void setSurname ( String surname )
	{
		this.surname = surname;
	}

	@Id
	public String getEmail ()
	{
		return email;
	}

	protected void setEmail ( String email )
	{
		this.email = email;
	}

	/**
	 * The SHA1+Base64 encrypted version of the password for this user. This is used for user management operations, 
	 * {@link #getApiPasswordHash()} is used for API invocations. The hashed password is expected to have the same format 
	 * that {@link #hashPassword(String)} generates.
	 * 
	 */
	@Column ( columnDefinition = "char(27)", nullable = false )
	public String getPasswordHash ()
	{
		return passwordHash;
	}

	public void setPasswordHash ( String passwordHash )
	{
		this.passwordHash = passwordHash;
	}

	@Lob
	public String getNotes ()
	{
		return notes;
	}

	public void setNotes ( String notes )
	{
		this.notes = notes;
	}

	@Transient
	public Role getRole () {
		return role;
	}

	public void setRole ( Role role ) {
		this.role = role;
	}

	@Column ( name = "role", nullable = true ) // if null, no credential
	protected String getRoleAsString () {
		return this.role == null ? null : this.role.toString ();
	} 
	
	protected void setRoleAsString ( String role ) {
		this.role = role == null ? null : Role.valueOf ( role );
	}
	
	/**
	 * A SHA1+Base64 password to be used for API invocations. {@link #getPasswordHash()} is used for user management operations.
	 * The hashed password is expected to have the same format that {@link #hashPassword(String)} generates.	 * 
	 */
	@Column ( columnDefinition = "char(27)", nullable = false )
	public String getApiPasswordHash ()
	{
		return apiPasswordHash;
	}

	public void setApiPasswordHash ( String apiPasswordHash )
	{
		this.apiPasswordHash = apiPasswordHash;
	}
	
	public boolean hasPowerOf ( Role role ) {
		return this.getRole ().hasPowerOf ( role );
	}

	public boolean equals ( Object o )
	{
		if ( o == null ) return false;
		if ( this == o ) return true;
		if ( this.getClass () != o.getClass () ) return false;

		User that = (User) o;
		return this.getEmail ().equals ( that.getEmail () );
	}

	public int hashCode ()
	{
		return this.getEmail ().hashCode ();
	}

	/**
	 * Takes a clear password and converts it into a SHA1 code, then it encodes in a format suitable to be stored into a 
	 * relational database and manipulated through SQL statements. Currently we use Base64
	 */
	public static String hashPassword ( String clearPassword )
	{
		if ( messageDigest == null )
		{
			try {
				messageDigest = MessageDigest.getInstance ( "SHA1" );
			} 
			catch ( NoSuchAlgorithmException ex ) {
				throw new RuntimeException ( "Internal error, cannot get the SHA1 digester from the JVM", ex );
			}
		}
		
		// With 20 bytes as input, the BASE64 encoding is always a 27 character string, with the last character always equals
		// a padding '=', so we don't need the latter in this context
		return 
			DatatypeConverter.printBase64Binary ( messageDigest.digest ( clearPassword.getBytes () ) ).substring (0, 27);
	}
	
	/**
	 * Generates a random password of 32 bytes and converts it into Base64. This is used for auto-generation of e.g., 
	 * API passwords.
	 */
  public static String generateSecret ()
  {
		SecureRandom randomGen = new SecureRandom ();
		byte secret[] = new byte [ 32 ];
		randomGen.nextBytes ( secret );
		
		// Last char is always a padding '=' and we don't need it here
		return DatatypeConverter.printBase64Binary ( secret ).substring ( 0, 43 );
  }

}