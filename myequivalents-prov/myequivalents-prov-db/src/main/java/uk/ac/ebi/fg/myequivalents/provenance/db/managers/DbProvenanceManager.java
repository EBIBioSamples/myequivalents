package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbMyEquivalentsManager;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegisterEntryList;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.utils.JAXBUtils;

/**
 * A DB-based implementation of {@link ProvRegistryManager}. Operations here are committed upon invocation.
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
	public List<ProvenanceRegisterEntry> find ( String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params )
	{
		List<ProvenanceRegisterEntry> result = provDao.find ( userEmail, operation, from, to, params );
		return result;
	}


	@Override
	public String findAs ( String outputFormat, String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params )
	{
		outputFormat = StringUtils.trimToNull ( outputFormat );
		if ( !"xml".equalsIgnoreCase ( outputFormat ) ) throw new IllegalArgumentException ( 
			"Unsopported output format '" + outputFormat + "'" 
		);
		
		return findAsXml ( userEmail, operation, from, to, params );
	}

	private String findAsXml ( String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params )
	{
		return JAXBUtils.marshal ( 
			new ProvRegisterEntryList ( this.find ( userEmail, operation, from, to, params ) ), 
			ProvRegisterEntryList.class
		);		
	}


	@Override
	public List<ProvenanceRegisterEntry> findEntityMappingProv ( String entityId, List<String> validUsers )
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String findEntityMappingProvAs ( String outputFormat, String entityId, List<String> validUsers )
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Set<List<ProvenanceRegisterEntry>> findMappingProv ( String xEntityId, String yEntityId, List<String> validUsers )
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String findMappingProvAs ( String outputFormat, String xEntityId, String yEntityId, List<String> validUsers )
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public int purge ( Date from, Date to )
	{
		this.userDao.enforceRole ( User.Role.EDITOR );
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		int result = provDao.purge ( from, to );
		ts.commit ();
		return result;
	}

}
