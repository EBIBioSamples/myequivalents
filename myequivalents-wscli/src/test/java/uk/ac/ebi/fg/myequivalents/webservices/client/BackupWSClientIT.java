package uk.ac.ebi.fg.myequivalents.webservices.client;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.fg.myequivalents.webservices.client.AccessControlWSClientIT.CLI_SPRING_CONFIG_FILE_NAME;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.xpath.XPathConstants;

import junit.framework.Assert;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.FormatHandler;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.test.MappingsGenerator;
import uk.ac.ebi.utils.xml.XPathReader;

/**
 * Tests for the {@link BackupManager}-related service.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Mar 2015</dd>
 *
 */
public class BackupWSClientIT
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	private ManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ( CLI_SPRING_CONFIG_FILE_NAME );
	private BackupManager bkpMgr = mgrFact.newBackupManager ( 
		MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET
	);

	
	/**
	 * TODO: comment me!
	 */
	@Test
	public void testDumpBasic ()
	{
		Stream<MyEquivalentsModelMember> stream = bkpMgr.dump ( null, null );
		long ct = stream
		.peek ( e -> log.info ( "Dumped element: {}", e ) )
		.count ();
		
		assertTrue ( "Wrong no of dumped elements!", ct > 0 );
	}
	
	/**
	 * TODO: comment me!
	 */
	@Test
	public void testUploadBasic ()
	{
		// Upload
		EntityMappingSearchResult mapr = new EntityMappingSearchResult ( true );
		EntityMapping 
			e1 = new EntityMapping ( Service.UNSPECIFIED_SERVICE, "http://test.foo.net/1", "b1" ),
			e2 = new EntityMapping ( Service.UNSPECIFIED_SERVICE, "http://test.foo.net/2", "b1" );
		
		mapr.addEntityMapping ( e1 );
		mapr.addEntityMapping ( e2 );
		Bundle bundle = mapr.getBundles ().iterator ().next ();		
		
		Stream<MyEquivalentsModelMember> upStrm = Stream.of ( Service.UNSPECIFIED_SERVICE, bundle );		
		int upCt = bkpMgr.upload ( upStrm );
		assertEquals ( "Wrong no of uploaded elements!", 2, upCt );
		
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET 
		);


		mapr = mapMgr.getMappings ( true, ":<" + e1.getURI () + ">");
		Collection<Bundle> bundles = mapr.getBundles ();
		assertEquals ( "Uploaded bundle not found!", 1, bundles.size () );
		assertEquals ( "Uploaded bundle is wrong!", bundle.getEntities (), bundles.iterator ().next ().getEntities () );
	}

	
	/**
	 * TODO: comment me!
	 */
	@Test
	public void testIO () throws IOException 
	{
		// ----- Backup
		
		StringWriter sw = new StringWriter ();
		TeeOutputStream out = new TeeOutputStream ( 
			new WriterOutputStream ( sw ),
			new FileOutputStream ( "target/dump.xml" )
		);
		bkpMgr.dump ( out, FormatHandler.of ( "xml" ), null, null );
		out.close ();

		String outs = Optional.ofNullable ( out.toString () ).orElse ( "" );
		assertFalse ( "dump() returns empty result!", outs.isEmpty () );
		
		// Check the XML with XPath
		XPathReader xpr = new XPathReader ( new ReaderInputStream ( new StringReader ( sw.toString () ), Charsets.UTF_8  ) );
		assertNotNull ( "No root element!", xpr.read ( "/myequivalents-backup", XPathConstants.NODE ) );
		assertNotNull ( "No service element!", xpr.read ( 
			"/myequivalents-backup/service[@name != '']", XPathConstants.NODE ) 
		);
		assertNotNull ( "No entity element!", xpr.read ( 
			"/myequivalents-backup/bundle/entity[@service-name!='' and @accession!='']", XPathConstants.NODE ) 
		);
		
		log.info ( "----- Backup done, now uploading -----" );
	
		
		
		// ----- And Restore
		
		bkpMgr = mgrFact.newBackupManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET
		);
		assertTrue ( "upload() returns wrong result!", 
			bkpMgr.upload ( new FileInputStream ( "target/dump.xml" ), FormatHandler.of ( "xml" ) ) > 0 
		);
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

}
