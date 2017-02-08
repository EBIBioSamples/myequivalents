package uk.ac.ebi.fg.myequivalents.managers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbBackupManager;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.FormatHandler;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.test.MappingsGenerator;
import uk.ac.ebi.utils.xml.XPathReader;

/**
 * Tests for {@link DbBackupManager}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Mar 2015</dd>
 *
 */
public class BackupManagerTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Rule
	public MappingsGenerator mapGenerator = new MappingsGenerator (){{
		// mapGenerator.nmappingBundles = 2000;
		// mapGenerator.maxMappingId = Integer.MAX_VALUE - 1;		
	}};

	private DbManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
	
	private BackupManager bkpMgr;

	
	@Before
	public void init () 
	{
		bkpMgr = mgrFact.newBackupManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET
		);		
	}
	
	
	
	@Test
	public void testBackupAndUpload () throws FileNotFoundException 
	{
		// ----- Backup
		
		Stream<MyEquivalentsModelMember> dumpStrm = bkpMgr.dump ( null, null );
		List<MyEquivalentsModelMember> generatedElems = new LinkedList<> ();
		
		// Fetch them back and do some testing
		// 
		long elemCt = dumpStrm
		.peek ( elem -> { 
				log.info ( "dumped item: {}", elem );
				generatedElems.add ( elem );				
			})
		.peek ( this::checkElementIsStored )		
		.count ();

		assertEquals ( "dump() returns wrong no of dumped bundles!", elemCt, this.countDBElements () );
				
		
		// ----- And Restore

		log.info ( "----- Backup done, now uploading -----" );
		mapGenerator.cleanUp ();
		
		bkpMgr = mgrFact.newBackupManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET
		);
		
		// And check the uploaded items
		int retVal = bkpMgr.upload ( generatedElems.stream () );		
		assertEquals ( "upload() return value is wrong!", generatedElems.size (), retVal );
		
		long upElemCt = generatedElems
		.stream()
		.peek ( this::checkElementIsStored )
		.map ( elem -> { log.info ( "uploaded item: {}", elem ); return elem; } )
		.count ();
		
		assertEquals ( "upload() returns wrong no of uploaded bundles!", upElemCt, this.countDBElements () );
	}

	
	@Test
	public void testXmlFormatHandler () throws Exception
	{		
		// We'll need them later to verify
		List<MyEquivalentsModelMember> generatedElems = 
			bkpMgr.dump ( null, null ).collect ( Collectors.toCollection ( LinkedList::new  ) );
		
		// And this too
		StringWriter sw = new StringWriter ();
				
	  // Send the output to the SW and a file
		TeeOutputStream out = new TeeOutputStream (
			new WriterOutputStream ( sw ),
			new FileOutputStream ( new File ( "target/dump.xml" ) )  
		);
		// Here we go
		FormatHandler formatHandler = FormatHandler.of ( "xml" );
		bkpMgr.dump ( out, formatHandler, null, null );
		out.close ();

		
		// Check the XML with XPath
		XPathReader xpr = new XPathReader ( new ReaderInputStream ( new StringReader ( sw.toString () ), Charsets.UTF_8  ) );
		assertNotNull ( "No XML root!", xpr.read ( "/myequivalents-backup", XPathConstants.NODE ) );

		// They should be all in the XML
		//
		generatedElems
		.stream()
		.filter ( elem -> elem instanceof Bundle )
		.forEach ( elem -> 
		{
			Bundle b = (Bundle) elem;
			for ( Entity ent: b.getEntities () )
			{
				String xpExpr = String.format ( 
					"/myequivalents-backup/bundle/entity[@service-name='%s' and @accession='%s']",
					ent.getServiceName (),
					ent.getAccession ()
				);
				assertNotNull ( 
					String.format ( "No Entity '%s:%s' in the dump!", ent.getServiceName (), ent.getAccession () ),					
					xpr.read ( xpExpr, XPathConstants.NODE ) 
				);
			}
		});
		
		// Same for mapping containers
		generatedElems
		.stream()
		.filter ( elem -> elem instanceof Describeable )
		.forEach ( elem -> 
		{
			Describeable d = (Describeable) elem;
			String xmlElem = Optional.of ( 
				d instanceof Repository ? "repository" 
				: d instanceof Service ? "service" 
				: d instanceof ServiceCollection ? "service-collection" : null )
				.orElseThrow ( 
					() -> new RuntimeException ( "Don't know how to deal with type " + d.getClass ().getSimpleName () ) 
				);
			
			String xpExpr = String.format ( "/myequivalents-backup/%s[@name='%s']", xmlElem, d.getName () );
			assertNotNull ( 
				String.format ( "No %s '%s' in the dump!", d.getClass ().getSimpleName (), d.getName () ),					
				xpr.read ( xpExpr, XPathConstants.NODE ) 
			);
		});		
		
		
		// --- Now the upload
		int nuploads = 
			bkpMgr.upload ( new ReaderInputStream ( new StringReader ( sw.toString () ), Charsets.UTF_8  ), formatHandler );		
		assertEquals ( "Wrong no. of uploaded items!", generatedElems.size (), nuploads );
		
		long upElemCt = generatedElems
		.stream()
		.peek ( this::checkElementIsStored )
		.map ( elem -> { log.info ( "restored item: {}", elem ); return elem; } )
		.count ();
		
		// Again for the uploaded DB
		assertEquals ( "upload() returns wrong no of uploaded bundles!", upElemCt, this.countDBElements () );
	}
	
	
	@Test
	public void countEntities ()
	{
		assertEquals ( "countEntities() doesn't match!", 
			bkpMgr.countEntities (), 
			(int) bkpMgr.dump ( null, null )
			.filter ( e -> e instanceof Bundle )
			.map ( b -> ( (Bundle) b ).getEntities ().size () )
			.reduce ( ( s1, s2 ) -> s1 + s2 )
			.orElse ( 0 )
		);
	}
	
	private void checkElementIsStored ( MyEquivalentsModelMember elem )
	{
		if ( elem instanceof Describeable ) 
		{
			ServiceManager servMgr = mgrFact.newServiceManager (
				MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET		
			);
			Describeable d = (Describeable) elem;
						
			Set<? extends Describeable> dDB = null;
			if ( elem instanceof Repository )
				dDB = servMgr.getRepositories ( d.getName () ).getRepositories ();
			else if ( elem instanceof ServiceCollection )
				dDB = servMgr.getServiceCollections ( d.getName () ).getServiceCollections ();
			else if ( elem instanceof Service )
				dDB = servMgr.getServices ( d.getName () ).getServices ();
			else throw new RuntimeException ( 
				"Don't know how to deal with the class " + d.getClass ().getName () 
			);

			if ( dDB == null || dDB.isEmpty () )
				fail ( String.format ( "Cannot find %s '%s' in the backend!", d.getClass ().getSimpleName (), d.getName () ) );
			
			return;
		}
		
		if ( !( elem instanceof Bundle ) ) throw new RuntimeException ( 
			"Don't know how to deal with the class " + elem.getClass ().getName () 
		);
			
		Bundle bundle = (Bundle) elem;
		
		Entity e = bundle.getEntities ().stream ().findFirst ().orElseGet (  
			() -> { fail ( "An empty bundle in the result!" ); return null; } 
		);
		
		EntityMappingManager mmgr = mgrFact.newEntityMappingManager (
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET		
		);
		
		Bundle bundleDB = mmgr
			.getMappings ( true, e.getServiceName () + ":" + e.getAccession () )
			.getBundles ().stream ().findFirst ().orElseGet (
					() -> { fail ( MessageFormat.format ( "Upload failed for {0}!", e ) ); return null; } 				
		);
		
		assertEquals ( 
			MessageFormat.format ( "Uploaded bundle mismatches for {0}!", e ),
			bundle.getEntities (), bundleDB.getEntities () 
		);
		
		mmgr.close ();					
	}
	
	
	
	private long countDBElements ()
	{
		EntityManager emgr = mgrFact.getEntityManagerFactory ().createEntityManager ();
		long result = 0;
		
		for ( String dbname: new String [] { "repository", "service_collection", "service" } )
			result += ( (BigInteger) emgr
				.createNativeQuery ( "SELECT COUNT ( * ) FROM " + dbname )
				.getSingleResult () ).longValue ();
		
		result += ( (BigInteger) emgr
			.createNativeQuery ( "SELECT COUNT ( DISTINCT bundle ) FROM entity_mapping" )
			.getSingleResult () ).longValue ();
		
		emgr.close ();	
		
		return result;
	}
}
