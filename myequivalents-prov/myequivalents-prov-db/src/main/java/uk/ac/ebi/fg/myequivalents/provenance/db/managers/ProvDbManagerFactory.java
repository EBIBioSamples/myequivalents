package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import java.util.Properties;

import org.hibernate.ejb.HibernatePersistence;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>10 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbManagerFactory extends DbManagerFactory
{
	private static LocalContainerEntityManagerFactoryBean springEmf = null;
		
	public ProvDbManagerFactory ( Properties hibernateProperties )
	{
		if ( springEmf == null ) 
		{
			springEmf = new LocalContainerEntityManagerFactoryBean ();
			springEmf.setPackagesToScan ( "uk.ac.ebi.fg.myequivalents.**.*" );
			springEmf.setPersistenceUnitName ( "myEquivalentsProvPersistenceUnit" );
			springEmf.setJpaDialect ( new HibernateJpaDialect () );
			springEmf.setJpaProperties ( hibernateProperties );
			springEmf.setPersistenceProviderClass ( HibernatePersistence.class );
			springEmf.afterPropertiesSet ();
		}
		
		this.entityManagerFactory = springEmf.getObject ();
	}

	@Override
	public ServiceManager newServiceManager ()
	{
		// TODO Auto-generated method stub
		return new ProvDbServiceManager ( entityManagerFactory.createEntityManager () );
	}

	@Override
	public ServiceManager newServiceManager ( String email, String apiPassword )
	{
		// TODO Auto-generated method stub
		return new ProvDbServiceManager ( entityManagerFactory.createEntityManager (),  email, apiPassword );
	}

	
}
