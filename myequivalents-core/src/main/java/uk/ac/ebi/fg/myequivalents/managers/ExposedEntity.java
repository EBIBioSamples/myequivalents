/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.managers;

import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Service;

/**
 * serviceName is a read-only and shortcut getter in the superclass. If we want it to become a changeable property, 
 * we need an extension and to use it for XML-loading purposes only.
 *
 * <dl><dt>date</dt><dd>Oct 3, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ExposedEntity extends Entity
{
	private static final long serialVersionUID = 1964502702735603174L;

	private String serviceName;

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
	
}
