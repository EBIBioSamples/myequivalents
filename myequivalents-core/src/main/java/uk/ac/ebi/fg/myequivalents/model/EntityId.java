package uk.ac.ebi.fg.myequivalents.model;

/**
 * A class to represent the identity of an {@link Entity}, that is, a pair of service (or service name) + 
 * accession and/or URI. Both accession and URI can be provided to an EntityId, in order to ease further processing
 * (e.g., accession-based fetch and subsequent check of URI). 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 May 2015</dd>
 *
 */
public class EntityId
{
	private String serviceName;
	private String acc;
	private String uri;
	private Service service;

	public EntityId ( String serviceName, String acc )
	{
		this ( serviceName, acc, null );
	}
	
	/**
	 * Can be inititalised either with a service name (service is null in that case), or with a service (serviceName
	 * is taken from the service).
	 */
	public EntityId ( String serviceName, String acc, String uri )
	{
		super ();
		this.serviceName = serviceName;
		this.acc = acc;
		this.uri = uri;
	}

	/**
	 * @see #EntityId(String, String, String).
	 */
	public EntityId ( Service service, String acc )
	{
		this ( service, acc, null );
	}
		
	/**
	 * @see #EntityId(String, String, String).
	 */
	public EntityId ( Service service, String acc, String uri )
	{
		super ();
		this.service = service;
		this.acc = acc;
		this.uri = uri;
	}

	/**
	 * @see #EntityId(String, String, String).
	 */
	public String getServiceName ()
	{
		return service == null ? serviceName : service.getName ();
	}
	
	public String getAcc ()
	{
		return acc;
	}
	
	public String getUri ()
	{
		return uri;
	}

	public Service getService ()
	{
		return service;
	}

  public boolean equals ( Object o ) 
  {
  	if ( o == null ) return false;
  	if ( this == o ) return true;
  	if ( this.getClass () != o.getClass () ) return false;
  	
    // Compare accessions if both are non-null, use identity otherwise
  	EntityId that = (EntityId) o;

  	String serviceName = getServiceName ();
    if ( serviceName != null ? !serviceName.equals ( that.getServiceName () ) : that.getServiceName () != null) return false;
    if ( this.getAcc () != null ? !acc.equals ( that.getAcc () ) : that.getAcc () != null) return false;
    return !( this.getUri () != null ? !uri.equals ( that.getUri () ) : that.getUri () != null );
  }
  
  @Override
  public int hashCode()
  {
  	String serviceName = getServiceName ();
    int result = serviceName != null ? serviceName.hashCode() : 0;
    result = 31 * result + ( getAcc () != null ? this.acc.hashCode() : 0 );
    result = 31 * result + ( getUri () != null ? this.uri.hashCode() : 0 );
    return result;
  }

  @Override
  public String toString() 
  {
  	return String.format ( 
  		"%s { serviceName: '%s', acc: '%s', uri: '%s' }", 
  		this.getClass ().getSimpleName (), this.getServiceName (), this.getAcc (), this.getUri ()
  	);
  }
}
