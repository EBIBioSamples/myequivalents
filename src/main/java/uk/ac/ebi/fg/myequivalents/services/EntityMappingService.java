/*
 * TODO: 
 *   Map as params to web service: http://stackoverflow.com/questions/4654423/how-to-have-a-hashmap-as-webparam-with-jbossws-3-1-2
 */
package uk.ac.ebi.fg.myequivalents.services;
import java.io.StringWriter;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Jun 7, 2012</dd></dl>
 * @author brandizi
 *
 */
public class EntityMappingService
{
	private EntityManager entityManager;
	private EntityMappingDAO entityMappingDAO;
	
	public EntityMappingService ( EntityManager em )
	{
		this.entityManager = em;
		this.entityMappingDAO = new EntityMappingDAO ( entityManager );
	}

	public void storeMappings ( String... entities )
	{
		if ( entities == null || entities.length == 0 ) return;
		if ( entities.length % 4 != 0 ) throw new IllegalArgumentException (
		  "Wrong no. of arguments for storeMappings, I expect a list of (serviceName1/accession1, serviceName2/accession2) quadruples"
		);
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			if ( entities.length == 4 )
				entityMappingDAO.storeMapping ( entities [ 0 ], entities [ 1 ], entities [ 2 ], entities [ 3 ] );
			else
				entityMappingDAO.storeMappings ( entities );
		ts.commit ();
	}

	public void storeMappingBundle ( String... entities )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		  entityMappingDAO.storeMappingBundle ( entities );
		ts.commit ();
	}

	public EntityMappingsResult getMappings ( 
		boolean addServices, boolean addServiceCollections, boolean addRepositories, String... entities )
	{
		EntityMappingsResult result = new EntityMappingsResult ( addServices, addServiceCollections, addRepositories );

		if ( entities == null || entities.length == 0 ) return result;
		if ( entities.length % 2 != 0 ) throw new IllegalArgumentException (
		  "Wrong no. of arguments for getMappings, I expect a list of serviceName/accession pairs"
		);
		
		for ( int i = 0; i < entities.length; i++ )
			result.addAllEntityMappings ( entityMappingDAO.findEntityMappings ( entities [ i ], entities [ ++i ]) );
		
		return result;
	}

	private String getMappingsAsXml (
		boolean addServices, boolean addServiceCollections, boolean addRepositories, String... entities			
	)
	{
		EntityMappingsResult result = this.getMappings ( addServices, addServiceCollections, addRepositories, entities );
		StringWriter sw = new StringWriter ();
		
		try
		{
			JAXBContext context = JAXBContext.newInstance ( EntityMappingsResult.class );
			Marshaller m = context.createMarshaller ();
			m.setProperty ( Marshaller.JAXB_FORMATTED_OUTPUT, true );
			m.setProperty ( Marshaller.JAXB_ENCODING, "UTF-8" );
			m.marshal ( result, sw );
		} 
		catch ( PropertyException ex ) {
			// TODO: Return error XML back
			throw new RuntimeException ( "Internal error while generating the XML search result: " + ex.getMessage () );
		} 
		catch ( JAXBException ex ) {
			throw new RuntimeException ( "Internal error while generating the XML search result: " + ex.getMessage () );
		}

		return sw.toString ();
	}
	
	public String getMappingsAs (
		String outputFormat, boolean addServices, boolean addServiceCollections, boolean addRepositories, String... entities			
	)
	{
		if ( "xml".equals ( outputFormat ) )
			return getMappingsAsXml ( addServices, addServiceCollections, addRepositories, entities );
		
		throw new RuntimeException ( "Invalid format '" + outputFormat + "'" );
	}
}
