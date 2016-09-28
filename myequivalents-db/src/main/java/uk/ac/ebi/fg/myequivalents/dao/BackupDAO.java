package uk.ac.ebi.fg.myequivalents.dao;

import java.util.ArrayList;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * DAO used to implement {@link BackupManager} functionality in the DB back end.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>20 Feb 2015</dd>
 *
 */
public class BackupDAO extends AbstractDAO 
{
	private RepositoryDAO repoDao;
	private ServiceCollectionDAO servCollDao;
	private ServiceDAO serviceDao;
	private EntityMappingDAO mapDao;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	public BackupDAO ( EntityManager entityManager )
	{
		super ( entityManager );
	}
	
	@SuppressWarnings ( { "rawtypes", "unchecked" } )
	public Stream<MyEquivalentsModelMember> dump ( Integer offset, Integer limit )
	{				
		Stream result [] = {
			repoDao.dump ( null, null ),
			servCollDao.dump ( null, null ),
			serviceDao.dump ( null, null ),
			mapDao.dump ( offset, limit )
		};
		
		return (Stream<MyEquivalentsModelMember>)
			Stream.of ( result ).reduce ( Stream::concat ).orElse ( Stream.empty () );		
	}
	
	/**
	 * As usually, this doesn't take care of opening/committing any transaction, you should use 
	 * {@link #postUpload(Describeable, int)}/{@link #postUpload(Bundle, int)} for that.
	 * 
	 */
	public int upload ( Stream<MyEquivalentsModelMember> in )
	{
  	final int itemCounter[] = { 0 };

		in.forEach ( elem -> 
		{
			if ( elem instanceof Service )
			{
				Service s = (Service) elem;
				
				// rebuild the repo from its name
				String repoName = s.getRepositoryName ();
				if ( StringUtils.trimToNull ( repoName ) != null ) 
				{
					Repository r = repoDao.findByName ( repoName, false );
					if ( r == null ) throw new RuntimeException ( 
						"Error while uploading data from file: repository '" + repoName + "' not found" 
					);
					s.setRepository ( r );
				}

				// same for the collection
				String scName = s.getServiceCollectionName ();
				if ( StringUtils.trimToNull ( scName ) != null ) 
				{
					ServiceCollection sc = servCollDao.findByName ( scName, false );
					if ( sc == null ) throw new RuntimeException ( 
						"Error while uploading data from file: ServiceCollection '" + scName + "' not found" 
					);
					s.setServiceCollection ( sc );
				}
				
				serviceDao.store ( s );
				postUpload ( s, ++itemCounter [ 0 ] );
			}
			else if ( elem instanceof ServiceCollection )
			{
				ServiceCollection sc = (ServiceCollection) elem;
				servCollDao.store ( sc );
				postUpload ( sc, ++itemCounter [ 0 ] );
			}
			else if ( elem instanceof Repository )
			{
				Repository r = (Repository) elem;
				repoDao.store ( r );
				postUpload ( r, ++itemCounter [ 0 ] );
			}
			else if ( elem instanceof Bundle )
			{
				Bundle bundle = (Bundle) elem;
				mapDao.storeMappingBundle ( new ArrayList<Entity> ( bundle.getEntities () ) );
				postUpload ( bundle, ++itemCounter [ 0 ] );			
			}			
			else throw new IllegalArgumentException (
				"Cannot upload an instance of " + elem.getClass ().getName ()
			);
			
			if ( itemCounter [ 0 ] % 1000 == 0 )
				log.info ( "{} items uploaded", itemCounter [ 0 ] );
		});
		
		return itemCounter [ 0 ];
	}

	
	
	public void setEntityManager ( EntityManager entityManager )
	{
		super.setEntityManager ( entityManager );
		
		if ( repoDao == null ) 
		{
			repoDao = new RepositoryDAO ( entityManager );
			servCollDao = new ServiceCollectionDAO ( entityManager );
			serviceDao = new ServiceDAO ( entityManager );
			mapDao = new EntityMappingDAO ( entityManager );
			return;
		}
		
		repoDao.setEntityManager ( entityManager );
		servCollDao.setEntityManager ( entityManager );
		serviceDao.setEntityManager ( entityManager );
		mapDao.setEntityManager ( entityManager );
	}
	
	
	/**
	 * An hook to define something to do after having issued an upload operation. This will include
	 * transaction control operations. This default implementation is empty.
	 * 
	 */
	protected void postUpload ( Describeable d, int itemCounter )
	{
	}

	/**
	 * @see #postUpload(Describeable, int)
	 */
	protected void postUpload ( Bundle b, int itemCounter )
	{
	}

	public EntityManager getEntityManager ()
	{
		return entityManager;
	}
}
