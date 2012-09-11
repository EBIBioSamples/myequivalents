package uk.ac.ebi.fg.myequivalents.managers;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

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
 * TODO: Use {@link Validate} instead of manual validation.
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
	public void storeMappings ( String... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return;
		if ( entityIds.length % 2 != 0 ) throw new IllegalArgumentException (
		  "Wrong no. of arguments for storeMappings, I expect a list of (serviceName1/accession1, serviceName2/accession2) quadruples"
		);
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			if ( entityIds.length == 2 )
				entityMappingDAO.storeMapping ( entityIds [ 0 ], entityIds [ 1 ] );
			else
				entityMappingDAO.storeMappings ( entityIds );
		ts.commit ();
	}

	/**
	 * Stores a mapping bundle. The parameter consists of a list of entity references, where every entity is given by a pair
	 * of service/accession. This uses {@link EntityMappingManager#storeMappingBundle(String...)} (see there for details) 
	 * and wraps it with the transaction management. You'll get an exception if any of the named services doesn't exist.
	 */
	public void storeMappingBundle ( String... entityIds )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		  entityMappingDAO.storeMappingBundle ( entityIds );
		ts.commit ();
	}

	/**
	 * Deletes mappings between entities. The parameter consists of a list of quadruples, where every quadruple is a pair
	 * of service/accession. This uses {@link EntityMappingManager#storeMappings(String...)} (see there for details) and
	 * wraps it with the transaction management.
	 */
	public int deleteMappings ( String... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return 0;

		int result = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			if ( entityIds.length == 1 )
				result = entityMappingDAO.deleteMappings ( entityIds [ 0 ] );
			else
				result = entityMappingDAO.deleteMappingsForAllEntitites ( entityIds );
		ts.commit ();
		return result;
	}
	
	/**
	 * Deletes entities. The parameter consists of a list of entity references, where every entity is given by a pair
	 * of service/accession. The entities are removed from any mapping it belonged to (i.e., they disappear altogether). 
	 * This uses {@link EntityMappingManager#deleteEntities(String...)} (see there for details) 
	 * and wraps it with the transaction management. 
	 */
	public int deleteEntities ( String... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return 0;

		int result = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			if ( entityIds.length == 1 )
				result = entityMappingDAO.deleteEntity ( entityIds [ 0 ] ) ? 1 : 0;
			else
				result = entityMappingDAO.deleteEntitites ( entityIds );
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
	public EntityMappingSearchResult getMappings ( Boolean wantRawResult, String... entityIds )
	{
		if ( wantRawResult == null ) wantRawResult = false;
		EntityMappingSearchResult result = new EntityMappingSearchResult ( wantRawResult );

		if ( entityIds == null || entityIds.length == 0 ) return result;
		
		for ( int i = 0; i < entityIds.length; i++ )
			result.addAllEntityMappings ( entityMappingDAO.findEntityMappings ( entityIds [ i ] ) );
		
		return result;
	}

	
	
	/**
	 * Invokes {@link #getMappings(boolean, String...)} and format the result in XML format. 
	 * TODO: document the format. This is based on JAXB and reflects the structure of {@link EntityMappingSearchResult}. 
	 * See {@link EntityMappingManagerTest} for details. 
	 * 
	 */
	private String getMappingsAsXml ( boolean wantRawResult, String... entityIds )
	{
		return JAXBUtils.marshal ( 
			this.getMappings ( wantRawResult, entityIds ), 
			EntityMappingSearchResult.class
		);
	}
	
	/**
	 * Returns the result of {@link #getMappings(boolean, String...)} in the specified format. At the moment this is 
	 * only 'xml' and {@link #getRepositoriesAsXml(String...)} is used for this. We plan formats like RDF or JSON for 
	 * the future.
	 */
	public String getMappingsAs ( String outputFormat, Boolean wantRawResult, String... entityIds )
	{
		if ( wantRawResult == null ) wantRawResult = false;
		if ( StringUtils.trimToNull ( outputFormat ) == null || "xml".equals ( outputFormat ) )
			return getMappingsAsXml ( wantRawResult, entityIds );
		else
			return "<error>Unsopported output format '" + outputFormat + "'</error>";		
	}
}
