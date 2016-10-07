package uk.ac.ebi.fg.myequivalents.rdf.export;

import org.coode.owlapi.turtle.TurtleOntologyFormat;

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