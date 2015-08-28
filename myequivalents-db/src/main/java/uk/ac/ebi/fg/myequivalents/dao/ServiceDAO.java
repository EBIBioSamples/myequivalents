package uk.ac.ebi.fg.myequivalents.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.myequivalents.model.Service;

/**
 * the {@link Service} DAO. 
 *
 * <dl><dt>date</dt><dd>Jul 19, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceDAO extends DescribeableDAO<Service>
{
	public ServiceDAO ( EntityManager entityManager )
	{
		super ( entityManager );
	}

	/**
	 * When a public-only restriction is needed, it cascades repository's visibility attributes to all its services 
	 * having no specified visibility attribute.  
	 */
	@Override
	public Service findByName ( String serviceName, boolean mustBePublic )
	{
		serviceName = StringUtils.trimToNull ( serviceName );
		if ( serviceName == null ) return null;
		
		String hqlName = "service.findByName"; if ( mustBePublic ) hqlName += ".publicOnly";
		Query q = getEntityManager ().createNamedQuery ( hqlName, Service.class )
			.setParameter ( "serviceName", serviceName );

		@SuppressWarnings ( "unchecked" )
		List<Service> results = q.getResultList ();
		
		return results.isEmpty () ? null : results.iterator ().next ();
	}

	/**
	 * First deletes linked entities.
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public boolean delete ( String serviceName )
	{
		serviceName = StringUtils.trimToNull ( serviceName );
		if ( serviceName == null ) return false;
		if ( !exists ( serviceName ) ) return false;
		
		EntityManager em = getEntityManager ();
		em.createNativeQuery ( "DELETE FROM entity_mapping WHERE service_name = '" + serviceName + "'" ).executeUpdate ();
		
		Query qDanglingBundles = em.createNativeQuery ( 
			"SELECT bundle FROM ENTITY_MAPPING GROUP BY bundle HAVING count(accession) = 1" 
		);
		Query qDelBundle = em.createNativeQuery ( "DELETE FROM entity_mapping WHERE bundle = :bundle" );

		boolean result = false;
		
		for ( String bundle: (List<String>) qDanglingBundles.getResultList () )
			result |= qDelBundle.setParameter ( "bundle", bundle ).executeUpdate () > 0;
		
		return result | super.delete ( serviceName );
	}
	
	public Service findByUriPattern ( String uriPattern, boolean mustBePublic )
	{
		uriPattern = StringUtils.trimToNull ( uriPattern );
		if ( uriPattern == null ) return null;
		
		String hqlName = "service.findByUriPattern"; if ( mustBePublic ) hqlName += ".publicOnly";
		Query q = getEntityManager ().createNamedQuery ( hqlName ).setParameter ( "uriPattern", uriPattern );

		@SuppressWarnings ( "unchecked" )
		List<Service> results = q.getResultList ();
		
		return results.isEmpty () ? null : results.iterator ().next ();
	}
	
	public Service findByUriPattern ( String uriPattern ) 
	{
		return findByUriPattern ( uriPattern, true );
	}

	
	@SuppressWarnings ( "unchecked" )
	public List<Service> findByUriPatternLike ( String uriPattern, boolean mustBePublic )
	{
		uriPattern = StringUtils.trimToNull ( uriPattern );
		if ( uriPattern == null ) return null;
		
		String hqlName = "service.findByUriPattern.like"; if ( mustBePublic ) hqlName += ".publicOnly";
		Query q = getEntityManager ().createNamedQuery ( hqlName, Service.class )
			.setParameter ( "uriPattern", uriPattern );

		return q.getResultList ();
	}
	
	public List<Service> findByUriPatternLike ( String uriPattern )
	{
		return findByUriPatternLike ( uriPattern, true );
	}

}
