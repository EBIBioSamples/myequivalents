package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.test.TestEntityMgrFactoryProvider;
import uk.ac.ebi.fg.myequivalents.test.TestEntityMgrProvider;

import static junit.framework.Assert.*;

public class ServiceDAOTest
{
	@ClassRule
	public static TestEntityMgrFactoryProvider emfProvider = new TestEntityMgrFactoryProvider ();
	
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( emfProvider.getEntityManagerFactory () );

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
