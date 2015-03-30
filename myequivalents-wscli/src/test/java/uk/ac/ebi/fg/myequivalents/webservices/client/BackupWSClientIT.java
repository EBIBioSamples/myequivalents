package uk.ac.ebi.fg.myequivalents.webservices.client;

import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.fg.myequivalents.webservices.client.AccessControlWSClientIT.CLI_SPRING_CONFIG_FILE_NAME;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.test.MappingsGenerator;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Mar 2015</dd>
 *
 */
public class BackupWSClientIT
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testBackup () throws IOException 
	{
		ManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ( CLI_SPRING_CONFIG_FILE_NAME );
		BackupManager bkpMgr = mgrFact.newBackupManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET
		);

		
		// ----- Backup
		
		OutputStream out = new FileOutputStream ( "target/test_dump.xml" );
		int result = bkpMgr.dump ( out, null, null );
		out.close ();
		assertTrue ( "dump() returns wrong result!", result > 0 );
		log.info ( "----- Backup done, now uploading -----" );
		
		
		// ----- And Restore
		
		bkpMgr = mgrFact.newBackupManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET
		);
		assertTrue ( "upload() returns wrong result!", bkpMgr.upload ( new FileInputStream ( "target/test_dump.xml" ) ) > 0 );
	}

}
