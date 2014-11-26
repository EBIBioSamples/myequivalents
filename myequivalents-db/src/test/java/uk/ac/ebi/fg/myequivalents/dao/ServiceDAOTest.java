package uk.ac.ebi.fg.myequivalents.dao;

import static junit.framework.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * 
 * Various tests for {@link ServiceDAO}.
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceDAOTest
{
	/** Normally you cast this to {@link ManagerFactory}, here we force the specific value cause we need it and we're sure of it*/
	private DbManagerFactory managerFactory = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();

	private ServiceDAO dao;
	
	@Before
	public void initDAO ()
	{
		dao = new ServiceDAO (  managerFactory.getEntityManagerFactory ().createEntityManager () );
	}
	
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
