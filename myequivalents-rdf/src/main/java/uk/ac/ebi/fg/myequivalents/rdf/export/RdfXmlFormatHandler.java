package uk.ac.ebi.fg.myequivalents.rdf.export;

import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;

/**
 * 
 * Support for RDF/XML. This simply defines type strings and interfaces OWL API. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Feb 2017</dd></dl>
 *
 */
public class RdfXmlFormatHandler extends RdfFormatHandler
{
	public RdfXmlFormatHandler () 
	{
		super ( new String[] { "rdf", "rdf+xml" },
		new String[] { "application/rdf+xml" },
		RDFXMLOntologyFormat::new );
	}
}