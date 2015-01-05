package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;

/**
 * 
 * A wrapper for {@link DbServiceManager}, which keeps track of service/repository/collection changing operations, using
 * the {@link ProvenanceRegisterEntry provenance register}. 
 * 
 * <dl><dt>date</dt><dd>31 Mar 2014</dd></dl>
 * @author Marco Brandizi
 *  
 */
public class ProvDbServiceManager extends DbServiceManager
{
	private ProvenanceRegisterEntryDAO provRegDao;
	
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
		provRegDao = new ProvenanceRegisterEntryDAO ( this.entityManager );
	}
	
	
	/**
	 * save a 'service.storeServices' and 'service' parameters into the provenance registry.
	 */
	@Override
	public void storeServices ( Service... services )
	{
		if ( services == null || services.length == 0 ) return;
		super.storeServices ( services );
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "service.storeServices", p ( Arrays.asList ( services ) ) 
	  ));
	  ts.commit ();
	  
	}
	

	/**
	 * save a 'service.storeServicesFromXML' and different parameters into the provenance registry.
	 */
	public void storeServicesFromXML ( Reader reader )
	{
		ServiceSearchResult servRes = super.storeServicesFromXMLAndGetResult ( reader );
		if ( servRes == null || servRes.size () == 0 ) return;
		
		List<ProvenanceRegisterParameter> params = p ( servRes.getServices () );
		p ( params, servRes.getRepositories () );
		p ( params, servRes.getServiceCollections () );
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( this.getUserEmail (), "service.storeServicesFromXML", params ) );
	  ts.commit ();
	}
	
	/**
	 * save a 'service.deleteServices' and 'service' parameters into the provenance registry.
	 */
	@Override
	public int deleteServices ( String... names )
	{
		int result = super.deleteServices ( names );
		if ( result == 0 ) return result;
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			this.getUserEmail (), "service.deleteServices", p ( "service", Arrays.asList ( names ) ) 
	  ));
	  ts.commit ();
		
		return result;
	}
	
	/**
	 * save a 'service.storeServiceCollections' and 'serviceCollection' parameters into the provenance registry.
	 */
	@Override
	public void storeServiceCollections ( ServiceCollection... servColls ) 
	{
		if ( servColls == null || servColls.length == 0 ) return;
		super.storeServiceCollections ( servColls );

		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			this.getUserEmail (), "service.storeServiceCollections", p ( Arrays.asList ( servColls ) ) 
	  ));
	  ts.commit ();
	}
	
	/**
	 * save a 'service.deleteServiceCollections' and 'serviceCollection' parameters into the provenance registry.
	 */
	@Override
	public int deleteServiceCollections ( String... names )
	{
		int result = super.deleteServiceCollections ( names );
		if ( result == 0 ) return result;
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			this.getUserEmail (), "service.deleteServiceCollections", p ( "serviceCollection", Arrays.asList ( names ) ) 
	  ));
	  ts.commit ();

	  return result;
	}


	
	
	/**
	 * save a 'service.storeRepositories' and 'repository' parameters into the provenance registry.
	 */
	@Override
	public void storeRepositories ( Repository... repos ) 
	{
		if ( repos == null || repos.length == 0 ) return;
		super.storeRepositories ( repos );

		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			this.getUserEmail (), "service.storeRepositories", p ( Arrays.asList ( repos ) ) 
	  ));
	  ts.commit ();
	}
	
	/**
	 * save a 'service.deleteRepositories' and 'repository' parameters into the provenance registry.
	 */
	@Override
	public int deleteRepositories ( String... names )
	{
		int result = super.deleteRepositories ( names );
		if ( result == 0 ) return result;
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			this.getUserEmail (), "service.deleteRepositories", p ( "repository", Arrays.asList ( names ) ) 
	  ));
	  ts.commit ();

	  return result;
	}
	
}
