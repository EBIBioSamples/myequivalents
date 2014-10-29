//package uk.ac.ebi.fg.myequivalents.provenance.webservices.server.test;
//
//import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;
//
//import java.util.Arrays;
//import java.util.Date;
//
//import javax.persistence.EntityManager;
//import javax.persistence.EntityTransaction;
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;
//
//import org.joda.time.DateTime;
//
//import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
//import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
//import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
//import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
//import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
//import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
//import uk.ac.ebi.fg.myequivalents.resources.Resources;
//import uk.ac.ebi.fg.myequivalents.webservices.server.test.WebTestDataInitializer;
//
///**
// * TODO: Comment me!
// *
// * <dl><dt>date</dt><dd>27 Oct 2014</dd></dl>
// * @author Marco Brandizi
// *
// */
//public class WebTestProvDataInitializer implements ServletContextListener
//{
//	public static final ProvenanceRegisterEntry e = new ProvenanceRegisterEntry ( 
//		"foo.user", "foo.op", p ( "foo.entity", Arrays.asList ( "acc1", "acc2", "acc3" ) )
//	);
//	public static final ProvenanceRegisterEntry e1 = new ProvenanceRegisterEntry ( 
//		"foo.user1", "foo.op1", p ( "foo.entity", Arrays.asList ( "acc1", "acc2", "acc3" ) )
//	);
//
//	@Override
//	public void contextInitialized ( ServletContextEvent ev )
//	{
//		if ( !"true".equals ( System.getProperty ( WebTestDataInitializer.INIT_FLAG_PROP, null ) ) ) return;
//		
//		System.out.println ( "\n\n ___________________ Creating Test Provenance Data __________________________ \n\n\n" );
//		ManagerFactory mgrf = Resources.getInstance ().getMyEqManagerFactory ();
//		EntityManager em = ((DbManagerFactory) mgrf).getEntityManagerFactory ().createEntityManager ();
//		
//		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( em );
//		
//		EntityTransaction ts = em.getTransaction ();
//		ts.begin ();
//		provDao.create ( e );
//		provDao.create ( e1 );
//		ts.commit ();
//		em.close ();
//	}
//
//	
//	@Override
//	public void contextDestroyed ( ServletContextEvent ev )
//	{
//		if ( !"true".equals ( System.getProperty ( WebTestDataInitializer.INIT_FLAG_PROP, null ) ) ) return;
//
//		ProvManagerFactory mgrf = Resources.getInstance ().getMyEqManagerFactory ();
//		
//		ProvRegistryManager regMgr = mgrf.newProvRegistryManager (
//			WebTestDataInitializer.adminUser.getEmail (), WebTestDataInitializer.adminSecret
//		);
//		
//		regMgr.purge ( new DateTime ().minusMinutes ( 3 ).toDate (), null );
//		regMgr.close ();
//	}
//
//}
