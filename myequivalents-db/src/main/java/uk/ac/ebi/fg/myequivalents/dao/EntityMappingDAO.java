package uk.ac.ebi.fg.myequivalents.dao;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.CacheMode;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.hibernate.jpa.HibernateEntityManager;
import org.hibernate.type.StringType;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.EntityId;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.utils.DbEntityIdResolver;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.JAXBUtils;
import uk.ac.ebi.utils.security.IdUtils;

/**
 * 
 * The DAO for {@link EntityMapping}. This manages basic operations and, as any other DAOs, delegates transaction
 * managements to the outside.
 * 
 * TODO: factorise the queries.
 *
 * <dl><dt>date</dt><dd>May 25, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class EntityMappingDAO extends AbstractTargetedDAO<EntityMapping>
{		
	private final Random random = new Random ( System.currentTimeMillis () );
	
	public EntityMappingDAO ( EntityManager entityManager )
	{
		super ( entityManager, EntityMapping.class );
		this.setEntityIdResolver ( new DbEntityIdResolver ( entityManager ) );
	}	
	
	/**
	 * This calls {@link #storeMapping(String, String, String, String)} after having achieved the entity ID structure
	 * via {@link #entityIdResolver}. 
	 */
	public void storeMapping ( String entityId1, String entityId2 )
	{
		EntityId eid1 = entityIdResolver.doall ( entityId1 );
		EntityId eid2 = entityIdResolver.doall ( entityId2 );
		
		storeMapping ( eid1.getServiceName (), eid1.getAcc (), eid2.getServiceName (), eid2.getAcc () );
	}


	/**
	 * Stores a mapping between two entities, i.e., a link between service1/acc1 and service2/acc2. Works as specified by 
	 * {@link EntityMappingManager#storeMappings(String...)}.
	 * 
	 */
	public void storeMapping ( String serviceName1, String accession1, String serviceName2, String accession2 )
	{
		serviceName1 = StringUtils.trimToNull ( serviceName1 );
		Validate.notNull ( serviceName1, "Cannot work with a null service name (first entity)" );
		
		accession1 = StringUtils.trimToNull ( accession1 );
		Validate.notNull ( accession1, "Cannot work with a null accession (first entity)" );

		serviceName2 = StringUtils.trimToNull ( serviceName2 );
		Validate.notNull ( serviceName2, "Cannot work with a null service name (2nd entity)" );
		
		accession2 = StringUtils.trimToNull ( accession2 );
		Validate.notNull ( accession2, "Cannot work with a null accession (2nd entity)" );

		String bundle1 = this.findBundle ( serviceName1, accession1, true );
		String bundle2 = this.findBundle ( serviceName2, accession2, true );
		
		if ( bundle1 == null )
		{
			if ( bundle2 == null )
				// The mapping doesn't exist at all, create it
				//
				bundle1 = bundle2 = this.create ( serviceName2, accession2 ) ;
			
			// join the 1 side to bundle2
			this.join ( serviceName1, accession1, bundle2 );
			return;
		}
		
		// The same for the symmetric case
		//
		if ( bundle2 == null )
		{
			// bundle1 is not null at this point, join the side 2 to bundle1
			this.join ( serviceName2, accession2, bundle1 );
			return;
		}
		
		// Bundles are both non null
		//
		
		// The mapping already exists
		if ( bundle1.equals ( bundle2 ) ) return;
		
		// They exists, but belongs to different bundles, so we need to merge them
		this.mergeBundles ( bundle1, bundle2 );
	}

	/**
	 * Assumes the array parameter contains pairs of entity IDs (see {@link #entityIdResolver}} and creates
	 * a mapping for each pair (i.e., calls {@link #storeMapping(String, String)}). 
	 * 
	 * Throws an exception if the input is not a multiple of 2.
	 *  
	 */
	public void storeMappings ( String... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return;
		Validate.isTrue ( entityIds.length % 2 == 0, "Wrong no. of arguments for storeMappings, I expect a list of " +
			"(entity-id-1, entity-id-2) pairs" 
		);
		
		for ( int i = 0; i < entityIds.length; i++ )
			storeMapping ( entityIds [ i ], entityIds [ ++ i ] );
	}


	public void storeMappingBundle ( List<Entity> entities )
	{
		if ( entities == null ) return;
		
		int nents = entities.size ();
		
		// Check if there is some entry already in
		//
		for ( int i = 0; i < nents; i++ )
		{
			Entity ei = entities.get ( i );
			String bundle = this.findBundle ( ei.getServiceName (), ei.getAccession (), true );
			if ( bundle != null )
			{
				// There is already a bundle with one of the input entities, so let's attach all of them to this
				for ( int j = 0; j < nents; j++ )
				{
					if ( i == j ) continue;
					Entity ej = entities.get ( j );
					String bundle1 = this.findBundle ( ej.getServiceName (), ej.getAccession (), true );
					if ( bundle.equals ( bundle1 ) ) continue;
					if ( bundle1 == null ) 
						this.join ( ej, bundle );
					else
						this.moveBundle ( bundle1, bundle );
				}
				return;
			}
		} // for i
		
		
		// It has not found any of the entries, so we need to create a new bundle that contains all of them.
		//
		String bundle = null;
		for ( int i = 0; i < nents; i++ )
		{
			Entity e = entities.get ( i );
			if ( bundle == null )
				bundle = this.create ( e );
			else
				this.join ( e, bundle );
		}

	}

	public void storeMappingBundles ( EntityMappingSearchResult mappings )
	{
		if ( mappings == null ) return;
		for ( Bundle bundle: mappings.getBundles () )
			storeMappingBundle ( new ArrayList<> ( bundle.getEntities () ) );
	}

	
	
	/**
	 * Works like specified by {@link EntityMappingManager#storeMappingBundle(String...)}. 
	 * Uses {@link #entityIdResolver} to get the entity ID structure.
	 *  
	 */
	public void storeMappingBundle ( String... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return;
		
		// Check if there is some entry already in
		//
		for ( int i = 0; i < entityIds.length; i++ )
		{
			EntityId eid = entityIdResolver.doall ( entityIds [ i ] );
			
			String bundle = this.findBundle ( eid.getServiceName (), eid.getAcc (), true );
			if ( bundle != null )
			{
				// There is already a bundle with one of the input entities, so let's attach all of them to this
				for ( int j = 0; j < entityIds.length; j++ )
				{
					if ( i == j ) continue; 
					EntityId jeid = entityIdResolver.doall ( entityIds [ j ] );
					String serviceNameJ = jeid.getServiceName (), accessionJ = jeid.getAcc ();
					String bundle1 = this.findBundle ( serviceNameJ, accessionJ, true );
					if ( bundle.equals ( bundle1 ) ) continue;
					if ( bundle1 == null ) 
						this.join ( serviceNameJ, accessionJ, bundle );
					else
						this.moveBundle ( bundle1, bundle );
				}
				
				// And then we're done
				return;
			}
		} // for i
		
		
		// It has not found any of the entries, so we need to create a new bundle that contains all of them.
		//
		String bundle = null;
		for ( int i = 0; i < entityIds.length; i++ )
		{
			EntityId eid = entityIdResolver.doall ( entityIds [ i ] );
			
			if ( bundle == null )
				bundle = this.create ( eid.getServiceName (), eid.getAcc () );
			else
				this.join ( eid.getServiceName (), eid.getAcc (), bundle );
		}
	}
	
	
	/**
	 * Uses {@link #entityIdResolver} to get the entity ID structure contained by the parameter 
	 * and then invokes {@link #deleteEntity(String, String)}.
	 */
	public boolean deleteEntity ( String entityId ) 
	{
		EntityId eid = entityIdResolver.doall ( entityId );
		return deleteEntity ( eid.getServiceName (), eid.getAcc () );
	}
	
	
	/**
	 * Deletes an entity from the database, i.e., it removes it from any equivalence/mapping relation it is involved in.
	 *  
	 * @return true if the entity was in the DB and it was removed, false if that wasn't the case and the database was
	 * left unchanged.
	 */
	public boolean deleteEntity ( String serviceName, String accession )
	{
		// Invalid values
		serviceName = StringUtils.trimToNull ( serviceName ); if ( serviceName == null ) return false;
		accession = StringUtils.trimToNull ( accession ); if ( accession == null ) return false;

		String bundle = findBundle ( serviceName, accession );
		if ( bundle == null ) return false;
		
		entityManager.createNativeQuery (
			"DELETE FROM entity_mapping WHERE service_name = '" + serviceName + "' AND accession = '" + accession + "'"
		).executeUpdate ();
		
		// If the bundle is left with 1 member only, must go away
		if ( ((Number) entityManager.createNativeQuery ( 
			"SELECT COUNT( bundle ) AS ct FROM entity_mapping WHERE bundle = '" + bundle + "'" 
		).getSingleResult () ).longValue () == 1 )
			entityManager.createNativeQuery ( "DELETE FROM entity_mapping WHERE bundle = '" + bundle + "'" ).executeUpdate ();
		
		return true;
	}
	
	/**
	 * Assumes the input is a set of entity IDs and removes all the corresponding entities, 
	 * via {@link #deleteEntity(String)}.
	 * 
	 * @return the number of entities that were deleted, i.e., the number of times {@link #deleteEntity(String, String)}
	 * returned true. 
	 * 
	 */
	public int deleteEntitites ( String... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return 0;

		int ct = 0;
		for ( int i = 0; i < entityIds.length; i++ )
			ct += this.deleteEntity ( entityIds [ i ] ) ? 1 : 0;
		
		return ct;
	}
	
	/**
	 * Uses {@link #entityIdResolver} to get the entity ID structure contained in the parameter, then invokes 
	 * {@link #deleteMappings(String, String)} with it. 
	 * 
	 */
	public int deleteMappings ( String entityId ) 
	{
		EntityId eid = entityIdResolver.doall ( entityId );
		return deleteMappings ( eid.getServiceName (), eid.getAcc () );
	}
	
	
	/**
	 * Deletes all the mappings that involve the entity, i.e., the equivalence class it belongs to.
	 *  
	 * @return the number of entities (including the parameter) that were in the same equivalence relationship and are
	 * now deleted. Returns 0 if no such mapping exists.
	 *  
	 */
	public int deleteMappings ( String serviceName, String accession )
	{
		// Invalid values
		serviceName = StringUtils.trimToNull ( serviceName ); if ( serviceName == null ) return 0;
		accession = StringUtils.trimToNull ( accession ); if ( accession == null ) return 0;

		String bundle = this.findBundle ( serviceName, accession, true );
		return bundle == null ? 0 : this.deleteBundle ( bundle );
	}

	/**
	 * Assumes the input is a list of entity IDs and deletes all the relations such entities are involved
	 * in, via {@link #deleteMappings(String)}.  
	 *  
	 * This call throws an exception if the input is not a multiple of 2.
	 * 
	 * @returns the total number of entities related to the parameters (including the latter) that are now deleted.
	 * 
	 */
	public int deleteMappingsForAllEntitites ( String... entityIds )
	{
		if ( entityIds == null || entityIds.length == 0 ) return 0;

		int ct = 0;
		for ( int i = 0; i < entityIds.length; i++ )
			ct += this.deleteMappings ( entityIds [ i ] );
		
		return ct;
	}
	
	/**
	 * Uses {@link #entityIdResolver} to get the entity ID structure contained in the parameter, then invokes 
	 * {@link #findMappings(String, String)}.
	 */
	public List<String> findMappings ( String entityId, boolean mustBePublic )
	{
		EntityId eid = entityIdResolver.doall ( entityId );
		return findMappings ( eid.getServiceName (), eid.getAcc (), mustBePublic );
	}

	/** Defaults to mustBePublic = true */
	public List<String> findMappings ( String entityId ) {
		return findMappings ( entityId, true );
	}

	
	
	/**
	 * Finds all the entities that are related to the parameter.
	 * 
	 * @return a possibly empty list of (serviceName, accession) pair. It <b>does include the parameter in the result</b>.
	 * It returns an empty list if either parameter is empty. It never returns null.
	 * 
	 */
	public List<String> findMappings ( String serviceName, String accession, final boolean mustBePublic )
	{
		final String serviceNameTrim = StringUtils.trimToNull ( serviceName );
		final String accessionTrim = StringUtils.trimToNull ( accession );
		final List<String> result = new ArrayList<String> ();
		if ( serviceName == null || accession == null ) return result; 
		
		final String sql = mustBePublic
			?
				"SELECT em1.service_name AS service_name, em1.accession AS accession\n" +
				"FROM entity_mapping em1, entity_mapping em2\n" + 
				"WHERE em1.bundle = em2.bundle AND em2.service_name = ? AND em2.accession = ?\n" +
				// First of all the parameter entity must be public (or, transitively, one of its containers)
				"AND (\n" + 
				"  (em2.public_flag = 1 OR em2.public_flag IS NULL AND em2.release_date IS NOT NULL AND em2.release_date <= ? )\n" + 
				"  OR em2.public_flag IS NULL AND em2.release_date IS NULL AND em2.service_name IN (" +
				"    SELECT name FROM service s WHERE ( s.public_flag = 1 OR s.public_flag IS NULL AND s.release_date IS NOT NULL AND s.release_date <= ? )\n" +
				"      OR (s.public_flag IS NULL AND s.release_date IS NULL AND s.repository_name IN " +
				"        (SELECT name FROM repository r WHERE r.public_flag = 1 OR r.public_flag IS NULL AND ( r.release_date IS NOT NULL AND r.release_date <= ? ) )" +
				"    )\n" +
				"  )\n" +
				")\n" + 
				// then, all the linked entities must be pub too 
				"AND (\n" + 
				"  (em1.public_flag = 1 OR em1.public_flag IS NULL AND em1.release_date IS NOT NULL AND em1.release_date <= ? )\n" + 
				"  OR em1.public_flag IS NULL AND em1.release_date IS NULL AND em1.service_name IN (" +
				"    SELECT name FROM service s WHERE ( s.public_flag = 1 OR s.public_flag IS NULL AND s.release_date IS NOT NULL AND s.release_date <= ? )\n" +
				"      OR (s.public_flag IS NULL AND s.release_date IS NULL AND s.repository_name IN " +
				"        (SELECT name FROM repository r WHERE r.public_flag = 1 OR r.public_flag IS NULL AND ( r.release_date IS NOT NULL AND r.release_date <= ? ) )" +
				"    )\n" +
				"  )\n" +
				")" 
			:
				"SELECT em1.service_name AS service_name, em1.accession AS accession FROM entity_mapping em1, entity_mapping em2\n" +
				"  WHERE em1.bundle = em2.bundle AND em2.service_name = ? AND em2.accession = ?";
		
		((HibernateEntityManager)entityManager).getSession ().doWork ( new Work() {
			@Override
			public void execute ( Connection conn ) throws SQLException {
				PreparedStatement stmt = conn.prepareStatement ( sql );
				stmt.setString ( 1, serviceNameTrim );
			  stmt.setString ( 2, accessionTrim );
			  if ( mustBePublic ) {
			  	java.sql.Date now = new java.sql.Date ( System.currentTimeMillis () );
			  	for ( int i = 3; i <= 8; i++ ) stmt.setDate ( i, now );
			  }
				for ( ResultSet rs = stmt.executeQuery (); rs.next (); )
				{
					String serviceNamei = rs.getString ( "service_name" ), accessioni = rs.getString ( "accession" );
					result.add ( serviceNamei ); 
					result.add ( accessioni ); 
				}
			}} 
		);
		
		return result;
	}

	/** Defaults to mustBePublic = true */
	public List<String> findMappings ( String serviceName, String accession ) {
		return findMappings ( serviceName, accession, true );
	}

	
	/**
	 * Uses {@link #entityIdResolver} to get the entity ID structure contained in the parameter, then invokes
	 * {@link #findEntityMappings(String, String)}.
	 * 
	 */
	public List<EntityMapping> findEntityMappings ( String entityId, boolean mustBePublic ) 
	{
		EntityId eid = entityIdResolver.doall ( entityId );
		return findEntityMappings ( eid.getServiceName (), eid.getAcc (), mustBePublic );
	}
	
	/** Defaults to mustBePublic = true */
	public List<EntityMapping> findEntityMappings ( String entityId ) {
		return findEntityMappings ( entityId, true );
	}

	/**
	 * The same as {@link #findMappings(String, String)}, but returns a list of {@link EntityMapping}s from which 
	 * services can be fetched. Whether you need this or the version that returns strings, it depends on the type of
	 * result needed, e.g., for raw results only the string-result version is faster, for complete results, this version
	 * is faster and more practical.
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	public List<EntityMapping> findEntityMappings ( String serviceName, String accession, boolean mustBePublic )
	{
		serviceName = StringUtils.trimToNull ( serviceName );
		accession = StringUtils.trimToNull ( accession );
		if ( serviceName == null || accession == null ) return new ArrayList<EntityMapping> (); 

		String queryName = mustBePublic ? "getPublicMappings" : "getAllMappings";
		Query q = entityManager.createNamedQuery ( queryName, EntityMapping.class )
			.setParameter ( "serviceName", serviceName )
			.setParameter ( "accession", accession );
		return q.getResultList ();
	}
	
	/** Defaults to mustBePublic = true */
	public List<EntityMapping> findEntityMappings ( String serviceName, String accession ) {
		return findEntityMappings ( serviceName , accession, true );
	}

	
	/**
	 * Finds a single {@link EntityMapping}, ignoring all the involved mappings.
	 */
	@SuppressWarnings ( "unchecked" )
	public EntityMapping findEntityMapping ( String serviceName, String accession, boolean mustBePublic )
	{
		serviceName = StringUtils.trimToNull ( serviceName );
		accession = StringUtils.trimToNull ( accession );
		if ( serviceName == null || accession == null ) return null; 
		
		String queryName = mustBePublic ? "findPublicEntityMapping" : "findEntityMapping";
		Query q = entityManager.createNamedQuery ( queryName, EntityMapping.class )
			.setParameter ( "serviceName", serviceName )
			.setParameter ( "accession", accession );
		
		List<EntityMapping> result = q.getResultList ();
		
		return result == null || result.isEmpty () ? null : result.get ( 0 );
	}

	/** Defaults to mustBePublic = true */
	public EntityMapping findEntityMapping ( String serviceName, String accession ) {
		return findEntityMapping ( serviceName, accession, true );
	}

	
	/** 
	 * Works like {@link #findEntityMappings(String, String, boolean)}, splits the ID into its chunks. 
	 */
	public EntityMapping findEntityMapping ( String entityId, boolean mustBePublic ) 
	{
		EntityId eid = entityIdResolver.doall ( entityId );
		return findEntityMapping ( eid.getServiceName (), eid.getAcc (), mustBePublic );
	}

	/** Defaults to mustBePublic = true */
	public EntityMapping findEntityMapping ( String entityId ) {
		return findEntityMapping ( entityId, true );
	} 

	
	public List<EntityMapping> findMappingsForTarget ( String targetServiceName, String entityId, boolean mustBePublic)
	{
		EntityId eid = entityIdResolver.doall ( entityId );
		return findMappingsForTarget ( targetServiceName, eid.getServiceName (), eid.getAcc (), mustBePublic );
	}

	/** Defaults to mustBePublic = true */
	public List<EntityMapping> findMappingsForTarget ( String targetServiceName, String entityId ) {
		return findMappingsForTarget ( targetServiceName, entityId, true );
	}

	
	/**
	 * Returns the entity IDs of those entities that are equivalent to the input entity and belong to the 
	 * specified target service. It returns a list, cause nothing forbids to map an entity into multiple ones 
	 * onto the same target service. 
	 *   
	 */
	@SuppressWarnings ( "unchecked" )
	public List<EntityMapping> findMappingsForTarget ( String targetServiceName, String serviceName, String accession, boolean mustBePublic ) 
	{
		serviceName = StringUtils.trimToNull ( serviceName );
		accession = StringUtils.trimToNull ( accession );
		targetServiceName = StringUtils.trimToNull ( targetServiceName );
		if ( serviceName == null || accession == null || targetServiceName == null ) return new ArrayList<EntityMapping> (); 

		String queryName = mustBePublic ? "findPublicMappingsForTarget" : "findMappingsForTarget";
		Query q = entityManager.createNamedQuery ( queryName, EntityMapping.class )
			.setParameter ( "serviceName", serviceName )
			.setParameter ( "accession", accession )
			.setParameter ( "targetServiceName", targetServiceName );
		
		return q.getResultList ();
	}
	
	/** Defaults to mustBePublic = true */
	public List<EntityMapping> findMappingsForTarget ( String targetServiceName, String serviceName, String accession ) {
		return findMappingsForTarget ( targetServiceName, serviceName, accession, true );
	} 

	
	/**
	 * Creates a new {@link EntityMapping}, assigning a new bundle. This is a wrapper of {@link #create(String, String, String)}, 
	 * with bundle = null.
	 * 
	 * @return the new bundle ID.
	 *  
	 */
	private String create ( String serviceName, String accession )
	{
		return create ( serviceName, accession, null );
	}

	/**
	 * Joins an entity to an existing bundle, i.e., creates a new {@link EntityMapping} with the parameters. This is 
	 * a wrapper to {@link #create(String, String, String)} with an exception in the case that bundle == null. 
	 *  
	 */
	private String join ( String serviceName, String accession, String bundle )
	{
		if ( bundle == null ) throw new RuntimeException (
			"Cannot work with an empty bundle ID"
		);
		return create ( serviceName, accession, bundle );
	}

	private String join ( Entity e, String bundle )
	{
		if ( bundle == null ) throw new RuntimeException (
			"Cannot work with an empty bundle ID"
		);
		return create ( e, bundle );
	}

	
	/**
	 * Creates a new {@link EntityMapping}, by first creating a new bundle (via {@link #createNewBundleId()}) 
	 * if the corresponding parameter is null.
	 * 
	 * @return the newly created bundle or the bundle parameter if this is not null. 
	 */
	private String create ( String serviceName, String accession, String bundle )
	{
		if ( bundle == null )
			bundle = createNewBundleId ();
		
		Query q = entityManager.createNativeQuery (   
			"INSERT INTO entity_mapping (service_name, accession, bundle) VALUES ( :serviceName, :acc, :bundle )" 
		);
		q.setParameter ( "serviceName", serviceName );
		q.setParameter ( "acc", accession );
		q.setParameter ( "bundle", bundle );
		
		// TODO: check the return value
		q.executeUpdate ();
		return bundle;
	}
		
	
	private String create ( Entity e, String bundle )
	{
		if ( bundle == null )
			bundle = createNewBundleId ();
		
		Date relDate = e.getReleaseDate ();
		Boolean pubFlag = e.getPublicFlag ();
		
		Query q = entityManager.createNativeQuery ( String.format (   
			"INSERT INTO entity_mapping (service_name, accession, bundle, %s %s) "
			+ "VALUES ( :serviceName, :acc, :bundle, %s %s)", 
			(relDate == null ? "" : "release_date," ),
			(pubFlag == null ? "" : "public_flag" ),
			(relDate == null ? "" : ":release_date," ),
			(pubFlag == null ? "" : ":public_flag" )
		));
		q.setParameter ( "serviceName", e.getServiceName () );
		q.setParameter ( "acc", e.getAccession () );
		q.setParameter ( "bundle", bundle );
		if ( relDate != null ) q.setParameter ( "release_date", e.getReleaseDate () );
		if ( pubFlag != null ) q.setParameter ( "public_flag", pubFlag ? 1 : 0 );
		
		// TODO: check the return value
		q.executeUpdate ();
		return bundle;
	}

	private String create ( Entity e )
	{
		return create ( e, null );
	}

	
	/**
	 * Merges two bundles, i.e., takes all the {@link EntityMapping} having one of the two bundle IDs and replace their
	 * bundle ID with the value of the other parameter. Due to optimisation needs, the method selects which bundle to
	 * change randomly (so you cannot know which one is preserved after the operation). 
	 * 
	 * @returns the new unique bundle that there is now in the DB (i.e., either bundle1 or bundle2, the other bundle
	 * doesn't exist anymore and was merged with the one that is returned).
	 * 
	 * This low-level operation is used to store possibly new mappings. It is essentially a random swap of the 
	 * parameters, followed by {@link #moveBundle(String, String)}.
	 *  
	 */
	private String mergeBundles ( String bundle1, String bundle2 )
	{
		bundle1 = StringUtils.trimToNull ( bundle1 );
		bundle2 = StringUtils.trimToNull ( bundle2 );

		// Bundles have roughly the same size, let's choose which one to update randomly, just to make the no. of times
		// that the smaller bundle is chosen uniform.
		//
		if ( random.nextBoolean () )
		{
			String tmp = bundle1;
			bundle1 = bundle2;
			bundle2 = tmp;
		}

		moveBundle ( bundle1, bundle2 );
		return bundle2;
	}

	/**
	 * Moves srcBundle to destBundle, i.e., replaces all the {@link EntityMapping}(s) in srcBundle with destBundle.
	 * This low-level operation is used to store possibly new mappings.
	 * 
	 */
	private void moveBundle ( String srcBundle, String destBundle )
	{
		srcBundle = StringUtils.trimToNull ( srcBundle );
		destBundle = StringUtils.trimToNull ( destBundle );
		
		Query q = entityManager.createNativeQuery ( 
			"UPDATE entity_mapping SET bundle = '" + destBundle + "' WHERE bundle = '" + srcBundle + "'" 
		);
		// TODO: check > 0
		q.executeUpdate ();
	}
	

	/**
	 * Wraps isForUpdate = false 
	 */
	private String findBundle ( String serviceName, String accession )
	{
		return findBundle ( serviceName, accession, false );
	}

	
	/**
	 * @return the ID of the bundle that the parameter belongs to. null if there is no such bundle.
	 * isForUpdate = true is used in DAO methods that need to perform changes on the {@link EntityMapping}
	 * 
	 * table. Setting the flag cause tells this method to use SQL-locking mechanisms, such as FOR UPDATE, 
	 * which allows the DB to know that the table/records involved in the query are being updated within 
	 * a transaction.
	 *  
	 */
	private String findBundle ( String serviceName, String accession, boolean isForUpdate )
	{
		String sql = "SELECT bundle FROM entity_mapping\n"
			+ "  WHERE service_name = :serviceName AND accession = :accession";
		if ( isForUpdate ) sql += " FOR UPDATE";

		Query q = entityManager.createNativeQuery ( sql )
			.setParameter ( "serviceName", serviceName )
			.setParameter ( "accession", accession );
		
		// we cannot use this together with FOR UPDATE, since the extra syntax (LIMIT, rownum <= 1) is placed
		// after FOR UPDATE. We haven't notice a significant performance change anyway, and we're keeping this restriction
		// for read-only queries just in case.
		//
		if ( !isForUpdate ) q.setMaxResults ( 1 );
		
		// We've seen this problem (http://stackoverflow.com/questions/4873201/hibernate-native-query-char3-column)
		q.unwrap ( SQLQuery.class ).addScalar ( "bundle", StringType.INSTANCE );
				
		@SuppressWarnings ( "unchecked" )
		List<String> results = q.getResultList ();
		
		return results.isEmpty () ? null : results.iterator ().next ();
	}
	
	
	/**
	 * Deletes a bundle by ID, i.e., removes all the {@link EntityMapping} that have the parameter as bundle ID.  
	 * 
	 */
	private int deleteBundle ( String bundle )
	{
		return entityManager.createNativeQuery ( "DELETE FROM entity_mapping WHERE bundle = :bundle" )
			.setParameter ( "bundle", bundle )
			.executeUpdate ();
	}
	
	
	
	public int dump ( OutputStream out, Integer offset, Integer limit, double randomQuota )
	{
		int result = 0;
		
		Random rnd = new Random ();
		
		Session session = (Session) this.entityManager.getDelegate ();
		
		String sql = "SELECT bundle, service_name, accession, release_date, public_flag FROM entity_mapping ORDER BY bundle";
	
		SQLQuery qry = session.createSQLQuery ( sql );
		
		// TODO: needs hibernate.jdbc.batch_size?
		qry
			.setReadOnly ( true )
			.setFetchSize ( 1000 )
			.setCacheable ( false )
			.setCacheMode ( CacheMode.IGNORE );

		if ( offset != null && offset >= 0 ) qry.setFirstResult ( offset );
		if ( limit != null && offset < Integer.MAX_VALUE ) qry.setMaxResults ( limit );
		
		List<String> entityIds = new ArrayList<> ();
		List<Date> relDates = new ArrayList<> ();
		List<Boolean> pubFlags = new ArrayList<> ();
		String prevBundle = null;
		
		for ( ScrollableResults rs = qry.scroll ( ScrollMode.FORWARD_ONLY ); rs.next (); )
		{
			result++;
			
			String bundle = (String) rs.get ( 0 );
			
			if ( prevBundle == null ) prevBundle = bundle;
	
			String serviceName = (String) rs.get ( 1 ), acc = (String) rs.get ( 2 );
			String entityId = serviceName + ":" + acc;
			
			if ( !bundle.equals ( prevBundle ) )
			{
				// Jump a random amount of data
				if ( rnd.nextDouble () >= randomQuota ) continue;
				
				// Now dump what we got so far
				//
				EntityMappingSearchResult maps = new EntityMappingSearchResult ( true );
				List<EntityMapping> ents = new LinkedList<EntityMapping> ();
	
				int i = 0;
				for ( String thisEntityId: entityIds )
				{
					EntityId eid = entityIdResolver.doall ( thisEntityId );
					Service service = new Service ( eid.getServiceName () );
					
					EntityMapping ent = new EntityMapping ( service, eid.getAcc (), prevBundle );
					
					ent.setReleaseDate ( relDates.get ( i ) );
					ent.setPublicFlag ( pubFlags.get ( i ) );
					
					ents.add ( ent );
					i++;
				}
											
				maps.addAllEntityMappings ( ents );
				
				JAXBUtils.marshal ( 
					maps.getBundles ().iterator ().next (), Bundle.class, out, Marshaller.JAXB_FRAGMENT, true  
				);
				
				// reset all the accumulators
				entityIds = new ArrayList<> ();
				relDates = new ArrayList<> ();
				pubFlags = new ArrayList<> ();
	
				// Start with a new bundle
				prevBundle = bundle;
				
			} // if bundle changed
			
			
			entityIds.add ( entityId );
			relDates.add ( (Date) rs.get ( 3 ) );
			BigDecimal pubFlag = ((BigDecimal) rs.get ( 4 ));
			pubFlags.add ( pubFlag == null ? null : pubFlag.intValue () == 1 );
			
		} // for rs
			
		return result;
		
	} // dump ()

	
	/**
	 * Creates a new bundle ID to create a new bundle with the parameter. This is supposed to be used when a new bundle 
	 * is being inserted in the DB, i.e., because the parameter entity is not in any other stored bundle yet. 
	 * 
	 * You should assume the method returns an opaque string which of value depends 1-1 from the parameter. 
	 * 
	 * At the moment it uses UUIDs, encoded in BASE64. This generates some overhead in both space and time, but we 
	 * prefer to deal with string IDs, rather than not so portable byte arrays.
	 *  
	 */
	private static String createNewBundleId ()
	{
		return IdUtils.createCompactUUID ();
	}

	public void setEntityManager ( EntityManager entityManager )
	{
		this.entityManager = entityManager;
	}

}
