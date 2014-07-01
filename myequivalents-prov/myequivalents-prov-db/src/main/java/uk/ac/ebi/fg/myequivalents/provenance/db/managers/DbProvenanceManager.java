package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbMyEquivalentsManager;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>1 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DbProvenanceManager extends DbMyEquivalentsManager implements ProvRegistryManager
{
	ProvenanceRegisterEntryDAO provDao;
	
	public DbProvenanceManager ( EntityManager entityManager, String email, String apiPassword )
	{
		super ( entityManager, email, apiPassword );
		this.userDao.enforceRole ( User.Role.ADMIN );
		
		provDao = new ProvenanceRegisterEntryDAO ( entityManager );
	}


	@Override
	public List<ProvenanceRegisterEntry> find ( String userEmail, String operation, Date from, Date to, List<String> parameterPairs )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		List<ProvenanceRegisterEntry> result = provDao.find ( userEmail, operation, from, to, parameterPairs );
		ts.commit ();
		return result;
	}

	@Override
	public int purge ( Date from, Date to )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		int result = provDao.purge ( from, to );
		ts.commit ();
		return result;
	}

}
