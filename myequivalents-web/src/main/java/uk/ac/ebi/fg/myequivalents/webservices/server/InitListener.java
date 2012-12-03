/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.webservices.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * <p>Gets the path to the configuration from context.xml (set it as Parameter) and loads a configuration from
 * such custom path (e.g., loads hibernate.properties from it).</p>
 *  
 * <p>This is useful when sysops set restrictions on the application server. And no, we don't want
 * to use data sources, cause this would not allow you to set Hibernate parameters such as the dialect class.</p>
 * 
 * <p>You have to set the myequivalents.config-location for this to work. This is interpreted as a directory, where to
 * look for files like hibernate.properties. If such files are not found there, the usual standard lookup method is
 * used instead (e.g., the classpath). See also main/resources/META-INF/context.xml</p>
 * 
 * <dl><dt>date</dt><dd>Nov 28, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class InitListener implements ServletContextListener
{
	/**
	 * Does nothing
	 */
	@Override
	public void contextDestroyed ( ServletContextEvent sce )
	{
	}

	
	@Override
	public void contextInitialized ( ServletContextEvent sce )
	{
		String configLoc = sce.getServletContext ().getInitParameter ( "myequivalents.config-location" );
		if ( configLoc != null )
			Resources.getInstance ().setConfigLocation ( configLoc );
	}

}
