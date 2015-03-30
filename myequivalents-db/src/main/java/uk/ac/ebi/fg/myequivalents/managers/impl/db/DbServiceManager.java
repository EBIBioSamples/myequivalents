package uk.ac.ebi.fg.myequivalents.managers.impl.db;

import static uk.ac.ebi.fg.myequivalents.access_control.model.User.Role.EDITOR;

import java.io.Reader;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.dao.RepositoryDAO;
import uk.ac.ebi.fg.myequivalents.dao.ServiceCollectionDAO;
import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ExposedService;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.utils.ManagerUtils;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.JAXBUtils;

/**
 * <h2>The DB-based {@link ServiceManager} implementation relational database connection straight (without other 
 * managers in between, but often using DAOs in the uk.ac.ebi.fg.myequivalents.dao package).</h2>
 * 
 * <p>This is used to manage the contexts the {@link Entity entities} refer to, 
 * i.e., the {@link Service}s and connected things, namely {@link ServiceCollection}s and {@link Repository}s. 
 * The persistence-related invocations does the transaction management automatically (i.e., they commit all implied changes).</p>
 * 
 * <p>Note that this class instantiates a new {@link EntityManager} in its constructor. This makes it an 
 * entity-manager-per-request when the service is accessed via Apache Axis (cause it re-instantiates at every request).</p>
 * 
 * <p>You have to decide the lifetime of a {@link DbServiceManager} instance in your application, we suggest to apply the
 * manager-per-request approach.</p>
 * 
 * <p>As explained in the interface, this class is not thread-safe.</p> 
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DbServiceManager extends DbMyEquivalentsManager implements ServiceManager
{
	private ServiceDAO serviceDAO;
	private ServiceCollectionDAO serviceCollDAO;
	private RepositoryDAO repoDAO;

	/**
	 * Logins as anonymous.
	 */
	DbServiceManager ( EntityManager em ) {
		this ( em, null, null );
	}

	
	/**
	 * You don't instantiate this class directly, you must use the {@link DbManagerFactory}.
	 */
	protected DbServiceManager ( EntityManager em, String email, String apiPassword )
	{
		super ( em, email, apiPassword );
		this.serviceDAO = new ServiceDAO ( entityManager );
		this.serviceCollDAO = new ServiceCollectionDAO ( entityManager );
		this.repoDAO = new RepositoryDAO ( entityManager );
	}

	/**
	 * Uses {@link ServiceDAO#store(Service)} and adds up transaction management.
	 */
	@Override
	public void storeServices ( Service... services )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.enforceRole ( EDITOR );
			for ( Service service: services ) {
				// In case needed, unwrap from the wrapper used for web services
				if ( service instanceof ExposedService ) service = ((ExposedService) service).asService ();
				serviceDAO.store ( service );
			}
		ts.commit ();
	}
	
	@Override
	public void storeServicesFromXML ( Reader reader ) {
		storeServicesFromXMLAndGetResult ( reader );
	}

	/**
	 * This is like {@link #storeServicesFromXML(Reader)}, but it additionally returns the unserialized objects it found
	 * inside the XML. This is useful to implement other types of managers.  
	 */
	protected ServiceSearchResult storeServicesFromXMLAndGetResult ( Reader reader )
	{
		ServiceSearchResult servRes = null;
		try
		{
			JAXBContext context = JAXBContext.newInstance ( ServiceSearchResult.class );
			Unmarshaller u = context.createUnmarshaller ();
			servRes = (ServiceSearchResult) u.unmarshal ( reader );
		} 
		catch ( JAXBException ex ) {
			throw new RuntimeException ( "Error while reading services description from XML: " + ex.getMessage (), ex );
		}
		
		// Some massage and then the storage
		//
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.enforceRole ( EDITOR );
			for ( ServiceCollection sc: servRes.getServiceCollections () )
				serviceCollDAO.store ( sc );

			for ( Repository repo: servRes.getRepositories () )
				repoDAO.store ( repo );

			for ( Service service: servRes.getServices () ) 
			{
				{
					String servCollName = service.getServiceCollectionName ();
					if ( servCollName != null ) 
					{
						ServiceCollection servColl = serviceCollDAO.findByName ( servCollName, false );
						if ( servColl == null ) throw new RuntimeException ( String.format ( 
							"Cannot store service '%s' linked to the non-existing service-collection '%s'", 
							service.getName (), service.getServiceCollectionName ()
						));
												
						service.setServiceCollection ( servColl );
					}
				}

				{
					String repoName = service.getRepositoryName ();
					if ( repoName != null )
					{
						Repository repo = repoDAO.findByName ( repoName, false );
						if ( repo == null ) throw new RuntimeException ( String.format ( 
							"Cannot store service '%s' linked to the non-existing repository '%s'", 
							service.getName (), service.getRepositoryName ()
						));
						
						service.setRepository ( repo );
					}
				}

				serviceDAO.store ( ((ExposedService) service).asService () );
			}
		ts.commit ();
		
		return servRes;
	}
	
	/**
	 * Uses {@link ServiceDAO#delete(Service)} and adds up transaction management.
	 */
	@Override
	public int deleteServices ( String... names )
	{
		int ct = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.enforceRole ( EDITOR );
			for ( String name: names )
				if ( serviceDAO.delete ( name ) ) ct++;
		ts.commit ();
		return ct;
	}

	/**
	 * Uses {@link ServiceDAO#findByName(String, boolean))}.
	 */
	@Override
	public ServiceSearchResult getServices ( String... names ) 
	{
		User user = userDao.getLoggedInUser ();
		boolean mustBePublic = user == null ? true : !user.hasPowerOf ( EDITOR );
		
		ServiceSearchResult result = new ServiceSearchResult ();
		for ( String name: names )
		{
			Service service = serviceDAO.findByName ( name, mustBePublic );
			if ( service == null ) continue; 
			result.addService ( service );
			
			ServiceCollection sc = service.getServiceCollection ();
			if ( sc != null && ( !mustBePublic || sc.isPublic () ) ) result.addServiceCollection ( sc );
			
			Repository repo = service.getRepository ();
			if ( repo != null && ( !mustBePublic || repo.isPublic () ) ) result.addRepository ( repo );
		}
		return result;
	}

	/**
	 * Invokes {@link #getServices(String...)} and wraps the result into XML.
	 *   
	 * TODO: document the format. This is auto-generated via JAXB from {@link ServiceSearchResult} and reflects that class, for
	 * the moment examples are available in JUnit tests: {@link uk.ac.ebi.fg.myequivalents.managers.ServiceManagerTest}, 
	 * {@link uk.ac.ebi.fg.myequivalents.cmdline.MainTest}.
	 * 
	 */
	private String getServicesAsXml ( String... names )
	{
		return JAXBUtils.marshal ( getServices ( names ), ServiceSearchResult.class );
	}
	
	@Override
	public String getServicesAs ( String outputFormat, String... names ) 
	{
		ManagerUtils.checkOutputFormat ( outputFormat );
		return getServicesAsXml ( names );
	}
	

	/**
	 * Uses {@link ServiceCollectionDAO#store(ServiceCollection)} and adds up transaction management.
	 */
	@Override
	public void storeServiceCollections ( ServiceCollection... servColls ) 
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.enforceRole ( EDITOR );
			for ( ServiceCollection sc: servColls )
				serviceCollDAO.store ( sc );
		ts.commit ();
	}
	
	/**
	 * Uses {@link ServiceCollectionDAO#delete(String)} and adds up transaction management.
	 */
	@Override
	public int deleteServiceCollections ( String... names )
	{
		int ct = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.enforceRole ( EDITOR );
			for ( String name: names )
				if ( serviceCollDAO.delete ( name ) ) ct++;
		ts.commit ();
		return ct;
	}

	/**
	 * Uses {@link ServiceCollectionDAO#findByName(String)}.
	 */
	@Override
	public ServiceSearchResult getServiceCollections ( String... names ) 
	{
		User user = userDao.getLoggedInUser ();
		boolean mustBePublic = user == null ? true : !user.hasPowerOf ( EDITOR );

		ServiceSearchResult result = new ServiceSearchResult ();
		for ( String name: names )
		{
			ServiceCollection sc = serviceCollDAO.findByName ( name, mustBePublic );
			if ( sc == null ) continue; 
			result.addServiceCollection ( sc );
		}
		return result;
	}
	
	/**
	 * Invokes {@link #getServiceCollections(String...)} and wraps the result into XML.
	 *   
	 * TODO: document the format. This is auto-generated via JAXB from {@link ServiceSearchResult} and reflects that class, for
	 * the moment examples are available in JUnit tests: 
	 * {@link uk.ac.ebi.fg.myequivalents.managers.ServiceManagerTest}, 
	 * {@link uk.ac.ebi.fg.myequivalents.cmdline.MainTest}.
	 * 
	 */
	private String getServiceCollectionAsXml ( String... names )
	{
		return JAXBUtils.marshal ( getServiceCollections ( names ), ServiceSearchResult.class );
	}
	
	@Override
	public String getServiceCollectionsAs ( String outputFormat, String... names ) 
	{
		ManagerUtils.checkOutputFormat ( outputFormat );
		return getServiceCollectionAsXml ( names );
	}
	
	

	/**
	 * Uses {@link RepositoryDAO#store(Repository)} and adds up transaction management.
	 */
	@Override
	public void storeRepositories ( Repository... repos ) 
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.enforceRole ( EDITOR );
			for ( Repository repo: repos )
				repoDAO.store ( repo );
		ts.commit ();
	}
	
	/**
	 * Uses {@link RepositoryDAO#delete(String)} and adds up transaction management.
	 */
	@Override
	public int deleteRepositories ( String... names )
	{
		int ct = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		userDao.enforceRole ( EDITOR );
			for ( String name: names )
				if ( repoDAO.delete ( name ) ) ct++;
		ts.commit ();
		return ct;
	}

	/**
	 * Uses {@link RepositoryDAO#findByName(String)}.
	 */
	@Override
	public ServiceSearchResult getRepositories ( String... names ) 
	{
		User user = userDao.getLoggedInUser ();
		boolean mustBePublic = user == null ? true : !user.hasPowerOf ( EDITOR );

		ServiceSearchResult result = new ServiceSearchResult ();
		for ( String name: names )
		{
			Repository repo = repoDAO.findByName ( name, mustBePublic );
			if ( repo == null ) continue; 
			result.addRepository ( repo );
		}
		return result;
	}
	
	/**
	 * Invokes {@link #getRepositories(String...)} and wraps the result into XML.
	 *   
	 * TODO: document the format. This is auto-generated via JAXB from {@link ServiceSearchResult} and reflects that class, for
	 * the moment examples are available in JUnit tests: {@link uk.ac.ebi.fg.myequivalents.managers.ServiceManagerTest},
	 * {@link uk.ac.ebi.fg.myequivalents.cmdline.MainTest}.
	 */
	private String getRepositoriesAsXml ( String... names )
	{
		return JAXBUtils.marshal ( getRepositories ( names ), ServiceSearchResult.class );
	}
	
	@Override
	public String getRepositoriesAs ( String outputFormat, String... names ) 
	{
		ManagerUtils.checkOutputFormat ( outputFormat );
		return getRepositoriesAsXml ( names );
	}

	/**
	 * Close DB connections and terminate the use of this manager. Note that it cannot be re-used after this invocation.
	 * This may occasionally be useful (e.g., multi-thread applications).
	 */
	public void close () {
		entityManager.close ();
	}

}
