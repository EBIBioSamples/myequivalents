package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.test.TestEntityMgrProvider;

public class BasicHibernateTest
{
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( 
		Resources.getInstance ().getEntityManagerFactory () 
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
