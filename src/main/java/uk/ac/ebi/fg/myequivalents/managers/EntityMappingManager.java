package uk.ac.ebi.fg.myequivalents.managers;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
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

	/**
	 * Stores mappings between entities. The parameter consists of a list of quadruples, where every quadruple is a pair
	 * of service/accession. This uses {@link EntityMappingManager#storeMappings(String...)} (see there for details) and
	 * wraps it with the transaction management. You'll get an exception if any of the named services doesn't exist.
	 */
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

	/**
	 * Stores a mapping bundle. The parameter consists of a list of entity references, where every entity is given by a pair
	 * of service/accession. This uses {@link EntityMappingManager#storeMappingBundle(String...)} (see there for details) 
	 * and wraps it with the transaction management. You'll get an exception if any of the named services doesn't exist.
	 */
	public void storeMappingBundle ( String... entities )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		  entityMappingDAO.storeMappingBundle ( entities );
		ts.commit ();
	}

	/**
	 * Deletes mappings between entities. The parameter consists of a list of quadruples, where every quadruple is a pair
	 * of service/accession. This uses {@link EntityMappingManager#storeMappings(String...)} (see there for details) and
	 * wraps it with the transaction management.
	 */
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
	
	/**
	 * Deletes entities. The parameter consists of a list of entity references, where every entity is given by a pair
	 * of service/accession. The entities are removed from any mapping it belonged to (i.e., they disappear altogether). 
	 * This uses {@link EntityMappingManager#deleteEntities(String...)} (see there for details) 
	 * and wraps it with the transaction management. 
	 */
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
	
	/**
	 * Gets all the mappings to which the parameter entities are associated. 
	 * The parameter consists of a list of entity references, where every entity is given by a pair of service/accession. 
	 * This is based on {@link EntityMappingDAO#findEntityMappings(String, String)}.
	 * The result is put into an instance of {@link EntityMappingSearchResult} and available via its methods, e.g., 
	 * {@link EntityMappingSearchResult#getBundles()}. 
	 *  
	 * @param wantRawResult if true, omits service-related objects from the result ({@link Service}, {@link ServiceCollection}, 
	 * {@link Repository}), it only reports bundles of entity mappings. This will be faster if you just needs links.
	 * 
	 */
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

	
	
	/**
	 * Invokes {@link #getMappings(boolean, String...)} and format the result in XML format. 
	 * TODO: document the format. This is based on JAXB and reflects the structure of {@link EntityMappingSearchResult}. 
	 * See {@link EntityMappingManagerTest} for details. 
	 * 
	 */
	private String getMappingsAsXml ( boolean wantRawResult, String... entities )
	{
		return JAXBUtils.marshal ( 
			this.getMappings ( wantRawResult, entities ), 
			EntityMappingSearchResult.class
		);
	}
	
	/**
	 * Returns the result of {@link #getMappings(boolean, String...)} in the specified format. At the moment this is 
	 * only 'xml' and {@link #getRepositoriesAsXml(String...)} is used for this. We plan formats like RDF or JSON for 
	 * the future.
	 */
	public String getMappingsAs ( String outputFormat, boolean wantRawResult, String... entities )
	{
		if ( "xml".equals ( outputFormat ) )
			return getMappingsAsXml ( wantRawResult, entities );
		else
			return "<error>Unsopported output format '" + outputFormat + "'</error>";		
	}
}
