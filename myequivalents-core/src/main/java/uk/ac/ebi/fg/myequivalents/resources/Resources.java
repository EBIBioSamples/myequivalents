package uk.ac.ebi.fg.myequivalents.resources;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
	
	private static Resources instance = new Resources ();
	
	private Resources () {
	}

	public static Resources getInstance () {
		return instance;
	}
	
	/**
	 * The wrapper to the database. This works via Hibernate, so you need to put some hibernate.properties in the 
	 * Java classpath. See the Maven structure for examples.
	 * 
	 */
	public EntityManagerFactory getEntityManagerFactory ()
	{
		if ( entityManagerFactory != null ) return entityManagerFactory;
		return entityManagerFactory = Persistence.createEntityManagerFactory ( "defaultPersistenceUnit" );
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
