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
		
		
		String hql = mustBePublic
		  ? 
		  	"SELECT s FROM " + Service.class.getName () + " s LEFT JOIN s.repository r WHERE s.name = :serviceName AND (\n" +
		    // The service has some specific visibility attribute
			  "  ( s.publicFlag IS NULL AND s.releaseDate <= current_time() OR s.publicFlag = true )\n" +
		    // if the service has nothing, check its repo has something
		  	"  OR ( r IS NOT NULL AND s.publicFlag IS NULL AND s.releaseDate IS NULL AND ( r.publicFlag IS NULL AND r.releaseDate <= current_time() OR r.publicFlag = true ) )\n" +
			  ")"
      : 
      	"FROM " + Service.class.getName () + " WHERE name = :serviceName";

		Query q = getEntityManager ().createQuery ( hql ).setParameter ( "serviceName", serviceName );

		@SuppressWarnings ( "unchecked" )
		List<Service> results = q.getResultList ();
		
		return results.isEmpty () ? null : results.iterator ().next ();
	}

	/**
	 * First deletes linked entities.
	 */
	@Override
	public boolean delete ( String serviceName )
	{
		serviceName = StringUtils.trimToNull ( serviceName );
		if ( serviceName == null ) return false;
		if ( !exists ( serviceName ) ) return false;
		
		EntityManager em = getEntityManager ();
		em.createNativeQuery ( "DELETE FROM entity_mapping WHERE service_name = '" + serviceName + "'" ).executeUpdate ();
		em.createNativeQuery ( 
			"DELETE FROM entity_mapping WHERE bundle IN\n" +
			"( SELECT bundle FROM ENTITY_MAPPING GROUP BY bundle HAVING count(accession) < 2 )" ).executeUpdate ();
		
		return super.delete ( serviceName );
	}
	
}
