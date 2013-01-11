/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.managers.impl.db;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * Returns managers that are based on direct connection to a relational database. 
 * 
 * <dl><dt>date</dt><dd>Nov 26, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DbManagerFactory implements ManagerFactory
{
	private EntityManagerFactory entityManagerFactory;
	
	/**
	 * Creates a factory based on the Hibernate properties (eg, connection to a DB) passes as parameter. These are normally
	 * defined via spring, in the bean configuration file {@link Resources#MYEQ_MANAGER_FACTORY_FILE_NAME}. See the 
	 * documentation and the tests for further details.
	 *  
	 */
	public DbManagerFactory ( Properties hibernateProperties )
	{
		entityManagerFactory = Persistence.createEntityManagerFactory ( "defaultPersistenceUnit", hibernateProperties );
	}
	
	@Override
	public EntityMappingManager newEntityMappingManager ()
	{
		return new DbEntityMappingManager ( entityManagerFactory.createEntityManager () );
	}

	@Override
	public ServiceManager newServiceManager ()
	{
		return new DbServiceManager ( entityManagerFactory.createEntityManager ()  );
	}

	public EntityManagerFactory getEntityManagerFactory ()
	{
		return entityManagerFactory;
	}
}
