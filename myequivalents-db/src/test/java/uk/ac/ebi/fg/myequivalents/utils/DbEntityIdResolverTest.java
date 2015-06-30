package uk.ac.ebi.fg.myequivalents.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.dao.ServiceDAO;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.EntityId;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Jun 2015</dd>
 *
 */
public class DbEntityIdResolverTest
{
	/** Normally you cast this to {@link ManagerFactory}, here we force the specific value cause we need it and we're sure of it*/
	private DbManagerFactory managerFactory = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
	
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( managerFactory.getEntityManagerFactory () );

	@Test
	public void testResolveUri ()
	{
		String uriDom = "http://www.somewhere.net";
		String uriPrefx = uriDom + "/path/to/";
		String acc = "FOO_123";
		String uri = uriPrefx + acc;
		
		Service s = new Service ( "test.eidresolver.service1" );
		s.setUriPattern ( uriPrefx + "$id" );

		String uriDom2 = "http://www.somewhere.else.net/";
		String uriPat2 = uriDom2 + "$id?version=last&acc=$id";
		Service s2 = new Service ( "test.eidresolver.service2" );
		s2.setUriPattern ( uriPat2 );

		EntityManager em = emProvider.getEntityManager ();
		ServiceDAO servDao = new ServiceDAO ( em );
		EntityTransaction tx = em.getTransaction ();

		try
		{
		
			tx.begin ();
			servDao.store ( s );
			servDao.store ( s2 );
			tx.commit ();
			
			em = emProvider.newEntityManager ();
			DbEntityIdResolver resolver = new DbEntityIdResolver ( em );
			
			EntityId eid = resolver.resolveUri ( null, null, uri );
			Assert.assertNotNull ( "null result from ID resolver!", eid );
			Assert.assertEquals ( "Wrong service returned!", s.getName (), eid.getServiceName () );
			Assert.assertEquals ( "Wrong URI returned!", uri, eid.getUri () );
			Assert.assertEquals ( "Wrong acc returned!", acc, eid.getAcc () );
			
			eid = resolver.resolveUri ( null, acc, uri );
			Assert.assertNotNull ( "null result from ID resolver!", eid );
			Assert.assertEquals ( "Wrong service returned!", s.getName (), eid.getServiceName () );
			Assert.assertEquals ( "Wrong URI returned!", uri, eid.getUri () );
			Assert.assertEquals ( "Wrong acc returned!", acc, eid.getAcc () );

			eid = resolver.resolveUri ( s.getName (), acc, uri );
			Assert.assertNotNull ( "null result from ID resolver!", eid );
			Assert.assertEquals ( "Wrong service returned!", s.getName (), eid.getServiceName () );
			Assert.assertEquals ( "Wrong URI returned!", uri, eid.getUri () );
			Assert.assertEquals ( "Wrong acc returned!", acc, eid.getAcc () );
			
			// Now try with domain search instead
			String uri2 = EntityIdResolver.buildUriFromAcc ( acc, uriPat2 );
			eid = resolver.resolveUri ( null, null, uri2 );
			Assert.assertNotNull ( "null result from ID resolver!", eid );
			Assert.assertEquals ( "Wrong service returned!", s2.getName (), eid.getServiceName () );
			Assert.assertEquals ( "Wrong URI returned!", uri2, eid.getUri () );
			Assert.assertEquals ( "Wrong acc returned!", acc, eid.getAcc () );
		}
		finally
		{
			servDao.setEntityManager ( em );
			tx = em.getTransaction ();
			tx.begin ();
			servDao.delete ( s );
			servDao.delete ( s2 );
			tx.commit ();
		}
	}
	
}
