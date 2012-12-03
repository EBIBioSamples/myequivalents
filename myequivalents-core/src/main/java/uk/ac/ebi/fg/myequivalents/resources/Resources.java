package uk.ac.ebi.fg.myequivalents.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * The resources used by this application, such as the {@link #getEntityManagerFactory() entity manager factory}, i.e., 
 * the connection to the database.
 *
 * <dl><dt>date</dt><dd>Jun 15, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class Resources
{
	private EntityManagerFactory entityManagerFactory = null;
	private String configLocation = null;
	private Logger log = LoggerFactory.getLogger ( Resources.class );
	private static Resources instance = new Resources ();
	
	private Resources () {
	}

	public static Resources getInstance () {
		return instance;
	}
	
	/**
	 * <p>The wrapper to the database. This works via Hibernate, so you need to put some hibernate.properties in the 
	 * Java classpath. See the Maven structure for examples.</p>
	 * 
	 * <p>You have the option to set a custom path for hibernate.properties, via {@link #setConfigLocation(String)} 
	 * This is used by the web service server, in order to allow one to play with restrictions sysops might set for 
	 * application servers.</p>
	 * 
	 * <p>If such a property is null, hibernate.properties will be searched on the classpath as usually.</p>
	 * 
	 * <p>Note that neither the setter for such property, nor this method are synchronized, since you're not supposed to
	 * use the setter from a multi-thread context.</p>
	 * 
	 * 
	 */
	public EntityManagerFactory getEntityManagerFactory ()
	{
		if ( entityManagerFactory != null ) return entityManagerFactory;
		try 
		{
			File hibFile = null;

			if ( configLocation != null )
			{
				hibFile = new File ( configLocation + "/hibernate.properties" );
				if ( !hibFile.exists () ) hibFile = null;
			}
			
			if ( hibFile == null ) {
				log.info ( "Loading Database/Hibernate Parameters from CLASSPATH" );
				return entityManagerFactory = Persistence.createEntityManagerFactory ( "defaultPersistenceUnit" );
			}
			
			log.info ( "Loading Database/Hibernate Parameters from '" + hibFile.getCanonicalPath () + "'" );
			Properties hprops = new Properties ();
			hprops.load ( new FileReader ( hibFile ) );
			return entityManagerFactory = Persistence.createEntityManagerFactory ( "defaultPersistenceUnit", hprops );
		} 
		catch ( FileNotFoundException ex ) {
			throw new RuntimeException ( "Hibernate properties file not found: '" + configLocation + "'" );
		} 
		catch ( IOException ex ) {
			throw new RuntimeException ( 
				"Error while initialising Hibernate from '" + configLocation + "': " + ex.getMessage (), ex );
		}
	}

	/**
	 * @see #getEntityManagerFactory().
	 */
	public String getConfigLocation ()
	{
		return configLocation;
	}

	/**
	 * @see #getEntityManagerFactory().
	 */
	public void setConfigLocation ( String configLocation )
	{
		this.configLocation = configLocation;
	}

	
	/**
	 * closes the {@link #entityManagerFactory}
	 */
	@Override
	protected void finalize () throws Throwable
	{
    if ( entityManagerFactory != null && entityManagerFactory.isOpen() )
    	entityManagerFactory.close();
	}
}
