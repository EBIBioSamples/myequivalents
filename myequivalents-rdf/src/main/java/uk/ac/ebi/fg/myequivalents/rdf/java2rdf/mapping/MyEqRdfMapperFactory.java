package uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping;

import static uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils.registerNs;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.resources.Const;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Feb 2016</dd></dl>
 *
 */
public class MyEqRdfMapperFactory extends RdfMapperFactory
{
	/**
	 * Which RDF predicate is used to link two equivalent entities together. Default is "owl", corresponding to
	 * "owl:sameAs", the alternative is "shema.org", which uses http://schema.org/sameAs, as defined by 
	 * <a href = "http://topbraid.org/schema/">TopBraid</a>. This second option is made available because 
	 * <a href = "https://www.w3.org/2009/12/rdf-ws/papers/ws21">links based on owl:sameAs might not be what you mean</a>,
	 * in the sense that this type of relationships might be formally too strong and too committing for a given use case, 
	 * and therefore you might prefer something more lightweight (i.e., two URIs might be declared the "same" due to the
	 * fact they refer to the same real world entity, but wouldn't need to merge consistent statements about them, as
	 * reasoners end up doing when owl:sameAs is used).
	 * 
	 * TODO: document this in the wiki and in the command line
	 */
	public static final String SAME_AS_TYPE_PROP = new String ( Const.PROP_PREFIX + "same_as_type" );
	
	private boolean isOwlSameAs = true;
	
	
	static {
		init ();
	}
	
	{
		setMapper ( EntityMappingSearchResult.Bundle.class, new BundleRdfMapper () );
		setMapper ( Entity.class, new EntityRdfMapper() );
		setMapper ( Service.class, new ServiceRdfMapper () );
		setMapper ( Repository.class, new RepositoryRdfMapper () );
		setMapper ( ServiceCollection.class, new ServCollRdfMapper () );
		
		this.setOwlSameAs ( "owl".equals ( System.getProperty ( SAME_AS_TYPE_PROP, "owl" ) ) );
	}

	public MyEqRdfMapperFactory () {
		super ();
	}

	public MyEqRdfMapperFactory ( OWLOntology knowledgeBase ) {
		super ( knowledgeBase );
	}

	
	public boolean isOwlSameAs () {
		return isOwlSameAs;
	}

	public void setOwlSameAs ( boolean isOwlSameAs ) {
		this.isOwlSameAs = isOwlSameAs;
	}

	
	public static void init () 
	{
		registerNs ( "myeq", 	 				"http://rdf.ebi.ac.uk/terms/myeq#" );
		registerNs ( "myeqres",				"http://rdf.ebi.ac.uk/resource/myeq#" );
		registerNs ( "schema", 				"http://schema.org/" );
	}
}
