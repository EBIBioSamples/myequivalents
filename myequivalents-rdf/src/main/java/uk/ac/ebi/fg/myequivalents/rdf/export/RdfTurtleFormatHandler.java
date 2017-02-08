package uk.ac.ebi.fg.myequivalents.rdf.export;

import org.coode.owlapi.turtle.TurtleOntologyFormat;

/**
 * Support for RDF/Turtle. This simply defines type strings and interfaces OWL API. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Feb 2017</dd></dl>
 *
 */
public class RdfTurtleFormatHandler extends RdfFormatHandler
{
	public RdfTurtleFormatHandler () 
	{
		super ( 
			new String[] { "ttl", "turtle" }, 
			new String[] { "text/turtle" }, 
			TurtleOntologyFormat::new 
		);
	}
}