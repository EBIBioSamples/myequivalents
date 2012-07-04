package uk.ac.ebi.fg.myequivalents.managers;

import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import uk.ac.ebi.fg.myequivalents.dao.RepositoryDAO;
import uk.ac.ebi.fg.myequivalents.dao.ServiceCollectionDAO;
import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.managers.ExposedService.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.JAXBUtils;

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
		
	public void storeServices ( Service... services )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( Service service: services )
				serviceDAO.store ( service );
		ts.commit ();
	}
	
	public void storeServicesFromXML ( Reader reader ) throws JAXBException 
	{
		JAXBContext context = JAXBContext.newInstance ( ServiceSearchResult.class );
		Unmarshaller u = context.createUnmarshaller ();
		ServiceSearchResult sset = (ServiceSearchResult) u.unmarshal ( reader );
		
		// Some massage and then the storage
		//
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			for ( Service service: sset.getServices () ) 
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
	
	public ServiceSearchResult getServices ( String... names ) 
	{
		Set<Service> result = new HashSet<Service> ();
		for ( String name: names )
		{
			Service service = serviceDAO.findByName ( name );
			if ( service != null ) result.add ( service );
		}
		return new ServiceSearchResult ( result );
	}
	
	// TODO: Needs a 'completeFlag' and the feature to report connected entities (repos, serv-collections)
	private String getServicesAsXml ( String... names )
	{
		return JAXBUtils.marshal ( getServices ( names ), ServiceSearchResult.class );
	}
	
	public String getServicesAs ( String outputFormat, String... names ) 
	{
		if ( "xml".equals ( outputFormat ) )
			return getServicesAsXml ( names );
		else
			return "<error>Unsopported output format '" + outputFormat + "'</error>";		
	}
}
