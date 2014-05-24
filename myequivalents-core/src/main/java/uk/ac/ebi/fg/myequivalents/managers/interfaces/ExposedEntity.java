/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Service;

/**
 * serviceName is a read-only and shortcut getter in the superclass. If we want it to become a changeable property, 
 * we need to use this extension for XML-loading purposes only.
 * 
 * Moreover, this redefines {@link #getURI()}/{@link #setURI(String)}, since the Service is not here after XML 
 * de-serialisation and hence we allow JAXB to store the calculated URI as physical value.
 *
 * <dl><dt>date</dt><dd>Oct 3, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ExposedEntity extends Entity
{
	private static final long serialVersionUID = 1964502702735603174L;

	private String serviceName, uri;

	protected ExposedEntity () {
		super ();
	}

	public ExposedEntity ( Service service, String accession ) {
		super ( service, accession );
	}
	
	
	@Override
	public String getServiceName () {
		return this.serviceName;
	}

	@Override
	protected void setServiceName ( String serviceName )
	{
		this.serviceName = serviceName;
	}

	@Override
	public String getURI ()
	{
		return this.uri;
	}

	@Override
	protected void setURI ( String uri )
	{
		this.uri = uri;
	}
}
