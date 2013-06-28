package uk.ac.ebi.fg.myequivalents.managers.impl.db;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.DescribeableDAO;
import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * The relational version of {@link AccessControlManager}.
 *
 * <dl><dt>date</dt><dd>Mar 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DbAccessControlManager extends DbMyEquivalentsManager implements AccessControlManager
{
	
	public DbAccessControlManager ( EntityManager entityManager, String email, String apiPassword ) {
		this ( entityManager, email, apiPassword, false);
	}

	public DbAccessControlManager ( EntityManager entityManager, String email, String password, boolean isUserPassword ) 
	{
		super ( entityManager );
		if ( isUserPassword ) this.setFullAuthenticationCredentials ( email, password );
		else this.setAuthenticationCredentials ( email, password );
	}
	
	@Override
	public User setFullAuthenticationCredentials ( String email, String userPassword ) throws SecurityException
	{
		return setAuthenticationCredentials ( email, userPassword, true );
	}


	@Override
	public void storeUser ( User user )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.store ( user );
		ts.commit ();
	}

	/**
	 * TODO: ExposedUser?
	 */
	@Override
	public User getUser ( String email ) {
		return userDao.findByEmail ( email );
	}

	@Override
	public void setRole ( String email, Role role ) 
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.setRole ( email, role );
		ts.commit ();
	}

	@Override
	public boolean deleteUser ( String email )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			boolean result = userDao.delete ( email );
		ts.commit ();
		return result;
	}

	@Override
	public void setServicesVisibility ( Boolean publicFlag, Date releaseDate, String ... serviceNames ) 
	{
		setDescribVisibility ( Service.class, "Service", publicFlag, releaseDate, serviceNames );
	}

	@Override
	public void setRepositoriesVisibility ( Boolean publicFlag, Date releaseDate, String ... repositoryNames )
	{
		setDescribVisibility ( Service.class, "Repository", publicFlag, releaseDate, repositoryNames );
	}

	@Override
	public void setServiceCollectionVisibility ( Boolean publicFlag, Date releaseDate, String ... serviceCollNames ) 
	{
		setDescribVisibility ( ServiceCollection.class, "Service collection", publicFlag, releaseDate, serviceCollNames );
	}
	
	private <D extends Describeable> void setDescribVisibility ( 
		Class<D> targetClass, String describeableLabel, boolean publicFlag, Date releaseDate, String ... names )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		
			userDao.enforceRole ( User.Role.EDITOR );
			DescribeableDAO<D> descrDao = new DescribeableDAO<D> ( entityManager, targetClass );
			
			for ( String dname: names )
			{
				D descr = descrDao.findByName ( dname );
				if ( descr == null ) throw new RuntimeException ( String.format ( 
					"%s '%s' not found", describeableLabel, dname 
				));
				descr.setReleaseDate ( releaseDate );
				descr.setPublicFlag ( publicFlag );
			}
		ts.commit ();
	}
	
	@Override
	public void setEntityVisibility ( Boolean publicFlag, Date releaseDate, String ... entityIds )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		
			userDao.enforceRole ( User.Role.EDITOR );
			EntityMappingDAO emDao = new EntityMappingDAO ( entityManager );
			
			for ( String entityId: entityIds )
			{
				EntityMapping emap = emDao.findEntityMapping ( entityId );
				if ( emap == null ) throw 
					new RuntimeException ( String.format ( "Entity mapping '%s' not found", entityId ));
				emap.setReleaseDate ( releaseDate );
				emap.setPublicFlag ( publicFlag );
			}
		ts.commit ();
	}

}
