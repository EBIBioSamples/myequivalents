package uk.ac.ebi.fg.myequivalents.rdf.export;

import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;

public class RdfXmlFormatHandler extends RdfFormatHandler
{
	public RdfXmlFormatHandler () 
	{
		super ( new String[] { "rdf", "rdf+xml" },
		new String[] { "application/rdf+xml" },
		RDFXMLOntologyFormat::new );
	}
}