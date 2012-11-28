/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.webservices.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * <p>Gets the path to hibernate.properties from context.xml (set it as Parameter) and prepare Hibernate initialisation 
 * via such custom path. This is useful when sysops set restrictions on the application server. And no, we don't want
 * to use a data source, cause this would not allow you to set Hibernate parameters such as the dialect class.</p>
 * 
 * <p>You have to set the myequivalents.hibernate-properties-location for this to work, see 
 * main/resources/META-INF/context.xml</p>
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
		String hibPropLoc = sce.getServletContext ().getInitParameter ( "myequivalents.hibernate-properties-location" );
		if ( hibPropLoc != null )
			Resources.getInstance ().setHibernatePropertiesLocation ( hibPropLoc );
	}

}
