package uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping;

import static uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils.uri;
import static uk.ac.ebi.fg.java2rdf.utils.OwlApiUtils.assertLink;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ebi.fg.java2rdf.mapping.BeanRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.ObjRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.RdfMapperFactory;
import uk.ac.ebi.fg.java2rdf.mapping.urigen.RdfUriGenerator;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Entity;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Feb 2016</dd></dl>
 *
 */
public class BundleRdfMapper extends ObjRdfMapper<EntityMappingSearchResult.Bundle>
{

	@Override
	public boolean map ( Bundle bundle, Map<String, Object> params )
	{
		if ( bundle == null ) return false;

		RdfMapperFactory mapFactory = this.getMapperFactory ();
		BeanRdfMapper<Entity> emapper = (BeanRdfMapper<Entity>) mapFactory.getMapper ( Entity.class );
		RdfUriGenerator<Entity> eUriGen = emapper.getRdfUriGenerator ();

		String euri0 = null;
		for ( Entity e: bundle.getEntities () )
		{
			// Let's map the entity
			emapper.map ( e, params );

			// Let's state the equivalence
			String euri = eUriGen.getUri ( e, params );
			
			// Let's state the equivalence
			if ( euri0 == null ) {
				euri0 = euri;
				continue;
			}

			RdfMapperFactory mapFact = this.getMapperFactory ();
			OWLOntology onto = mapFact.getKnowledgeBase ();

			String sameAsProp = ( (MyEqRdfMapperFactory) this.getMapperFactory () ).isOwlSameAs ()
				? "owl:sameAs" : "schema:sameAs";
			
			assertLink ( onto, euri0, uri ( sameAsProp ), euri );
		}
		
		return true;
	}

}
