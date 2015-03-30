package uk.ac.ebi.fg.myequivalents.dao;

import static org.apache.commons.lang3.ArrayUtils.contains;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ExposedService;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.JAXBUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>20 Feb 2015</dd>
 *
 */
public class BackupDAO
{
	/** 
	 * TODO: does it need to be moved elsewhere?
	 */
	public static class JAXBObjectFactory 
	{
		public Service createService () {
			return new ExposedService () {};
		}
	}

	protected EntityManager entityManager;
	private RepositoryDAO repoDao;
	private ServiceCollectionDAO servCollDao;
	private ServiceDAO serviceDao;
	private EntityMappingDAO mapDao;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	public BackupDAO ( EntityManager entityManager )
	{
		this.entityManager = entityManager;
		repoDao = new RepositoryDAO ( entityManager );
		servCollDao = new ServiceCollectionDAO ( entityManager );
		serviceDao = new ServiceDAO ( entityManager );
		mapDao = new EntityMappingDAO ( entityManager );
	}

	public int dump ( OutputStream out, Integer offset, Integer limit )
	{
		if ( offset == null ) offset = 0;
		if ( limit == null ) limit = Integer.MAX_VALUE;
		
		int result = 0;
		
		long nrepos = repoDao.count (), nsc = -1, ns = -1, nm = -1;
		if ( offset < nrepos )
			result = repoDao.dump ( out, offset, limit == Integer.MAX_VALUE ? null : limit );
		
		if ( result < limit )
		{
			nsc = servCollDao.count ();
			if ( result + nsc < limit )
				result += servCollDao.dump ( out, (int) (offset - nrepos), limit == Integer.MAX_VALUE ? null : limit - result );
		}

		if ( result < limit )
		{
		  ns = serviceDao.count ();
		  if ( result + ns < limit )
		  	result += serviceDao.dump ( out, (int) (offset - nrepos - nsc), limit == Integer.MAX_VALUE ? null : limit );
		}
		
		if ( result < limit )
		{
			nm = mapDao.count ();
			if ( result + nm < limit )
				result += mapDao.dump ( out, (int) (offset - nrepos - nsc - ns), limit == Integer.MAX_VALUE ? null : limit, 100.0 );
		}
		
		return result;
	}
	
	
	public int upload ( InputStream input )
	{
    try
		{
    	// We need this because the anonymous handler below won't accept non-final variables and we don't 
    	// have time now to move it to an explicit definition.
    	//
    	final int itemCounter[] = { 0 };

    	SAXParserFactory spf = SAXParserFactory.newInstance();		
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader ();
			xmlReader.setContentHandler ( new DefaultHandler ()
			{
				private StringBuilder nodeValue = null;
				
				@Override
				public void startElement ( String uri, String localName, String qName, Attributes attrs ) throws SAXException
				{
					if ( contains ( new String [] { "service", "service-collection", "repository", "bundle" }, qName ) )
					{
 						
						nodeValue = new StringBuilder ();
					}

					if ( nodeValue == null ) return;
					
					nodeValue.append ( "<" ).append ( qName ).append ( ' ' );
					for ( int i = 0; i < attrs.getLength (); i++ )
					  nodeValue
					    .append ( attrs.getQName ( i ) )
					  	.append ( " = \"" ).append ( attrs.getValue ( i ) ).append ( "\" " );
					nodeValue.append ( ">\n" );
				}

				@Override
				public void endElement ( String uri, String localName, String qName ) throws SAXException
				{
					if ( nodeValue == null ) return;

					nodeValue.append ( "</" ).append ( qName ).append ( ">\n" );
										
					if ( "service".equals ( qName ) )
					{
						ExposedService s = JAXBUtils.unmarshal ( 
							new ReaderInputStream ( new StringReader ( nodeValue.toString () ), Charsets.UTF_8 ), 
							ExposedService.class,
							"com.sun.xml.internal.bind.ObjectFactory", new JAXBObjectFactory ()
						);
						
						String repoName = s.getRepositoryName ();
						if ( StringUtils.trimToNull ( repoName ) != null ) 
						{
							Repository r = repoDao.findByName ( repoName, false );
							if ( r == null ) throw new RuntimeException ( 
								"Error while uploading data from file: repository '" + repoName + "' not found" 
							);
							s.setRepository ( r );
						}

						String scName = s.getServiceCollectionName ();
						if ( StringUtils.trimToNull ( scName ) != null ) 
						{
							ServiceCollection sc = servCollDao.findByName ( scName, false );
							if ( sc == null ) throw new RuntimeException ( 
								"Error while uploading data from file: ServiceCollection '" + scName + "' not found" 
							);
							s.setServiceCollection ( sc );
						}
						
						serviceDao.store ( s.asService () );
						postUpload ( s, ++itemCounter [ 0 ] );
						nodeValue = null;
					}
					else if ( "service-collection".equals ( qName ) )
					{
						ServiceCollection sc = JAXBUtils.unmarshal ( nodeValue.toString (), ServiceCollection.class );
						servCollDao.store ( sc );
						postUpload ( sc, ++itemCounter [ 0 ] );
						nodeValue = null;
					}
					else if ( "repository".equals ( qName ) )
					{
						Repository repo = JAXBUtils.unmarshal ( nodeValue.toString (), Repository.class );
						repoDao.store ( repo );
						postUpload ( repo, ++itemCounter [ 0 ] );
						nodeValue = null;
					}
					else if ( "bundle".equals ( qName ) )
					{
						Bundle bundle = JAXBUtils.unmarshal ( nodeValue.toString (), Bundle.class );
						mapDao.storeMappingBundle ( new ArrayList<Entity> ( bundle.getEntities () ) );
						postUpload ( bundle, ++itemCounter [ 0 ] );
						nodeValue = null;
					}
					
					if ( nodeValue == null && itemCounter [ 0 ] % 1000 == 0 )
						// we just added a new chunk
						log.info ( "{} items read", itemCounter [ 0 ] );
				}
			});
			xmlReader.parse ( new InputSource ( input ) );
			return itemCounter [ 0 ];
		}
		catch ( ParserConfigurationException | SAXException | IOException ex )
		{
			throw new RuntimeException ( "Internal error while reading myEquivalents data dump" + ex.getMessage (), ex );
		}
	}

	
	
	public void setEntityManager ( EntityManager entityManager )
	{
		this.entityManager = entityManager;
		repoDao.setEntityManager ( entityManager );
		servCollDao.setEntityManager ( entityManager );
		serviceDao.setEntityManager ( entityManager );
		mapDao.setEntityManager ( entityManager );
	}
	
	
	protected void postUpload ( Describeable d, int itemCounter )
	{
	}

	protected void postUpload ( Bundle b, int itemCounter )
	{
	}

	public EntityManager getEntityManager ()
	{
		return entityManager;
	}
}
