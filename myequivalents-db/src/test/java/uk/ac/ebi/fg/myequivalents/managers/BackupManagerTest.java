package uk.ac.ebi.fg.myequivalents.managers;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.test.MappingsGenerator;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Mar 2015</dd>
 *
 */
public class BackupManagerTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testBackup () throws FileNotFoundException 
	{
		MappingsGenerator mgen = new MappingsGenerator ();
		mgen.generateMappings ();

		DbManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		BackupManager bkpMgr = mgrFact.newBackupManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET
		);


		// ----- Backup
		
		int result = bkpMgr.dump ( new FileOutputStream ( "target/test_dump.xml" ), null, null );
		mgen.cleanUp ();
		assertTrue ( "dump() returns wrong result!", result > 0 );
		log.info ( "----- Backup done, now uploading -----" );
		
		
		// ----- And Restore
		
		bkpMgr = mgrFact.newBackupManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET
		);
		assertTrue ( "upload() returns wrong result!", bkpMgr.upload ( new FileInputStream ( "target/test_dump.xml" ) ) > 0 );
		assertTrue ( "Database was empty!", mgen.cleanUp () > 0 );
	}

}
