package uk.ac.ebi.fg.myequivalents.test.scaling;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.persistence.EntityManagerFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbBackupManager;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbBackupManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * Some rough code to dump/upload myEq data.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Jan 2015</dd>
 *
 */
public class DumpTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	
	@Test @Ignore ( "Not a real test and time consuming" )
	public void dumpDb () throws Exception
	{
  	Resources res = Resources.getInstance ();
  	ManagerFactory mf = res.getMyEqManagerFactory ();
  	EntityManagerFactory emf = ((DbManagerFactory) mf).getEntityManagerFactory ();
  	
  	DbBackupManager bkpMgr = new DbBackupManager ( emf.createEntityManager (), "admin", "admin.secret" );
  	
  	FileOutputStream out = new FileOutputStream ( "target/dump.xml" );
  	bkpMgr.dump ( out, null, null );
  	out.close ();
	}

	@Test @Ignore ( "Not a real test and time consuming" )
	public void uploadDb () throws Exception
	{
  	Resources res = Resources.getInstance ();
  	ManagerFactory mf = res.getMyEqManagerFactory ();
  	EntityManagerFactory emf = ((DbManagerFactory) mf).getEntityManagerFactory ();
  	
  	//DbBackupManager bkpMgr = new DbBackupManager ( emf.createEntityManager (), "admin", "admin.secret" );
  	DbBackupManager bkpMgr = new ProvDbBackupManager ( emf.createEntityManager (), "admin", "admin.secret" );

  	InputStream in = new FileInputStream ( "target/dump.xml" );
		bkpMgr.upload ( in );
		in.close ();
	}


}
