package uk.ac.ebi.fg.myequivalents.test.scaling;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.jena.atlas.logging.Log;
import org.hibernate.CacheMode;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.OWL;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.model.EntityId;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.DbEntityIdResolver;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;

/**
 * Dirty hack to test RDF export toward Jena's TDB triple store. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>14 Oct 2015</dd>
 *
 */
public class RDFDumpTest
{
	public static final double RANDOM_QUOTA = 1d / 100;
	public static final String TDB_PATH = "/Applications/local/dev/semantic_web/fuseki/test_custom_rules_tdb/db";
	public static final String DEFAULT_URI_PREFIX = "http://www.ebi.ac.uk/foo/myeq/";
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	@Ignore ( "Not a real test, time consuming" )
	public void dumpIt ()
	{
		Random rnd = new Random ();
		
	  Dataset dataset = TDBFactory.createDataset( TDB_PATH );
		
	  EntityManagerFactory emf = ( (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory () ).getEntityManagerFactory ();
	  EntityManager em = emf.createEntityManager ();
	  
		EntityIdResolver entityIdResolver = new DbEntityIdResolver ( em );

		String sql = "SELECT bundle, service_name, accession, release_date, public_flag FROM entity_mapping ORDER BY bundle";
		
		Session session = (Session) em.getDelegate ();
		SQLQuery qry = session.createSQLQuery ( sql );
		
		// TODO: needs hibernate.jdbc.batch_size?
		qry
			.setReadOnly ( true )
			.setFetchSize ( 1000 )
			.setCacheable ( false )
			.setCacheMode ( CacheMode.IGNORE );
		
		List<String> entityIds = new ArrayList<> ();
		List<Date> relDates = new ArrayList<> ();
		List<Boolean> pubFlags = new ArrayList<> ();
		String prevBundle = null;
		
		for ( ScrollableResults rs = qry.scroll ( ScrollMode.FORWARD_ONLY ); rs.next (); )
		{
			String bundle = (String) rs.get ( 0 );
			
			if ( prevBundle == null ) prevBundle = bundle;
	
			String serviceName = (String) rs.get ( 1 ), acc = (String) rs.get ( 2 );
			String entityId = serviceName + ":" + acc;
			
			if ( !bundle.equals ( prevBundle ) )
			{
				// Jump a random amount of data
				if ( rnd.nextDouble () >= RANDOM_QUOTA ) continue;
				
				// Now dump what we got so far
				//
				int i = 0;
				String uri0 = null;
				
				for ( String thisEntityId: entityIds )
				{
					EntityId eid = entityIdResolver.doall ( thisEntityId );
					Service service = new Service ( eid.getServiceName () );
					
					EntityMapping ent = new EntityMapping ( service, eid.getAcc (), prevBundle );
					
					ent.setReleaseDate ( relDates.get ( i ) );
					ent.setPublicFlag ( pubFlags.get ( i ) );

					String uri = ent.getURI ();
					if ( uri == null ) uri = DEFAULT_URI_PREFIX + service.getName () + ":" + ent.getAccession ();
					
					if ( i == 0 ) 
						uri0 = uri;
					else
					{
						// log.info ( "{}, {}", uri0, uri );
						dataset.begin ( ReadWrite.WRITE );
						Model model = dataset.getDefaultModel ();
						model.add ( model.createResource ( uri0 ), OWL.sameAs, model.createResource ( uri ) );
						dataset.commit ();
						dataset.end ();
					}
					
					i++;
				}
				
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
			
		em.close ();
		emf.close ();
	}
}
