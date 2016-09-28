package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.bind.Marshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.JAXBUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;




/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Sep 2016</dd></dl>
 *
 */
public class XmlFormatHandler extends AbstractFormatHandler
{
	/**
	 * This is used in to create {@link ExposedService}, instead of the regular {@link Service}.	  
	 */
	public static class JAXBObjectFactory 
	{
		public Service createService () {
			return new ExposedService () {};
		}
	}

	public XmlFormatHandler () {
		super ( new String[] { "xml" }, new String [] { "text/xml", "application/xml" } );
	}
	

	@Override
	@SuppressWarnings ( "unchecked" )
	public int serialize ( Stream<MyEquivalentsModelMember> in, OutputStream out )
	{
		try		
		{
			final int result [] = { 0 };
			out.write ( "<myequivalents-backup>\n".getBytes () );
			in.forEach ( e -> { 
				JAXBUtils.marshal ( e, (Class<MyEquivalentsModelMember>) e.getClass (), out, Marshaller.JAXB_FRAGMENT, true );
				result [ 0 ] ++;
			});
			out.write ( "</myequivalents-backup>\n".getBytes () );
			
			return result [ 0 ];
		}
		catch ( IOException ex ) {
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
	}
	
	

	/**
	 * TODO: the XML parser is very very simple, it doesn't check that it starts with the correct root node, 
	 * it doesn't check that there aren't bad nodes.
	 * 
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public Stream<MyEquivalentsModelMember> read ( InputStream input )
	{
		try
		{			
			XMLInputFactory xmlFact = XMLInputFactory.newInstance();
			XMLEventReader xmlReader = xmlFact.createXMLEventReader ( input );
			
			// Plays with XMLEvent(s) received from the xmlReader (it's a stream mapping function, see below)
			Function<XMLEvent, MyEquivalentsModelMember> mapper = new Function<XMLEvent, MyEquivalentsModelMember>() 
			{
				Table<Class<Describeable>, String, Describeable> store = HashBasedTable.create ();
				private StringBuilder nodeValue = null;

				@Override
				public MyEquivalentsModelMember apply ( XMLEvent event )
				{
					if ( event.isStartElement () )
					{
						StartElement el = event.asStartElement ();
						String name = el.getName ().getLocalPart ();
						if ( ArrayUtils.contains ( new String [] { "service", "service-collection", "repository", "bundle" }, name ) )
							// mark the node we want to save
							nodeValue = new StringBuilder ();	
						
						// We do have to return something, so the XMLEventReader-associated stream goes ahead without damage
						// and the downstream stream filters nulls and keeps MyEquivalentsModelMember returned by this mapper
						// (end element condition)
						if ( nodeValue == null ) return null;
						
						// Because we are already inside the node, we have to rebuild its starting tag
						// (including ancestors)
						nodeValue.append ( "<" ).append ( name );
						for ( Iterator attrs = el.getAttributes (); attrs.hasNext ();  )
						{
							Attribute attr = (Attribute) attrs.next ();
						  nodeValue
						    .append ( ' ' ).append ( attr.getName ().getLocalPart () )
						  	.append ( " = \"" ).append ( attr.getValue () ).append ( "\"" );
						}
					  nodeValue.append ( ">\n" );
					}
					else if ( event.isEndElement () )
					{
						// Let's go ahead, we'll return something when ready
						if ( nodeValue == null ) return null;
	
						EndElement el = event.asEndElement ();
						String name = el.getName ().getLocalPart (); 
								
						// Collect all the XML you get while parsing the current node
						// This recursively copies the XML for a node of interest 
						nodeValue.append ( "</" ).append ( name ).append ( ">\n" );
											
						// And now rebuild our myEq element, using what you collected so far
						
						if ( "service".equals ( name ) )
						{
							ExposedService s = JAXBUtils.unmarshal ( 
								new ReaderInputStream ( new StringReader ( nodeValue.toString () ), Charsets.UTF_8 ), 
								ExposedService.class,
								"com.sun.xml.internal.bind.ObjectFactory", new JAXBObjectFactory ()
							);
							
							// rebuild the repo from its name
							String repoName = s.getRepositoryName ();
							if ( StringUtils.trimToNull ( repoName ) != null ) 
							{
								// We expect it to be met earlier and the store is needed to be able to recover it here
								Repository r = (Repository) store.get ( Repository.class, repoName );
								if ( r == null ) throw new RuntimeException ( 
									"Error while uploading data from file: repository '" + repoName + "' not found" 
								);
								s.setRepository ( r );
							}
	
							// same for the collection
							String scName = s.getServiceCollectionName ();
							if ( StringUtils.trimToNull ( scName ) != null ) 
							{
								ServiceCollection sc = (ServiceCollection) store.get ( ServiceCollection.class, scName );
								if ( sc == null ) throw new RuntimeException ( 
									"Error while uploading data from file: ServiceCollection '" + scName + "' not found" 
								);
								s.setServiceCollection ( sc );
							}								
							nodeValue = null;
							return s.asService ();
						}
						else if ( "service-collection".equals ( name ) )
						{
							ServiceCollection sc = JAXBUtils.unmarshal ( nodeValue.toString (), ServiceCollection.class );
							// See above, reconstructing Service(s) needs this
							store.put ( (Class) sc.getClass (), sc.getName (), sc );
							nodeValue = null;
							return sc;
						}
						else if ( "repository".equals ( name ) )
						{
							Repository repo = JAXBUtils.unmarshal ( nodeValue.toString (), Repository.class );
							// See above, reconstructing Service(s) needs this							
							store.put ( (Class) repo.getClass (), repo.getName (), repo );
							nodeValue = null;
							return repo;
						}
						else if ( "bundle".equals ( name ) )
						{
							Bundle bundle = JAXBUtils.unmarshal ( nodeValue.toString (), Bundle.class );
							nodeValue = null;
							return bundle;
						}
					} // endElement()						
					return null;
				} // apply ()
			}; // mapper ()
			
			
			// And now create the stream, the mapper above will return either nulls or myEq elements, 
			// so we need the additional step of filtering.
			//
			return StreamSupport
			.stream ( Spliterators.spliteratorUnknownSize ( xmlReader, Spliterator.IMMUTABLE ), false )
			.map ( mapper )
			.filter ( el -> el != null );
		}
		catch ( FactoryConfigurationError | XMLStreamException ex ) {
			throw new RuntimeException ( "Internal error while reading myEquivalents data dump: " + ex.getMessage (), ex );
		}
	}
}
