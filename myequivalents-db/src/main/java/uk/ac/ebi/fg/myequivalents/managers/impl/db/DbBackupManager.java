package uk.ac.ebi.fg.myequivalents.managers.impl.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.BackupDAO;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Describeable;

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
		public int upload ( InputStream input )
		{
			EntityTransaction ts = this.entityManager.getTransaction ();
			ts.begin ();
			int result = super.upload ( input );
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
	public int dump ( OutputStream out, Integer offset, Integer limit )
	{
		try
		{
			this.userDao.enforceRole ( Role.ADMIN );
			out.write ( "<myequivalents-backup>\n".getBytes () );
			int result = this.bkpDao.dump ( out, offset, limit );
			out.write ( "</myequivalents-backup>\n".getBytes () );
			return result;
		}
		catch ( IOException ex )
		{
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
	}

	/**
	 * Checks that the current user is an {@link Role#ADMIN admin} and then invokes {@link TransactionalBackupDAO}.
	 */
	@Override
	public int upload ( InputStream in )
	{
		this.userDao.enforceRole ( Role.ADMIN );
		return this.bkpDao.upload ( in );
	}

}
