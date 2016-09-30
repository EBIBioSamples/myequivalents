package uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping;

import static uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils.registerNs;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Feb 2016</dd></dl>
 *
 */
public class MyEqRdfMapperFactory extends RdfMapperFactory
{
	static {
		init ();
	}
	
	{
		setMapper ( EntityMappingSearchResult.Bundle.class, new BundleRdfMapper () );
		setMapper ( Entity.class, new EntityRdfMapper() );
		setMapper ( Service.class, new ServiceRdfMapper () );
		setMapper ( Repository.class, new RepositoryRdfMapper () );
		setMapper ( ServiceCollection.class, new ServCollRdfMapper () );
	}

	public MyEqRdfMapperFactory () {
		super ();
	}

	public MyEqRdfMapperFactory ( OWLOntology knowledgeBase ) {
		super ( knowledgeBase );
	}

	public static void init () 
	{
		registerNs ( "myeq", 	 				"http://rdf.ebi.ac.uk/terms/myeq#" );
		registerNs ( "myeqres",				"http://rdf.ebi.ac.uk/resource/myeq#" );
	}
}
