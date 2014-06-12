package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

import static org.junit.Assert.*;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>10 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbServiceManagerTest
{
	@Test
	public void testCreation () throws Exception
	{
		String editorPass = "test.password";
		String editorSecret = User.generateSecret (); 
		
		User editorUser = new User ( 
			"test.editor", "Test Editor", "User", editorPass, "test editor notes", Role.EDITOR, editorSecret 
		);
		
		DbManagerFactory mgrFact = (DbManagerFactory) Resources.getInstance ().getMyEqManagerFactory ();
		EntityManager em = mgrFact.getEntityManagerFactory ().createEntityManager ();
		
		UserDao userDao = new UserDao ( em );

		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		userDao.storeUnauthorized ( editorUser );
		ts.commit ();

		Reader xmlIn = new InputStreamReader ( this.getClass ().getResourceAsStream ( "/data/foo_services.xml" ) );
		ServiceManager smgr = mgrFact.newServiceManager ( editorUser.getEmail (), editorSecret );
		smgr.storeServicesFromXML ( xmlIn );
		
		ProvenanceRegisterEntryDAO provDao = new ProvenanceRegisterEntryDAO ( mgrFact.getEntityManagerFactory ().createEntityManager () );
		List<ProvenanceRegisterEntry> proves = provDao.find ( null, "%storeServices%", Arrays.asList ( "service", "%.service7" ) );
		assertEquals ( "Expected provenance records not saved (service7)!", 1, proves.size () );

		proves = provDao.find ( null, "%storeServices%", Arrays.asList ( "repository", "test.testmain.addedRepo1" ) );
		assertEquals ( "Expected provenance records not saved (addedRepo1)!", 1, proves.size () );
	
		proves = provDao.find ( null, null, Arrays.asList ( "serviceCollection", null ) );
		assertEquals ( "Expected provenance records not saved (servCollections)!", 1, proves.size () );
		
		ProvenanceRegisterEntry prove = proves.get ( 0 );
		System.out.println ( "---- SERVCOLL: " + prove );

		assertEquals ( "Fetched provenance record wrong (parameters)", 7, prove.getParameters ().size () );
		assertEquals ( "Fetched provenance record wrong (user)", editorUser.getEmail (), prove.getUserEmail () );
	}
}
