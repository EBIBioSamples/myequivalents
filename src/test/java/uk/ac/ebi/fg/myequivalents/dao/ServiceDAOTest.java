package uk.ac.ebi.fg.myequivalents.dao;

import static junit.framework.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceDAOTest
{
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( 
		Resources.getInstance ().getEntityManagerFactory () 
	);

	private ServiceDAO dao;
	
	@Before
	public void initDAO ()
	{
		dao = new ServiceDAO ( emProvider.getEntityManager () );
	}
	
/*	public static void clearDB ()
	{
		EntityManager em = emfProvider.getEntityManagerFactory ().createEntityManager ();
		ServiceDAO dao = new ServiceDAO ( em );
		EntityTransaction tns = em.getTransaction ();
		dao.deleteAll ();
		tns.commit ();
	}
*/	
	@Test
	public void testCreate ()
	{
		Service service = new Service ( "test.testCreate.service1", "someType", "A Test Service", "The Description of a Test Service" );
		service.setUriPrefix ( "http://somewhere.in.the.net/service1/" );
		EntityManager em = dao.getEntityManager ();
		EntityTransaction tns = em.getTransaction ();
		tns.begin ();
		dao.delete ( service );
		assertTrue ( "Service deletion failed", !dao.exists ( service ) );
		dao.store ( service );
		tns.commit ();
		assertTrue ( "New service creation failed!", dao.exists ( service ) );
	}
}
