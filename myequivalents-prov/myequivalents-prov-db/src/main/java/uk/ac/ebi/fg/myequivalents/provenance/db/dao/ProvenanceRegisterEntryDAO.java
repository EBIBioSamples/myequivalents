package uk.ac.ebi.fg.myequivalents.provenance.db.dao;

import javax.persistence.EntityManager;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>31 Mar 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceRegisterEntryDAO
{
	private EntityManager entityManager;
	
	public ProvenanceRegisterEntryDAO ( EntityManager em ) {
		this.entityManager = em;
	}


}
