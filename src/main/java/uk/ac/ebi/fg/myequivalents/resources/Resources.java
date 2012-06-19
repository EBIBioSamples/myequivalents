package uk.ac.ebi.fg.myequivalents.resources;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * 
 * TODO: Comment me!
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
	
	public EntityManagerFactory getEntityManagerFactory ()
	{
		if ( entityManagerFactory != null ) return entityManagerFactory;
		return entityManagerFactory = Persistence.createEntityManagerFactory ( "defaultPersistenceUnit" );
	}

	@Override
	protected void finalize () throws Throwable
	{
    if ( entityManagerFactory != null && entityManagerFactory.isOpen() )
    	entityManagerFactory.close();
	}
}
