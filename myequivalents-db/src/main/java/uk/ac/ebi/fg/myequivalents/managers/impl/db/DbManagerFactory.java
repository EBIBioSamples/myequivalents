/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.managers.impl.db;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
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
	protected static EntityManagerFactory entityManagerFactory;
	
	protected DbManagerFactory () {
	}
	
	/**
	 * Creates a factory based on the Hibernate properties (eg, connection to a DB) passes as parameter. These are normally
	 * defined via Spring, in the bean configuration file {@link Resources#MYEQ_MANAGER_FACTORY_FILE_NAME}. See the 
	 * documentation and the tests for further details.
	 *  
	 */
	public DbManagerFactory ( Properties hibernateProperties )
	{
		synchronized ( DbManagerFactory.class )
		{
			if ( entityManagerFactory == null )
			{
				// Spring scans JPA annotations across multiple .jars, very convenient, no persistence.xml needed
				LocalContainerEntityManagerFactoryBean springEmf = new LocalContainerEntityManagerFactoryBean ();
				springEmf.setPackagesToScan ( "uk.ac.ebi.fg.myequivalents.**.*" );
				springEmf.setPersistenceUnitName ( "myEquivalentsPersistenceUnit" );
				springEmf.setJpaDialect ( new HibernateJpaDialect () );
				springEmf.setPersistenceProviderClass ( HibernatePersistenceProvider.class );
				springEmf.setJpaProperties ( hibernateProperties );
				springEmf.afterPropertiesSet ();
				entityManagerFactory = springEmf.getObject ();
			}
		}
	}
	
	@Override
	public EntityMappingManager newEntityMappingManager ()
	{
		return new DbEntityMappingManager ( entityManagerFactory.createEntityManager () );
	}
	
	@Override
	public EntityMappingManager newEntityMappingManager ( String email, String apiPassword )
	{
		return new DbEntityMappingManager ( entityManagerFactory.createEntityManager (), email, apiPassword );
	}
	

	@Override
	public ServiceManager newServiceManager ()
	{
		return new DbServiceManager ( entityManagerFactory.createEntityManager () );
	}

	@Override
	public ServiceManager newServiceManager ( String email, String apiPassword )
	{
		return new DbServiceManager ( entityManagerFactory.createEntityManager (), email, apiPassword );
	}

	
	@Override
	public AccessControlManager newAccessControlManager ( String email, String apiPassword )
	{
		return new DbAccessControlManager ( entityManagerFactory.createEntityManager (), email, apiPassword );
	}

	@Override
	public AccessControlManager newAccessControlManagerFullAuth ( String email, String userPassword )
	{
		return new DbAccessControlManager ( entityManagerFactory.createEntityManager (), email, userPassword, true );
	}

	@Override
	public BackupManager newBackupManager ( String email, String apiPassword )
	{
		return new DbBackupManager ( entityManagerFactory.createEntityManager (), email, apiPassword );
	}
	
	public EntityManagerFactory getEntityManagerFactory ()
	{
		return entityManagerFactory;
	}


}
