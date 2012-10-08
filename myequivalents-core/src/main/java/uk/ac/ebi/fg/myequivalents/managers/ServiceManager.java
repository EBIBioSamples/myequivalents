package uk.ac.ebi.fg.myequivalents.managers;

import java.io.Reader;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import uk.ac.ebi.fg.myequivalents.dao.RepositoryDAO;
import uk.ac.ebi.fg.myequivalents.dao.ServiceCollectionDAO;
import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.JAXBUtils;

/**
 * 
 * <h2>The Service (and related things) Manager</h2>
 * 
 * <p>This is used to manager the contexts the {@link Entity entities} refer to, 
 * i.e., the {@link Service}s and connected things, namely {@link ServiceCollection}s and {@link Repository}s. 
 * The persistence-related invocations does the transaction management automatically (i.e., they commit all implied changes).</p>
 * 
 * <p>Note that this class instantiates a new {@link EntityManager} in its constructor. This makes it an 
 * entity-manager-per-request when the service is accessed via Apache Axis (cause it re-instantiates at every request).</p>
 * 
 * <p>You have to decide the lifetime of a {@link ServiceManager} instance in your application, we suggest to apply the
 * manager-per-request approach.</p>
 * 
 * <p>This class is not thread-safe, the idea is that you create a new instance per thread, do some operations, release.</p> 
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceManager
{
	private EntityManager entityManager;
	private ServiceDAO serviceDAO;
	private ServiceCollectionDAO serviceCollDAO;
	private RepositoryDAO repoDAO;
	
	
	public ServiceManager ()
	{
		this.entityManager = Resources.getInstance ().getEntityManagerFactory ().createEntityManager ();
		this.serviceDAO = new ServiceDAO ( entityManager );
		this.serviceCollDAO = new ServiceCollectionDAO ( entityManager );
		this.repoDAO = new RepositoryDAO ( entityManager );
	}
	
	/**
	 * Stores a set of services. This is based on {@link ServiceDAO#store(Service)}, wrapped by transaction management.
	 */
	public void storeServices ( Service... services )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( Service service: services )
				serviceDAO.store ( service );
		ts.commit ();
	}
	
	/**
	 * Stores services described by means of XML passed to the parameter reader. 
	 * TODO: document the format. This is auto-generated via JAXB from {@link ExposedService} and reflects that class, for
	 * the moment examples are available in JUnit tests: {@link ServiceManagerTest}, {@link uk.ac.ebi.fg.myequivalents.cmdline.MainTest}.
	 */
	public void storeServicesFromXML ( Reader reader ) throws JAXBException 
	{
		JAXBContext context = JAXBContext.newInstance ( ServiceSearchResult.class );
		Unmarshaller u = context.createUnmarshaller ();
		ServiceSearchResult servRes = (ServiceSearchResult) u.unmarshal ( reader );
		
		// Some massage and then the storage
		//
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
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
						ServiceCollection servColl = serviceCollDAO.findByName ( servCollName );
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
						Repository repo = repoDAO.findByName ( repoName );
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
	}
	
	/**
	 * Deletes services by name. This uses {@link ServiceDAO#delete(String)} and wraps it with transaction management. 
	 */
	public int deleteServices ( String... names )
	{
		int ct = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( String name: names )
				if ( serviceDAO.delete ( name ) ) ct++;
		ts.commit ();
		return ct;
	}
	
	/**
	 * Gets services by name. It pulls up related stuff (i.e., {@link ServiceCollection}s and {@link Repository repositories} 
	 * referred by the service) and put it all inside the {@link ServiceSearchResult} used as result.
	 * 
	 * This method uses {@link ServiceDAO}. 
	 */
	public ServiceSearchResult getServices ( String... names ) 
	{
		ServiceSearchResult result = new ServiceSearchResult ();
		for ( String name: names )
		{
			Service service = serviceDAO.findByName ( name );
			if ( service == null ) continue; 
			result.addService ( service );
			
			ServiceCollection sc = service.getServiceCollection ();
			if ( sc != null ) result.addServiceCollection ( sc );
			
			Repository repo = service.getRepository ();
			if ( repo != null ) result.addRepository ( repo );
		}
		return result;
	}

	/**
	 * Invokes {@link #getServices(String...)} and wraps the result into XML.
	 *   
	 * TODO: document the format. This is auto-generated via JAXB from {@link ServiceSearchResult} and reflects that class, for
	 * the moment examples are available in JUnit tests: {@link ServiceManagerTest}, {@link uk.ac.ebi.fg.myequivalents.cmdline.MainTest}.
	 * 
	 */
	private String getServicesAsXml ( String... names )
	{
		return JAXBUtils.marshal ( getServices ( names ), ServiceSearchResult.class );
	}
	
	/**
	 *  Returns the same result returned by {@link #getServices(String...)} in the format specified by the parameter. 
	 *  At the moment this is only 'xml' and {@link #getServicesAsXml(String...)} is used for this. We plan formats 
	 *  like RDF or JSON for the future.
	 */
	public String getServicesAs ( String outputFormat, String... names ) 
	{
		if ( "xml".equals ( outputFormat ) )
			return getServicesAsXml ( names );
		else
			return "<error>Unsopported output format '" + outputFormat + "'</error>";		
	}
	
	
	
	
	/**
	 * Stores {@link ServiceCollection}s. This uses {@link ServiceCollectionDAO#store(ServiceCollection)} and wraps it 
	 * with transaction management. 
	 */
	public void storeServiceCollections ( ServiceCollection... servColls ) 
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( ServiceCollection sc: servColls )
				serviceCollDAO.store ( sc );
		ts.commit ();
	}
	
	/**
	 * Deletes service-collections by name. This uses {@link ServiceCollectionDAO#delete(String)} and wraps it with 
	 * transaction management. Note that you'll get an exception if there is a service referring to any of the 
	 * collections you're trying to delete. 
	 * 
	 */
	public int deleteServiceCollections ( String... names )
	{
		int ct = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( String name: names )
				if ( serviceCollDAO.delete ( name ) ) ct++;
		ts.commit ();
		return ct;
	}

	/**
	 * Gets {@link ServiceCollection}s by name. For coherence with the rest of this manager, puts the result into 
	 * {@link ServiceSearchResult}. This uses {@link ServiceCollectionDAO#findByName(ServiceCollection)}
	 */
	public ServiceSearchResult getServiceCollections ( String... names ) 
	{
		ServiceSearchResult result = new ServiceSearchResult ();
		for ( String name: names )
		{
			ServiceCollection sc = serviceCollDAO.findByName ( name );
			if ( sc == null ) continue; 
			result.addServiceCollection ( sc );
		}
		return result;
	}
	
	/**
	 * Invokes {@link #getServiceCollections(String...)} and wraps the result into XML.
	 *   
	 * TODO: document the format. This is auto-generated via JAXB from {@link ServiceSearchResult} and reflects that class, for
	 * the moment examples are available in JUnit tests: {@link ServiceManagerTest}, {@link uk.ac.ebi.fg.myequivalents.cmdline.MainTest}.
	 * 
	 */
	private String getServiceCollectionAsXml ( String... names )
	{
		return JAXBUtils.marshal ( getServiceCollections ( names ), ServiceSearchResult.class );
	}
	
	/**
	 *  Returns the same result returned by {@link #getServiceCollections(String...)} in the format specified by the parameter. 
	 *  At the moment this is only 'xml' and {@link #getServiceCollectionAsXml(String...)} is used for this. We plan formats 
	 *  like RDF or JSON for the future.
	 */
	public String getServiceCollectionsAs ( String outputFormat, String... names ) 
	{
		if ( "xml".equals ( outputFormat ) )
			return getServiceCollectionAsXml ( names );
		else
			return "<error>Unsopported output format '" + outputFormat + "'</error>";		
	}
	
	


	/**
	 * Stores {@link Repository repositories}. This uses {@link RepositoryDAO#store(Repository)} and wraps it 
	 * with transaction management. 
	 */
	public void storeRepositories ( Repository... repos ) 
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( Repository repo: repos )
				repoDAO.store ( repo );
		ts.commit ();
	}
	
	/**
	 * Deletes {@link Repository repositories} by name. This uses {@link RepositoryDAO#delete(String)} and wraps it 
	 * with transaction management. 
	 */
	public int deleteRepositories ( String... names )
	{
		int ct = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( String name: names )
				if ( repoDAO.delete ( name ) ) ct++;
		ts.commit ();
		return ct;
	}

	/**
	 * Gets {@link Repository repositories} by name. For coherence with the rest of this manager, puts the result into 
	 * {@link ServiceSearchResult}. This uses {@link RepositoryDAO#findByName(String)}.
	 */
	public ServiceSearchResult getRepositories ( String... names ) 
	{
		ServiceSearchResult result = new ServiceSearchResult ();
		for ( String name: names )
		{
			Repository repo = repoDAO.findByName ( name );
			if ( repo == null ) continue; 
			result.addRepository ( repo );
		}
		return result;
	}
	
	/**
	 * Invokes {@link #getRepositories(String...)} and wraps the result into XML.
	 *   
	 * TODO: document the format. This is auto-generated via JAXB from {@link ServiceSearchResult} and reflects that class, for
	 * the moment examples are available in JUnit tests: {@link ServiceManagerTest}, {@link uk.ac.ebi.fg.myequivalents.cmdline.MainTest}.
	 */
	private String getRepositoriesAsXml ( String... names )
	{
		return JAXBUtils.marshal ( getRepositories ( names ), ServiceSearchResult.class );
	}
	
	/**
	 *  Returns the same result returned by {@link #getRepositories(String...)} in the format specified by the parameter. 
	 *  At the moment this is only 'xml' and {@link #getRepositoriesAsXml(String...)} is used for this. We plan formats 
	 *  like RDF or JSON for the future.
	 */
	public String getRepositoriesAs ( String outputFormat, String... names ) 
	{
		if ( "xml".equals ( outputFormat ) )
			return getRepositoriesAsXml ( names );
		else
			return "<error>Unsopported output format '" + outputFormat + "'</error>";		
	}

}
