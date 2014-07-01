package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import java.util.Properties;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>10 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbManagerFactory extends DbManagerFactory implements ProvManagerFactory
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
			springEmf.setPersistenceProviderClass ( HibernatePersistenceProvider.class );
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

	
	@Override
	public EntityMappingManager newEntityMappingManager ()
	{
		return new ProvDbEntityMappingManager ( entityManagerFactory.createEntityManager () );
	}

	@Override
	public EntityMappingManager newEntityMappingManager ( String email, String apiPassword )
	{
		return new ProvDbEntityMappingManager ( entityManagerFactory.createEntityManager (), email, apiPassword );
	}

	@Override
	public AccessControlManager newAccessControlManager ( String email, String apiPassword )
	{
		return new ProvDbAccessControlManager ( entityManagerFactory.createEntityManager (), email, apiPassword );
	}

	@Override
	public AccessControlManager newAccessControlManagerFullAuth ( String email, String userPassword )
	{
		return new ProvDbAccessControlManager ( entityManagerFactory.createEntityManager (), email, userPassword, true );
	}

	@Override
	public ProvRegistryManager newProvRegistryManager ( String email, String apiPassword )
	{
		return new DbProvenanceManager ( entityManagerFactory.createEntityManager (), email, apiPassword );
	}
}
