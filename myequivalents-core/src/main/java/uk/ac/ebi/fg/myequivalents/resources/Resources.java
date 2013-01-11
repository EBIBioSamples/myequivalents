package uk.ac.ebi.fg.myequivalents.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;


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
	public static final String MYEQ_MANAGER_FACTORY_FILE_NAME = "myeq-manager-config.xml"; 
	
	private String configLocation = null;
	private ApplicationContext springApplicationContext = null;
	private ManagerFactory myEqManagerFactory = null;
	
	private static Resources instance = new Resources ();
	
	private Logger log = LoggerFactory.getLogger ( Resources.class );

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

	/**
	 * Returns the manager factory used for this application, based on spring and the resource configuration file named 
   * {@value #MYEQ_MANAGER_FACTORY_FILE_NAME}. The returned factory is the bean named 'myEquivalentsManagerFactory' that
   * you defined in such file. The resource file is looked-up in the classpath or in {@link #getConfigLocation()}, if
   * that's not null.
   * 
	 */
	public ManagerFactory getMyEqManagerFactory ()
	{
		if ( myEqManagerFactory != null ) return myEqManagerFactory;
		return myEqManagerFactory = (ManagerFactory) getSpringApplicationContext().getBean ( "myEquivalentsManagerFactory" );
	}
	
	/**
	 * Get the spring application context from the {@value #MYEQ_MANAGER_FACTORY_FILE_NAME} spring file. The locations
	 * this file is looked for are described in {@link #getMyEqManagerFactory()}. 
	 */
	public ApplicationContext getSpringApplicationContext ()
	{
		if ( springApplicationContext != null ) return springApplicationContext;
		 
		String configLocation = Resources.getInstance ().getConfigLocation ();
		try 
		{
			String springFilePath = null;
			File springFile = null;
	
			if ( configLocation != null )
			{
				springFile = new File ( springFilePath = configLocation + "/" + MYEQ_MANAGER_FACTORY_FILE_NAME );
				if ( !springFile.exists () ) springFile = null;
			}
			
			if ( springFile == null ) {
				log.info ( "Loading MyEquivalents's Manager Configuration (" + MYEQ_MANAGER_FACTORY_FILE_NAME + ") from CLASSPATH" );
				return springApplicationContext = new ClassPathXmlApplicationContext ( MYEQ_MANAGER_FACTORY_FILE_NAME );
			}
			
			log.info ( "Loading MyEquivalents's Manager Configuration from '" + springFile.getCanonicalPath () + "'" );
			return springApplicationContext = new FileSystemXmlApplicationContext ( springFilePath );
			
		} 
		catch ( FileNotFoundException ex ) {
			throw new RuntimeException ( "MyEquivalents's Manager Configuration (" + MYEQ_MANAGER_FACTORY_FILE_NAME + ") not found at: '" + configLocation + "'" );
		} 
		catch ( IOException ex ) {
			throw new RuntimeException ( 
				"Error while initialising MyEquivalents's Manager Configuration (" + MYEQ_MANAGER_FACTORY_FILE_NAME + ") from '" + configLocation + "': " + ex.getMessage (), ex );
		}
		
	}
}
