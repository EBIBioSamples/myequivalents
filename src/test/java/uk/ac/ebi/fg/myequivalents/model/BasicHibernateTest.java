package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.test.TestEntityMgrFactoryProvider;
import uk.ac.ebi.fg.myequivalents.test.TestEntityMgrProvider;

public class BasicHibernateTest
{
	@ClassRule
	public static TestEntityMgrFactoryProvider emfProvider = new TestEntityMgrFactoryProvider ();
	
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( emfProvider.getEntityManagerFactory () );
	
	@Test
	public void testEntityCollectionBasics ()
	{
		EntityManager em = emProvider.getEntityManager ();
		
		EntityCollection ectest = new EntityCollection ( "foo", "fooType" );
		ectest.setTitle ( "A test Entity Collection" );
		ectest.setDescription ( "A Test Description" );
		
		EntityTransaction transaction = em.getTransaction ();
		
		transaction.begin ();
		em.createQuery ( "delete EntityCollection where name = '" + ectest.getName () + "'" ).executeUpdate ();
		em.persist ( ectest );
		transaction.commit ();
	}
}
