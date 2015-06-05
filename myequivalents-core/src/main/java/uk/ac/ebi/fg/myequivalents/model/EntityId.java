package uk.ac.ebi.fg.myequivalents.model;

/**
 * TODO: comment me!
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
	
	public EntityId ( String serviceName, String acc, String uri )
	{
		super ();
		this.serviceName = serviceName;
		this.acc = acc;
		this.uri = uri;
	}

	public EntityId ( Service service, String acc )
	{
		this ( service, acc, null );
	}
		
	public EntityId ( Service service, String acc, String uri )
	{
		super ();
		this.service = service;
		this.acc = acc;
		this.uri = uri;
	}

	
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
	
}
