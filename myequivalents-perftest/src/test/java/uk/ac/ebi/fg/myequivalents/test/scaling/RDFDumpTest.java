package uk.ac.ebi.fg.myequivalents.test.scaling;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.hibernate.CacheMode;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.EntityId;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.DbEntityIdResolver;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Dirty hack to test RDF export toward Jena's TDB triple store. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>14 Oct 2015</dd>
 *
 */
public class RDFDumpTest
{
	public static final double RANDOM_QUOTA = 1d;
	public static final String TDB_PATH = "/Applications/local/dev/semantic_web/fuseki/test_custom_rules_tdb/db";
	public static final String RDF_OUT_PATH = "target/myeq.ttl";
	public static final String DEFAULT_URI_PREFIX = "http://www.ebi.ac.uk/foo/myeq/";

	public static final String NS_MYEQRES = "http://rdf.ebi.ac.uk/resource/myeq#";
	public static final String NS_MYEQ = "http://rdf.ebi.ac.uk/terms/myeq#";
		
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	@Ignore ( "Not a real test, time consuming" )
	public void dumpTDB ()
	{
		Random rnd = new Random ();
		
	  Dataset dataset = TDBFactory.createDataset( TDB_PATH );
		
	  EntityManagerFactory emf = ( (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory () ).getEntityManagerFactory ();
	  EntityManager em = emf.createEntityManager ();
	  
		EntityIdResolver entityIdResolver = new DbEntityIdResolver ( em );

		String sql = "SELECT bundle, service_name, accession, release_date, public_flag FROM entity_mapping ORDER BY bundle";
		
		Session session = (Session) em.getDelegate ();
		SQLQuery qry = session.createSQLQuery ( sql );
		
		int boundleCount = 0;
		
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
				
				if ( ++boundleCount % 10000 == 0 ) log.info ( "{} bundles done", boundleCount );
			} // if bundle changed
			
			
			entityIds.add ( entityId );
			relDates.add ( (Date) rs.get ( 3 ) );
			BigDecimal pubFlag = ((BigDecimal) rs.get ( 4 ));
			pubFlags.add ( pubFlag == null ? null : pubFlag.intValue () == 1 );
			
		} // for rs
			
		em.close ();
		emf.close ();
	}
	
	
	@Test
	//@Ignore ( "Not a real test, time consuming" )
	public void dumpFile () throws FileNotFoundException
	{
		Random rnd = new Random ();
		
		Model model = ModelFactory.createDefaultModel ();
		
		ManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
	  EntityManagerFactory emf = ( (DbManagerFactory) mgrFact ).getEntityManagerFactory ();
	  EntityManager em = emf.createEntityManager ();
	  
		EntityIdResolver entityIdResolver = new DbEntityIdResolver ( em );
		
		String sql = "SELECT bundle, service_name, accession, release_date, public_flag FROM entity_mapping ORDER BY bundle";
		
		ServiceDAO serviceDao = new ServiceDAO ( em );

		Cache<String, Service> serviceCache = CacheBuilder.newBuilder ().
				maximumSize ( 100000 )
				.expireAfterWrite ( 60, TimeUnit.MINUTES )
				.build ();
		
		Session session = (Session) em.getDelegate ();
		SQLQuery qry = session.createSQLQuery ( sql );
		
		int boundleCount = 0;
		
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
					
					String thisServiceName = eid.getServiceName ();
					Service service = serviceCache.getIfPresent ( thisServiceName );
					if ( service == null ) {
						serviceCache.put ( thisServiceName, service = serviceDao.findByName ( thisServiceName ) );
						assertService ( model, service );
					}
										
					Entity ent = new Entity ( service, eid.getAcc () );
					
					ent.setReleaseDate ( relDates.get ( i ) );
					ent.setPublicFlag ( pubFlags.get ( i ) );
					
					String uri = ent.getURI ();
					
					if ( uri != null ) 
					{
						assertEntity ( model, ent );
					
						if ( i == 0 ) 
							uri0 = uri;
						else
						{
							// log.info ( "{}, {}", uri0, uri );
							model.add ( model.createResource ( uri0 ), OWL.sameAs, model.createResource ( uri ) );
						}
					}
					i++;
				}
				
				// reset all the accumulators
				entityIds = new ArrayList<> ();
				relDates = new ArrayList<> ();
				pubFlags = new ArrayList<> ();
	
				// Start with a new bundle
				prevBundle = bundle;
				
				if ( ++boundleCount % 10000 == 0 ) log.info ( "{} bundles done", boundleCount );

			} // if bundle changed
			
			
			entityIds.add ( entityId );
			relDates.add ( (Date) rs.get ( 3 ) );
			Number pubFlag = ((Number) rs.get ( 4 ));
			pubFlags.add ( pubFlag == null ? null : pubFlag.intValue () == 1 );
			
		} // for rs
			
		em.close ();
		emf.close ();

		
		log.info ( "Writing to {}", RDF_OUT_PATH );
		
		RDFDataMgr.write ( 
			new BufferedOutputStream ( new FileOutputStream ( RDF_OUT_PATH ) ), 
			model,
			RDFFormat.TURTLE_PRETTY 
		);
		
	}
	
	
	private void assertEntity ( Model model, Entity e )
	{
		String euri = e.getURI ();
		
		assertLink ( model, euri, RDF.type, NS_MYEQ + "Entity" );
		assertData ( model, euri, DCTerms.identifier, e.getAccession () );
		assertData ( model, euri, DCTerms.issued, e.getReleaseDate () );

		Service s = e.getService ();
		String suri = NS_MYEQRES + "service_" + s.getName ();
		assertLink ( model, e.getURI (), NS_MYEQ + "has-service", suri );
	}
	
	private void assertService ( Model model, Service service )
	{
		if ( service == null ) return;
		String uri = NS_MYEQRES + "service_" + service.getName ();
		if ( !assertDescribeableBase ( model, service, uri ) ) return;
		assertLink ( model, uri, RDF.type, NS_MYEQ + "Service" );
		assertData ( model, uri, DC.type, service.getEntityType () );
		assertData ( model, uri, NS_MYEQ + "has-uri-pattern", service.getUriPattern () );
		
		Repository repo = service.getRepository ();
		if ( repo != null )
		{
			assertLink ( model, uri, NS_MYEQ + "has-repository", NS_MYEQRES + "repo_" + repo.getName () );
			assertRepository ( model, repo );
		}

		ServiceCollection sc = service.getServiceCollection ();
		if ( sc != null )
		{
			assertLink ( model, uri, NS_MYEQ + "has-service-collection", NS_MYEQRES + "servcoll_" + sc.getName () );
			assertServiceCollection ( model, sc );
		}
		
	}

	private void assertRepository ( Model model, Repository repo )
	{
		if ( repo == null ) return;
		String uri = NS_MYEQRES + "repo_" + repo.getName ();
		if ( !assertDescribeableBase ( model, repo, uri ) ) return;
		assertLink ( model, uri, RDF.type, NS_MYEQ + "Repository" );
	}

	private void assertServiceCollection ( Model model, ServiceCollection sc )
	{
		if ( sc == null ) return;
		String uri = NS_MYEQRES + "servcoll_" + sc.getName ();
		if ( !assertDescribeableBase ( model, sc, uri ) ) return;
		assertLink ( model, uri, RDF.type, NS_MYEQ + "ServiceCollection" );
	}
	
	
	private boolean assertDescribeableBase ( Model model, Describeable descr, String uri )
	{
		assertData ( model, uri, DCTerms.identifier, descr.getName () );
		assertData ( model, uri, DCTerms.title, descr.getTitle () );
		assertData ( model, uri, DCTerms.description, descr.getDescription () );
		assertData ( model, uri, DCTerms.issued, descr.getReleaseDate () );
				
		return true;
	}
	
	private void assertData ( Model model, String uri, Property prop, String val ) 
	{
		if ( ( val = StringUtils.trimToNull ( val ) ) == null ) return;
		model.add ( model.getResource ( uri ), prop, val );
	}
	
	private void assertData ( Model model, String uri, String prop, String val )
	{
		assertData ( model, uri, model.getProperty ( prop ), val );
	} 


	private void assertData ( Model model, String uri, Property prop, Object val ) 
	{
		if ( val == null ) return;
		model.add ( model.getResource ( uri ), prop, model.createTypedLiteral ( val ) );
	}
	
	private void assertLink ( Model model, String uri, Property prop, String uri1 )
	{
		if ( uri1 == null ) return;
		model.add ( model.getResource ( uri ), prop, model.getResource ( uri1 ) );
	}
	
	private void assertLink ( Model model, String uri, String propUri, String uri1 )
	{
		assertLink ( model, uri, model.getProperty ( propUri ), uri1 );
	}

}
