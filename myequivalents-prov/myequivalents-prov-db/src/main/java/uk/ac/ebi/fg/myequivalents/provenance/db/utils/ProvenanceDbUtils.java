package uk.ac.ebi.fg.myequivalents.provenance.db.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry.EntryType;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry.Operation;
import uk.ac.ebi.fg.myequivalents.provenance.utils.ProvenanceUtils;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>1 Apr 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceDbUtils
{
	public static void recordOperation ( 
		EntityManager entityManager, Operation operation, String topOperationName, UserDao userDao, Describeable... describs )
	{
		String topOp = ProvenanceUtils.getOperationString ( topOperationName, describs );
		
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( Describeable d: describs ) {
				ProvenanceRegisterEntry provEntry = new ProvenanceRegisterEntry ( d, operation, userDao.getLoggedInUser ().getEmail () );
				provEntry.setTopOperation ( topOp );
			}
		ts.commit ();
	}

	public static void recordOperation ( 
		EntityManager entityManager, Operation operation, String topOperationName, UserDao userDao, EntryType entryType, String... entityIds )
	{
		String topOp = ProvenanceUtils.getOperationString ( topOperationName, entityIds );
		
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( String id: entityIds ) {
				ProvenanceRegisterEntry provEntry = new ProvenanceRegisterEntry ( 
					id, entryType, operation, userDao.getLoggedInUser ().getEmail () 
				);
				provEntry.setTopOperation ( topOp );
			}
		ts.commit ();
	}
	
}
