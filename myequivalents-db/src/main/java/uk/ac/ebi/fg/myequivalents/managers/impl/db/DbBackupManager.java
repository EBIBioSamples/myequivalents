package uk.ac.ebi.fg.myequivalents.managers.impl.db;

import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.BackupDAO;
import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;

/**
 * The implementation of {@link BackupManager} for the relational DB backend.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>20 Feb 2015</dd>
 *
 */
public class DbBackupManager extends DbMyEquivalentsManager implements BackupManager
{
	/**
	 * Wraps basic operations with transactional demarcation.
	 */
	protected static class TransactionalBackupDAO extends BackupDAO
	{
		public TransactionalBackupDAO ( EntityManager entityManager ) {
			super ( entityManager );
		}

		/**
		 * This initialises a JPA transaction, before invoking the superclass implementation.
		 */
		@Override
		public int upload ( Stream<MyEquivalentsModelMember> in )
		{
			EntityTransaction ts = this.entityManager.getTransaction ();
			ts.begin ();
			int result = super.upload ( in );
			if ( ts.isActive () && !ts.getRollbackOnly () ) ts.commit ();
			return result;
		}

		/**
		 * Calls {@link #commitCheckPoint(int)}.
		 */
		@Override
		protected void postUpload ( Describeable d, int itemCounter )
		{
			commitCheckPoint ( itemCounter );
		}

		/**
		 * Calls {@link #commitCheckPoint(int)}.
		 */
		@Override
		protected void postUpload ( Bundle b, int itemCounter )
		{
			commitCheckPoint ( itemCounter );
		}

		/**
		 * This commits the current transaction from time to time, and starts a new one afterwards. 
		 */
		protected void commitCheckPoint ( int itemCounter )
		{
			if ( itemCounter % 10000 == 0 ) 
			{
				EntityTransaction ts = this.entityManager.getTransaction ();
				ts.commit ();
				ts.begin ();
			}
		}
	}

	
	protected BackupDAO bkpDao;
	
	
	protected DbBackupManager ( BackupDAO bkpDao, String email, String apiPassword )
	{
		super ( bkpDao.getEntityManager (), email, apiPassword );
		this.bkpDao = bkpDao;
	}

	public DbBackupManager ( EntityManager entityManager, String email, String apiPassword )
	{
		this ( new TransactionalBackupDAO ( entityManager ),  email, apiPassword );
	}
	
	/**
	 * Adds up root element tags for the output ( {@code <myequivalents-backup>} ).
	 */
	@Override
	public Stream<MyEquivalentsModelMember> dump ( Integer offset, Integer limit )
	{
		this.userDao.enforceRole ( Role.ADMIN );
		return this.bkpDao.dump ( offset, limit );
	}

	/**
	 * Checks that the current user is an {@link Role#ADMIN admin} and then invokes {@link TransactionalBackupDAO}.
	 */
	@Override
	public int upload ( Stream<MyEquivalentsModelMember> in )
	{
		this.userDao.enforceRole ( Role.ADMIN );
		return this.bkpDao.upload ( in );
	}

	@Override
	public int countEntities ()
	{
		EntityMappingDAO mapDao = new EntityMappingDAO ( this.bkpDao.getEntityManager () );
		return (int) mapDao.count ();
	}
	
}
