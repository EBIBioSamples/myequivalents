package uk.ac.ebi.fg.myequivalents.test.scaling;


import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.pent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.hibernate.jdbc.Work;
import org.hibernate.jpa.HibernateEntityManager;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbBackupManager;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.db.managers.ProvDbBackupManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.utils.EntityMappingUtils;

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

	
	@Test // @Ignore ( "Not a real test and time consuming" )
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

	@Test
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
