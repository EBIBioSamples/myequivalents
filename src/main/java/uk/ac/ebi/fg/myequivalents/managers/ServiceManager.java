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
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.utils.JAXBUtils;

public class ServiceManager
{
	private EntityManager entityManager;
	private ServiceDAO serviceDAO;
	private ServiceCollectionDAO serviceCollDAO;
	private RepositoryDAO repoDAO;
	
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
		Set<Service> services = sset.getServices ();
		for ( Service service: sset.getServices () )
		{
			if ( serviceCollDAO.exists ( service.getServiceCollectionName () ) )
				throw new RuntimeException ( String.format ( 
					"Cannot store service '%s' linked to the non-existing service-collection '%s'", 
					service.getName (), service.getServiceCollectionName ()
			));
			
			if ( repoDAO.exists ( service.getRepositoryName () ) )
				throw new RuntimeException ( String.format ( 
					"Cannot store service '%s' linked to the non-existing repository '%s'", 
					service.getName (), service.getRepositoryName ()
			));
		}
		this.storeServices ( services.toArray ( new Service [ 0 ] ) );
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
