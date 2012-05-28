package uk.ac.ebi.fg.myequivalents.test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.ClassRule;
import org.junit.rules.ExternalResource;


/**
 * An {@link EntityManagerFactory} provider to be used in test classes as {@link ClassRule} 
 * (please see JUnit documentation for details). Before starting the test methods in the test class using an instance
 * of this provider, the EM factory will be initialised and it will be closed after all the test class tests are run. 
 * 
 * This class connect to the DB that is specified via the defaultPersistenceUnit and whatever hibernate.properties that
 * is found via standard Hibernate configuration mechanisms.
 *
 * <dl><dt>date</dt><dd>Dec 13, 2011</dd></dl>
 * @author brandizi
 *
 */
public class TestEntityMgrFactoryProvider extends ExternalResource
{
	private EntityManagerFactory emf = null;

	@Override
	protected void before () throws Throwable
	{
		emf = Persistence.createEntityManagerFactory ( "defaultPersistenceUnit" );
	}

	/**
	 * Shuts down (closes) the {@link #entityManagerFactory} after all the tests in a test class have been run.
	 */
	@Override
	protected void after ()
	{
    if (emf != null && emf.isOpen())
    	emf.close();
	}
	
	public EntityManagerFactory getEntityManagerFactory () {
		return emf;
	}
}
