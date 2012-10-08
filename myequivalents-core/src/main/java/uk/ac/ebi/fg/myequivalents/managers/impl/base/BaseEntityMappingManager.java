package uk.ac.ebi.fg.myequivalents.managers.impl.base;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.JAXBUtils;

/**
 * 
 * <h2>The Base Entity Manager</h2>
 * 
 * <p>This is the base implementation of the {@link EntityMappingManager} interface, which uses a relational 
 * database connection, via the object model and the {@link EntityMappingDAO DAO}.</p>
 *
 * <p>Note that this class instantiates a new {@link EntityManager} in its constructor. This makes it an 
 * entity-manager-per-request in many cases (e.g., when accessed by a web service). This should be the best transactional
 * model to use in such cases. You might decide a different approach, by keeping an instance of this class the time
 * you wish.</p>
 * 
 * <p>The persistence-related invocations in this manager does the transaction management automatically 
 * (i.e., they commit all implied changes).</p>
 * 
 * <p>This class is not thread-safe, the idea is that you create a new instance per thread, do some operations and then release it.</p> 
 *
 * <dl><dt>date</dt><dd>Jun 7, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BaseEntityMappingManager implements EntityMappingManager
{
	private EntityManager entityManager;
	private EntityMappingDAO entityMappingDAO;
	
	public BaseEntityMappingManager ()
	{
		this.entityManager = Resources.getInstance ().getEntityManagerFactory ().createEntityManager ();
		this.entityMappingDAO = new EntityMappingDAO ( entityManager );
	}

	@Override
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

	@Override
	public void storeMappingBundle ( String... entityIds )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		  entityMappingDAO.storeMappingBundle ( entityIds );
		ts.commit ();
	}

	@Override
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
	
	@Override
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
	

	@Override
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
	
	@Override
	public String getMappingsAs ( String outputFormat, Boolean wantRawResult, String... entityIds )
	{
		if ( wantRawResult == null ) wantRawResult = false;
		if ( StringUtils.trimToNull ( outputFormat ) == null || "xml".equals ( outputFormat ) )
			return getMappingsAsXml ( wantRawResult, entityIds );
		else
			return "<error>Unsopported output format '" + outputFormat + "'</error>";		
	}
}
