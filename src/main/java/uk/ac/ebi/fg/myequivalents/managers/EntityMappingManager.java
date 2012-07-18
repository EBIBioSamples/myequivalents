/*
 * TODO: 
 *   Map as params to web service: http://stackoverflow.com/questions/4654423/how-to-have-a-hashmap-as-webparam-with-jbossws-3-1-2
 */
package uk.ac.ebi.fg.myequivalents.managers;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.JAXBUtils;

/**
 * 
 * <h2>The Entity Manager</h2>
 *
 * <p>Note that this class instantiates a new {@link EntityManager} in its constructor. This makes it an 
 * entity-manager-per-request when the service is accessed via Apache Axis (cause it re-instantiates at every request).
 * The persistence-related invocations does the transaction management automatically (i.e., they commit all implied changes).</p>
 * 
 * <p>You have to decide the lifetime of a EntityMappingManager instance in your application, we suggest to apply the
 * manager-per-request approach</p>
 * 
 * <p>This class is not thread-safe, the idea is that you create a new instance per thread, do some operations, release.</p> 
 *
 * <dl><dt>date</dt><dd>Jun 7, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class EntityMappingManager
{
	private EntityManager entityManager;
	private EntityMappingDAO entityMappingDAO;
	
	public EntityMappingManager ()
	{
		this.entityManager = Resources.getInstance ().getEntityManagerFactory ().createEntityManager ();
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

	public int deleteMappings ( String... entities )
	{
		if ( entities == null || entities.length == 0 ) return 0;
		if ( entities.length % 2 != 0 ) throw new IllegalArgumentException (
		  "Wrong no. of arguments for deleteMappings, I expect a list of serviceName/accession pairs"
		);

		int result = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			if ( entities.length == 2 )
				result = entityMappingDAO.deleteMappings ( entities [ 0 ], entities [ 1 ] );
			else
				result = entityMappingDAO.deleteMappingsForAllEntitites ( entities );
		ts.commit ();
		return result;
	}
	
	public int deleteEntities ( String... entities )
	{
		if ( entities == null || entities.length == 0 ) return 0;
		if ( entities.length % 2 != 0 ) throw new IllegalArgumentException (
		  "Wrong no. of arguments for deleteMappings, I expect a list of serviceName/accession pairs"
		);

		int result = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			if ( entities.length == 2 )
				result = entityMappingDAO.deleteEntity ( entities [ 0 ], entities [ 1 ] ) ? 1 : 0;
			else
				result = entityMappingDAO.deleteEntitites ( entities );
		ts.commit ();
		return result;
	}
	
	
	// TODO: Needs to be simplified into 'complete/non-complete'
	public EntityMappingSearchResult getMappings ( boolean wantRawResult, String... entities )
	{
		EntityMappingSearchResult result = new EntityMappingSearchResult ( wantRawResult );

		if ( entities == null || entities.length == 0 ) return result;
		if ( entities.length % 2 != 0 ) throw new IllegalArgumentException (
		  "Wrong no. of arguments for getMappings, I expect a list of serviceName/accession pairs"
		);
		
		for ( int i = 0; i < entities.length; i++ )
			result.addAllEntityMappings ( entityMappingDAO.findEntityMappings ( entities [ i ], entities [ ++i ]) );
		
		return result;
	}

	
	
	
	private String getMappingsAsXml ( boolean wantRawResult, String... entities )
	{
		return JAXBUtils.marshal ( 
			this.getMappings ( wantRawResult, entities ), 
			EntityMappingSearchResult.class
		);
	}
	
	public String getMappingsAs ( String outputFormat, boolean wantRawResult, String... entities )
	{
		if ( "xml".equals ( outputFormat ) )
			return getMappingsAsXml ( wantRawResult, entities );
		else
			return "<error>Unsopported output format '" + outputFormat + "'</error>";		
	}
}
