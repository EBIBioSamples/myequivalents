package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.dao.DbResources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Jul 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BasicHibernateTest
{
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( 
		DbResources.getInstance ().getEntityManagerFactory () 
	);
	
	@Test
	public void testEntityCollectionBasics ()
	{
		EntityManager em = emProvider.getEntityManager ();
		
		Service stest = new Service ( "foo", "fooType" );
		stest.setTitle ( "A test Entity Collection" );
		stest.setDescription ( "A Test Description" );
		
		EntityTransaction transaction = em.getTransaction ();
		
		transaction.begin ();
		em.createQuery ( "delete Service where name = '" + stest.getName () + "'" ).executeUpdate ();
		em.persist ( stest );
		transaction.commit ();
	}
}
