package uk.ac.ebi.fg.myequivalents.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * The resources used by this application.
 *
 * <dl><dt>date</dt><dd>Jun 15, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class Resources
{
	private String configLocation = null;
	private static Resources instance = new Resources ();
	
	private Resources () {
	}

	public static Resources getInstance () {
		return instance;
	}

	/**
	 * This is where the application searches for all its configuration files, such as hibernate.properties. This may
	 * be set in different ways, such as a Java property or a web context parameter.
	 */
	public String getConfigLocation ()
	{
		return configLocation;
	}

	/**
	 * @see #getConfigLocation().
	 */
	public void setConfigLocation ( String configLocation )
	{
		this.configLocation = configLocation;
	}

}
