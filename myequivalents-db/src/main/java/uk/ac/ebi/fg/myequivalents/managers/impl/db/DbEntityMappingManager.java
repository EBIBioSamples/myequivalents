package uk.ac.ebi.fg.myequivalents.managers.impl.db;
import static uk.ac.ebi.fg.myequivalents.access_control.model.User.Role.EDITOR;

import java.io.Reader;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.ReaderInputStream;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.utils.ManagerUtils;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.JAXBUtils;

/**
 * 
 * <h2>The relational implementation of {@link EntityManager}</h2>
 * 
 * <p>This is the database implementation of the {@link EntityMappingManager} interface, which uses a relational 
 * database connection, via the object model and the {@link EntityMappingDAO DAO}.</p>
 *
 * <dl><dt>date</dt><dd>Jun 7, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DbEntityMappingManager extends DbMyEquivalentsManager implements EntityMappingManager
{
	protected EntityMappingDAO entityMappingDAO;
	
	/**
	 * This logins as anonymous and it's used by the {@link DbManagerFactory} or subclasses.
	 */
	DbEntityMappingManager ( EntityManager em ) {
		this ( em, null, null );
	}
	
	/**
	 * You don't instantiate this class directly, you must use the {@link DbManagerFactory}.
	 * 
	 * This works like the 
	 * {@link DbMyEquivalentsManager#DbMyEquivalentsManager(EntityManager, String, String) super's implementation} 
	 */
	protected DbEntityMappingManager ( EntityManager em, String email, String apiPassword )
	{
		super ( em, email, apiPassword );
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
			userDao.enforceRole ( EDITOR );
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
			userDao.enforceRole ( EDITOR );
		  entityMappingDAO.storeMappingBundle ( entityIds );
		ts.commit ();
	}

	
	@Override
	public void storeMappingBundles ( EntityMappingSearchResult mappings )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.enforceRole ( EDITOR );
		  entityMappingDAO.storeMappingBundles ( mappings );
		ts.commit ();
	}

	@Override
	public void storeMappingBundlesFromXML ( Reader reader )
	{
		EntityMappingSearchResult mappings = JAXBUtils.unmarshal ( 
			new ReaderInputStream ( reader, Charsets.UTF_8 ), EntityMappingSearchResult.class 
		);
		this.storeMappingBundles ( mappings );
	}
	
	
	@Override
	public int deleteMappings ( String... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return 0;

		int result = 0;
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.enforceRole ( EDITOR );
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
			userDao.enforceRole ( EDITOR );
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
		
		User user = userDao.getLoggedInUser ();
		boolean mustBePublic = user == null ? true : !user.hasPowerOf ( EDITOR );

		for ( int i = 0; i < entityIds.length; i++ )
			result.addAllEntityMappings ( entityMappingDAO.findEntityMappings ( entityIds [ i ], mustBePublic ) );
		
		return result;
	}

	@Override
	public EntityMappingSearchResult getMappingsForTarget ( Boolean wantRawResult, String targetServiceName, String entityId )
	{
		if ( wantRawResult == null ) wantRawResult = false;
		EntityMappingSearchResult result = new EntityMappingSearchResult ( wantRawResult );
		
		User user = userDao.getLoggedInUser ();
		boolean mustBePublic = user == null ? true : !user.hasPowerOf ( EDITOR );

		result.addAllEntityMappings ( entityMappingDAO.findMappingsForTarget ( targetServiceName, entityId, mustBePublic ) );
		return result;
	}

	
	/**
	 * Invokes {@link #getMappings(boolean, String...)} and format the result in XML format. 
	 * TODO: document the format. This is based on JAXB and reflects the structure of {@link EntityMappingSearchResult}. 
	 * See {@link uk.ac.ebi.fg.myequivalents.managers.EntityMappingManagerTest} for details. 
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
		ManagerUtils.checkOutputFormat ( outputFormat );
		return getMappingsAsXml ( wantRawResult, entityIds );
	}

	private String getMappingsForTargetAsXml ( Boolean wantRawResult, String targetServiceName, String entityId )
	{
		return JAXBUtils.marshal ( 
			this.getMappingsForTarget ( wantRawResult, targetServiceName, entityId ), 
			EntityMappingSearchResult.class
		);
	}

	@Override
	public String getMappingsForTargetAs ( 
		String outputFormat, Boolean wantRawResult, String targetServiceName, String entityId )
	{
		if ( wantRawResult == null ) wantRawResult = false;

		ManagerUtils.checkOutputFormat ( outputFormat );
		return getMappingsForTargetAsXml ( wantRawResult, targetServiceName, entityId );
	}

}
