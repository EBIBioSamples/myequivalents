package uk.ac.ebi.fg.myequivalents.rdf.export;

import static uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils.getNamespaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.AbstractFormatHandler;
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;
import uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping.MyEqRdfMapperFactory;
import uk.ac.ebi.utils.memory.MemoryUtils;


/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Sep 2016</dd></dl>
 *
 */
public abstract class RdfFormatHandler extends AbstractFormatHandler
{
	public static class RDFTurtleFormatHandler extends RdfFormatHandler {
		public RDFTurtleFormatHandler () {
			super ( 
				new String[] { "ttl", "turtle" }, 
				new String[] { "text/turtle" }, 
				TurtleOntologyFormat::new 
			);
		}
	}

	public static class RDFXMLFormatHandler extends RdfFormatHandler {
		public RDFXMLFormatHandler () {
			super ( new String[] { "rdf", "rdf+xml" },
			new String[] { "application/rdf+xml" },
			RDFXMLOntologyFormat::new );
		}
	}
	
	protected final Supplier<PrefixOWLOntologyFormat> owlApiOntologyFormatSupplier;
	
	
	protected RdfFormatHandler ( 
		String[] shortTypes, String[] contentTypes, Supplier<PrefixOWLOntologyFormat> owlApiOntologyFormatSupplier ) 
	{
		super ( shortTypes, contentTypes );
		this.owlApiOntologyFormatSupplier = owlApiOntologyFormatSupplier;
	}
	
	
	
	@Override
	public int serialize ( Stream<MyEquivalentsModelMember> in, OutputStream out )
	{
		try
		{
			MyEqRdfMapperFactory.init ();
			
			OWLOntologyManager owlMgr = OWLManager.createOWLOntologyManager ();
			OWLOntology kb = owlMgr.createOntology ( IRI.create ( String.format ( 
				"http://www.ebi.ac.uk/myequivalents/export/%1$Y%1$m%1$d%1$H%1$M%1$S", new Date () 
			)));

			
			MyEqRdfMapperFactory mapFact = new MyEqRdfMapperFactory ( kb );
			int result = (int) in
			.peek ( e -> {			
				MemoryUtils.checkMemory ( () -> this.flushKnowledgeBase ( kb, out ) ); 
				mapFact.map ( e ); 			
			})
			.count ();
			
			// Save (possibly for the last time)
			this.flushKnowledgeBase ( kb, out );									
			return result;
		}
		catch ( OWLOntologyCreationException ex ) {
			throw new RuntimeException ( "Internal error while exporting to RDF: " + ex.getMessage (), ex );
		}		
	}

	
	@Override
	public Stream<MyEquivalentsModelMember> read ( InputStream in ) {
		throw new UnsupportedOperationException ( "Not implemented yet" );
	}

	
	private void flushKnowledgeBase ( OWLOntology kb, OutputStream out ) 
	{
		try
		{
			if ( kb.isEmpty () ) return;
			
			if ( out instanceof MultipleFileOutputStream )
				((MultipleFileOutputStream) out).nextFile ();
			
			PrefixOWLOntologyFormat owlApiOntologyFormat = this.owlApiOntologyFormatSupplier.get ();
			
			for ( Entry<String, String> nse: getNamespaces ().entrySet () )
				owlApiOntologyFormat.setPrefix ( nse.getKey (), nse.getValue () );			
			
			OWLOntologyManager ontoMgr = kb.getOWLOntologyManager ();
			ontoMgr.saveOntology ( kb, owlApiOntologyFormat, out );
			ontoMgr.removeOntology ( kb );
		}
		catch ( OWLOntologyStorageException | IOException ex ) {
			throw new RuntimeException ( "Internal error while saving RDF dump : " + ex.getMessage (), ex );
		}		
	}
	
}
