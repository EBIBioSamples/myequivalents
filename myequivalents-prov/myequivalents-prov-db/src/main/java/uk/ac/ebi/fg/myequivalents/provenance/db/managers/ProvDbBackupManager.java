package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import java.util.Arrays;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbBackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;

/**
 * The provenance-based implementation of {@link DbBackupManager}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Mar 2015</dd>
 *
 */
public class ProvDbBackupManager extends DbBackupManager
{
	/** 
	 * Adds provenance information to the database backend.
	 */
	protected static class ProvBackupDAO extends TransactionalBackupDAO
	{
		private ProvenanceRegisterEntryDAO provRegDao;
		private String userEmail;
		
		public ProvBackupDAO ( EntityManager entityManager, String userEmail ) {
			super ( entityManager );
			this.provRegDao = new ProvenanceRegisterEntryDAO ( entityManager );
			this.userEmail = userEmail;
		}

		/**
		 * Creates an entry of type 'backup.upload' in the {@link ProvenanceRegisterEntry} table.
		 * Then invokes its {@link TransactionalBackupDAO#postUpload(Describeable, int) parent's implementation}, in order 
		 * to have {@link #commitCheckPoint(int)} called. 
		 */
		@Override
		protected void postUpload ( Describeable d, int itemCounter )
		{
		  provRegDao.create ( new ProvenanceRegisterEntry ( 
				userEmail, "backup.upload", Arrays.asList ( ProvenanceRegisterParameter.p ( d ) )
			));
		  
			super.postUpload ( d, itemCounter );
		}

		/**
		 * Works like {@link #postUpload(Describeable, int)}.
		 */
		@Override
		protected void postUpload ( Bundle b, int itemCounter )
		{
		  provRegDao.create ( new ProvenanceRegisterEntry ( 
		  	userEmail, "backup.upload", ProvenanceRegisterParameter.p ( b.getEntities () ) 
		  ));

		  super.postUpload ( b, itemCounter );
		}
	}
	
	public ProvDbBackupManager ( EntityManager entityManager, String email, String apiPassword )
	{
		super ( new ProvBackupDAO ( entityManager, email ), email, apiPassword );
	}
}
