package uk.ac.ebi.fg.myequivalents.test.scaling;

import static java.lang.System.err;
import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.jdbc.Work;
import org.hibernate.jpa.HibernateEntityManager;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter;

/**
 * Some rough code to dump/upload myEq data.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Jan 2015</dd>
 *
 */
public class DumpTest
{
	@Test @Ignore ( "Not a real test and time consuming" )
	@SuppressWarnings ( "unchecked" )
	public void dumpDb () throws Exception
	{
  	Resources res = Resources.getInstance ();
  	ManagerFactory mf = res.getMyEqManagerFactory ();
  	EntityManager em = ((DbManagerFactory) mf).getEntityManagerFactory ().createEntityManager ();
		
  	
  	// Dump service-related data
  	
		ServiceSearchResult sr = new ServiceSearchResult ();
		
		EntityTransaction tx = em.getTransaction ();
  	tx.begin ();
  	List<Repository> repos = (List<Repository>) em.createQuery ( "FROM Repository" )
  		.setHint ( "org.hibernate.readOnly", true )
  		.getResultList ();
  	tx.commit ();
  	for ( Repository r: repos ) sr.addRepository ( r );
  	
  	tx.begin ();
  	List<ServiceCollection> scs = (List<ServiceCollection>) em.createQuery ( "FROM ServiceCollection" )
  		.setHint ( "org.hibernate.readOnly", true )
  		.getResultList ();
  	tx.commit ();
  	for ( ServiceCollection sc: scs ) sr.addServiceCollection ( sc );

  	tx.begin ();
  	List<Service> servs = (List<Service>) em.createQuery ( "FROM Service" )
  		.setHint ( "org.hibernate.readOnly", true )
  		.getResultList ();
  	tx.commit ();
  	for ( Service serv: servs ) sr.addService ( serv );
  	
  	err.print ( "service store " );
		JAXBContext context = JAXBContext.newInstance ( ServiceSearchResult.class );
		Marshaller m = context.createMarshaller ();
		m.setProperty ( Marshaller.JAXB_ENCODING, "UTF-8" );
		m.marshal ( sr, err );
		err.println ();

		
		// Dump mappings
		
		((HibernateEntityManager) em).getSession ().doWork ( new Work() {
			@Override
			public void execute ( Connection conn ) throws SQLException
			{
				Random rnd = new Random ();
				
				Statement stmt = conn.createStatement ();
				ResultSet rs = stmt.executeQuery ( "SELECT * FROM entity_mapping ORDER BY bundle"  );
				List<String> entityIds = new ArrayList<> ();
				List<Date> relDates = new ArrayList<> ();
				List<Boolean> pubFlags = new ArrayList<> ();
				
				for (	String prevBundle = null; rs.next (); )
				{
					String bundle = rs.getString ( "bundle" );
					
					if ( prevBundle == null ) prevBundle = bundle;

					String serviceName = rs.getString ( "service_name" ),
				  	acc = rs.getString ( "accession" );
					String entityId = serviceName + ":" + acc;
					
					if ( !bundle.equals ( prevBundle ) )
					{
						// Jump a random amount of data
						if ( rnd.nextDouble () >= 0.25 ) continue;
						
						err.print ( "mapping store-bundle " );
						// Output the bundle
						for ( String thisEntityId: entityIds )
							err.print ( thisEntityId + " " );
						err.println ();
						
						for ( int i = 0; i < entityIds.size (); i++ )
						{
							Date relDate = relDates.get ( i );
							Boolean pubFlag = pubFlags.get ( i );
							if ( relDate != null || pubFlag != null )
							{
								err.format ( "entity set visibility %s ", entityId );
								if ( relDate != null )
									err.format ( "--release-date %s", DateJaxbXmlAdapter.STR2DATE.marshal ( relDate ) );
								if ( pubFlag != null )
									err.format ( "--public-flag %s", pubFlag );
								err.println ();
							}
						}
						
						// reset all the accumulators
						entityIds = new ArrayList<> ();
						relDates = new ArrayList<> ();
						pubFlags = new ArrayList<> ();
						
						prevBundle = bundle; 
					} // if bundle
					
					
					entityIds.add ( entityId );
					relDates.add ( rs.getDate ( "release_date" ) );
					String pubFlagStr = rs.getString ( "public_flag" );
					pubFlags.add ( "1".equals ( pubFlagStr ) ? Boolean.TRUE : "0".equals ( pubFlagStr ) ? Boolean.FALSE : null );
					
				} // for rs
				
			} // Work.execute
		}); // Work 		
		
	} // dumpDb ()
	
	
	@Test @Ignore ( "Not a real test and time consuming" )
	public void uploadDb () throws Exception
	{
  	Resources res = Resources.getInstance ();
  	ManagerFactory mf = res.getMyEqManagerFactory ();
  	
  	EntityMappingManager mapMgr = mf.newEntityMappingManager ( "admin", "admin.secret" );
  	
		LineNumberReader rdr = new LineNumberReader ( new BufferedReader ( new FileReader ( "target/dump.txt" ) ) );
		for ( String line; ( line = rdr.readLine () ) != null; )
		{
			out.println ( line );
			if ( line.startsWith ( "service store " ) )
			{
				String xml = line.substring ( "service store ".length () );
				ServiceManager srvMgr = mf.newServiceManager ( "admin", "admin.secret" );
				srvMgr.storeServicesFromXML ( new StringReader ( xml ) );
			}
			else if ( line.startsWith ( "mapping store-bundle " ) )
			{
				String[] eids = StringUtils.split ( line.substring ( "mapping store-bundle ".length () ), ' ' );
				mapMgr.storeMappingBundle ( eids );
			}
			// TODO: visibility
		}
		rdr.close ();
	}
}
