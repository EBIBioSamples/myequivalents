package uk.ac.ebi.fg.myequivalents.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.Work;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 25, 2012</dd></dl>
 * @author brandizi
 *
 */
public class EntityMappingDAO
{
	private EntityManager entityManager;
	private final MessageDigest messageDigest;
	private final Random random = new Random ( System.currentTimeMillis () );
	
	public EntityMappingDAO ( EntityManager entityManager )
	{
		this.entityManager = entityManager;
		
		try {
			messageDigest = MessageDigest.getInstance ( "SHA1" );
		} 
		catch ( NoSuchAlgorithmException ex ) {
			throw new RuntimeException ( "Internal error, cannot get the SHA1 digester from the JVM", ex );
		}
	}

	
	public void storeMapping ( String serviceName1, String accession1, String serviceName2, String accession2 )
	{
		serviceName1 = StringUtils.trimToNull ( serviceName1 );
		if ( serviceName1 == null ) throw new IllegalArgumentException (
			"Cannot work with a null service name"
		);
		
		accession1 = StringUtils.trimToNull ( accession1 );
		if ( accession1 == null ) throw new IllegalArgumentException (
			"Cannot work with a null accession"
		);

		serviceName2 = StringUtils.trimToNull ( serviceName2 );
		if ( serviceName2 == null ) throw new IllegalArgumentException (
			"Cannot work with a null service name"
		);
		accession2 = StringUtils.trimToNull ( accession2 );
		if ( accession2 == null ) throw new IllegalArgumentException (
			"Cannot work with a null accession"
		);

		String bundle1 = this.findBundle ( serviceName1, accession1 );
		String bundle2 = this.findBundle ( serviceName2, accession2 );
		
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
		this.merge ( bundle1, bundle2 );
	}

	public void storeMappings ( String... entities )
	{
		if ( entities == null || entities.length == 0 ) return;
		if ( entities.length % 4 != 0 ) throw new IllegalArgumentException (
		  "Wrong no. of arguments for storeMappings, I expect a list of (serviceName1/accession1, serviceName2/accession2) quadruples"
		);
		
		for ( int i = 0; i < entities.length; i++ )
			storeMapping ( entities [ i ], entities [ ++i ], entities [ ++i ], entities [ ++i ] );
	}

	
	public void storeMappingBundle ( String... entities )
	{
		if ( entities == null || entities.length == 0 ) return;
		if ( entities.length % 2 != 0 ) throw new IllegalArgumentException (
		  "Wrong no. of arguments for storeMappings, I expect a list of serviceName/accession pairs"
		);

		// Some further input validation
		//
		for ( int i = 0; i < entities.length; i++ )
		{
			String serviceName = entities [ i ];
			String accession = entities [ ++i ];
			
			serviceName = StringUtils.trimToNull ( serviceName );
			if ( serviceName == null ) throw new IllegalArgumentException (
				"Cannot work with a null service name"
			);
			
			accession = StringUtils.trimToNull ( accession );
			if ( accession == null ) throw new IllegalArgumentException (
				"Cannot work with a null accession"
			);
		}
		
		// Check if there is some entry already in
		//
		for ( int i = 0; i < entities.length; i++ )
		{
			String bundle = this.findBundle ( entities [ i ], entities [ ++i ] );
			if ( bundle != null )
			{
				i--;
				// There is already a bundle with one of the input entities, so let's attach all of them to this
				for ( int j = 0; j < entities.length; j++ )
				{
					if ( i == j ) continue;
					String serviceName = entities [ j ], accession = entities [ ++j ];
					String bundle1 = this.findBundle ( serviceName, accession );
					if ( bundle.equals ( bundle1 ) ) continue;
					if ( bundle1 == null ) 
						this.join ( serviceName, accession, bundle );
					else
						this.moveBundle ( bundle1, bundle );
				}
				return;
			}
		} // for i
		
		
		// It has not found any of the entries, so we need to create a new bundle that contains all of them.
		//
		String bundle = null;
		for ( int i = 0; i < entities.length; i++ )
			if ( bundle == null )
				bundle = this.create ( entities [ i ], entities [ ++i ] );
			else
				this.join ( entities [ i ], entities [ ++i ], bundle );
	}
	
	
	public int deleteEntity ( String serviceName, String accession )
	{
		// Invalid values
		serviceName = StringUtils.trimToNull ( serviceName ); if ( serviceName == null ) return 0;
		accession = StringUtils.trimToNull ( accession ); if ( accession == null ) return 0;

		return entityManager.createNativeQuery (
			"DELETE FROM entity_mapping WHERE service_name = '" + serviceName + "' AND accession = '" + accession + "'"
		).executeUpdate ();
	}
	
	public int deleteEntitites ( String... entities )
	{
		if ( entities == null || entities.length == 0 ) return 0;
		if ( entities.length % 2 != 0 ) throw new IllegalArgumentException (
		  "Wrong no. of arguments for storeMappings, I expect a list of serviceName/accession pairs"
		);

		int ct = 0;
		for ( int i = 0; i < entities.length; i++ )
			ct += this.deleteEntity ( entities [ i ], entities [ ++i ] );
		
		return ct;
	}

	
	
	
	/**
	 * Deletes all the mappings that involve the entity, ie, the equivalence class it belongs in. 
	 * @return the number of deleted entities (including the two parameters). Returns 0 if this mapping doesn't exist.
	 *  
	 */
	public int deleteMappings ( String serviceName, String accession )
	{
		// Invalid values
		serviceName = StringUtils.trimToNull ( serviceName ); if ( serviceName == null ) return 0;
		accession = StringUtils.trimToNull ( accession ); if ( accession == null ) return 0;

		String bundle = this.findBundle ( serviceName, accession );
		return bundle == null ? 0 : this.deleteBundle ( bundle );
	}

	public int deleteMappings ( String... entities )
	{
		// TODO: implement me!
		return -1;
	}
	
	
	private String create ( String serviceName, String accession )
	{
		return create ( serviceName, accession, null );
	}

	private String join ( String serviceName, String accession, String bundle )
	{
		return create ( serviceName, accession, bundle );
	}
	
	private String create ( String serviceName, String accession, String bundle )
	{
		if ( bundle == null )
			bundle = createNewBundleId ( serviceName, accession );
		
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
		
	
	private String merge ( String bundle1, String bundle2 )
	{
		bundle1 = StringUtils.trimToNull ( bundle1 );
		bundle2 = StringUtils.trimToNull ( bundle2 );

		// Bundles have roughly the same size, let's choose the one to update randomly, just to make the the no. of times
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

	
	public List<String> findMappings ( String serviceName, String accession )
	{
		serviceName = StringUtils.trimToNull ( serviceName );
		accession = StringUtils.trimToNull ( accession );
		final List<String> result = new ArrayList<String> ();
		if ( serviceName == null || accession == null ) return result; 
		
		final String sql = 
			"SELECT em1.service_name AS service_name, em1.accession AS accession FROM entity_mapping em1, entity_mapping em2\n" +
			"  WHERE em1.bundle = em2.bundle AND em2.service_name = '" + serviceName + "' AND em2.accession = '" + accession + "'";
		
		final String serviceName1 = serviceName, accession1 = accession; // Because anonymous methods want constants.
		((HibernateEntityManager)entityManager).getSession ().doWork ( new Work() {
			@Override
			public void execute ( Connection conn ) throws SQLException {
				Statement stmt = conn.createStatement ();
				for ( ResultSet rs = stmt.executeQuery ( sql ); rs.next (); )
				{
					String serviceNamei = rs.getString ( "service_name" ), accessioni = rs.getString ( "accession" );
					if ( serviceName1.equals ( serviceNamei ) && accession1.equals ( accessioni ) ) continue;
					result.add ( serviceNamei ); 
					result.add ( accessioni ); 
				}
			}} 
		);
		
		return result;
	}
	
	
	private String findBundle ( String serviceName, String accession )
	{
		Query q = entityManager.createNativeQuery ( 
			"SELECT bundle FROM entity_mapping WHERE service_name = '" + serviceName + "' AND accession = '" + accession + "'" 
		);
		
		@SuppressWarnings ( "unchecked" )
		List<String> results = q.getResultList ();
		
		return results.isEmpty () ? null : results.iterator ().next ();
	}
	
	/*private boolean bundleExists ( String bundle )
	{
		Query q = entityManager.createNativeQuery ( "SELECT bundle FROM entity_mapping WHERE bundle = '" + bundle + "'" );
		q.setMaxResults ( 1 );
		return q.getResultList ().size () > 0;
	}*/

	private int deleteBundle ( String bundleId )
	{
		return entityManager.createNativeQuery ( 
			"DELETE FROM entity_mapping WHERE bundle = '" + bundleId + "'" ).executeUpdate ();
	}
	
	
	private String createNewBundleId ( String serviceName, String accession )
	{
		// With 20 bytes as input, the last character is always a padding '=', so we don't need it in this context  
		return 
			Base64.encodeBase64String ( messageDigest.digest ( ( serviceName + accession ).getBytes () ) ).substring (0, 26);
	}
	
}
