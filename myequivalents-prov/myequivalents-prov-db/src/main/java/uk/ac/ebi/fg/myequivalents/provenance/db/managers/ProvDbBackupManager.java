package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbBackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Mar 2015</dd>
 *
 */
public class ProvDbBackupManager extends DbBackupManager
{
	protected static class ProvBackupDAO extends TransactionalBackupDAO
	{
		private ProvenanceRegisterEntryDAO provRegDao;
		private String userEmail;
		
		public ProvBackupDAO ( EntityManager entityManager, String userEmail ) {
			super ( entityManager );
			this.provRegDao = new ProvenanceRegisterEntryDAO ( entityManager );
			this.userEmail = userEmail;
		}

		@Override
		protected void postUpload ( Describeable d, int itemCounter )
		{
		  provRegDao.create ( new ProvenanceRegisterEntry ( 
				userEmail, "backup.restore", Arrays.asList ( ProvenanceRegisterParameter.p ( d ) )
			));
			commitCheckPoint ( itemCounter );
		}

		@Override
		protected void postUpload ( Bundle b, int itemCounter )
		{
		  provRegDao.create ( new ProvenanceRegisterEntry ( 
		  	userEmail, "backup.restore", ProvenanceRegisterParameter.p ( b.getEntities () ) 
		  ));

		  commitCheckPoint ( itemCounter );
		}

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
	
	public ProvDbBackupManager ( EntityManager entityManager, String email, String apiPassword )
	{
		super ( new ProvBackupDAO ( entityManager, email ), email, apiPassword );
	}
}
