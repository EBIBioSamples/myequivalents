package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;

/**
 * DB-based implementation of {@link ProvManagerFactory}. This gets an {@link EntityManagerFactory} initialised
 * based on Spring facilities, which allow to put together JPA annotations from multiple packages.
 *
 * <dl><dt>date</dt><dd>10 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbManagerFactory extends DbManagerFactory implements ProvManagerFactory
{
	public ProvDbManagerFactory ( Properties hibernateProperties )
	{
		super ( hibernateProperties );
	}

	@Override
	public ServiceManager newServiceManager ()
	{
		return new ProvDbServiceManager ( entityManagerFactory.createEntityManager () );
	}

	@Override
	public ServiceManager newServiceManager ( String email, String apiPassword )
	{
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
		return new DbProvRegistryManager ( entityManagerFactory.createEntityManager (), email, apiPassword );
	}

	@Override
	public BackupManager newBackupManager ( String email, String apiPassword )
	{
		return new ProvDbBackupManager ( entityManagerFactory.createEntityManager (), email, apiPassword );
	}
	
	
}
