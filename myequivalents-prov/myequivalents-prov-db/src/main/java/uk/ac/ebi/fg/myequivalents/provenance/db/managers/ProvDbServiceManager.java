package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import java.io.Reader;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.provenance.db.utils.ProvenanceDbUtils;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry.EntryType;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry.Operation;

/**
 * 
 * TODO: Comment me!
 * 
 * <dl><dt>date</dt><dd>31 Mar 2014</dd></dl>
 * @author Marco Brandizi
 *  
 */
public class ProvDbServiceManager extends DbServiceManager
{
	/**
	 * Logins as anonymous.
	 */
	ProvDbServiceManager ( EntityManager em ) {
		this ( em, null, null );
	}

	
	/**
	 * You don't instantiate this class directly, you must use the {@link DbManagerFactory}.
	 */
	ProvDbServiceManager ( EntityManager em, String email, String apiPassword )
	{
		super ( em, email, apiPassword );
	}
	
	
	@Override
	public void storeServices ( Service... services )
	{
		if ( services == null || services.length == 0 ) return;
		
		super.storeServices ( services );
		ProvenanceDbUtils.recordOperation ( entityManager, Operation.STORE, "storeServices", userDao, services );
	}
	
	@Override
	public void storeServicesFromXML ( Reader reader )
	{
		ServiceSearchResult servRes = super.storeServicesFromXMLAndGetResult ( reader );
		if ( servRes == null || servRes.size () == 0 ) return;
		
		String topOp = "storeServicesFromXML (...)";
		Date opTs = new Date ();
		
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( ServiceCollection sc: servRes.getServiceCollections () )
			{
				ProvenanceRegisterEntry provEntry = new ProvenanceRegisterEntry (
					sc, Operation.STORE, userDao.getLoggedInUser ().getEmail (), opTs 
				);
				provEntry.setTopOperation ( topOp );
			}

			for ( Repository repo: servRes.getRepositories () )
			{
				ProvenanceRegisterEntry provEntry = new ProvenanceRegisterEntry (
					repo, Operation.STORE, userDao.getLoggedInUser ().getEmail (), opTs
				);
				provEntry.setTopOperation ( topOp );
			}
			
			for ( Service service: servRes.getServices () ) 
			{
				ProvenanceRegisterEntry provEntry = new ProvenanceRegisterEntry (
					service, Operation.STORE, userDao.getLoggedInUser ().getEmail (), opTs 
				);
				provEntry.setTopOperation ( topOp );
				
			}
		ts.commit ();
	}
	
	@Override
	public int deleteServices ( String... names )
	{
		int result = super.deleteServices ( names );
		if ( result == 0 ) return result;
		
		ProvenanceDbUtils.recordOperation ( entityManager, Operation.DELETE, "deleteServices", userDao, EntryType.SERVICE, names );
		return result;
	}
	

	@Override
	public void storeServiceCollections ( ServiceCollection... servColls ) 
	{
		if ( servColls == null || servColls.length == 0 ) return;
		
		super.storeServiceCollections ( servColls );
		ProvenanceDbUtils.recordOperation ( entityManager, Operation.STORE, "storeServiceCollections", userDao, servColls );
	}
	
	@Override
	public int deleteServiceCollections ( String... names )
	{
		int result = super.deleteServiceCollections ( names );
		if ( result == 0 ) return result;
		
		ProvenanceDbUtils.recordOperation ( 
			entityManager, Operation.DELETE, "deleteServiceCollections", userDao, EntryType.SERVICE_COLLECTION, names );
		return result;
	}


	
	

	@Override
	public void storeRepositories ( Repository... repos ) 
	{
		if ( repos == null || repos.length == 0 ) return;
		
		super.storeRepositories ( repos );
		ProvenanceDbUtils.recordOperation ( entityManager, Operation.STORE, "storeRepositories", userDao, repos );
	}
	
	@Override
	public int deleteRepositories ( String... names )
	{
		int result = super.deleteRepositories ( names );
		if ( result == 0 ) return result;
		
		ProvenanceDbUtils.recordOperation ( 
			entityManager, Operation.DELETE, "deleteRepositories", userDao, EntryType.REPOSITORY, names );
		return result;
	}

}
