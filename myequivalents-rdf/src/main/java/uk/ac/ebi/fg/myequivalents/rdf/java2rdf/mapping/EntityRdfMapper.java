package uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping;

import static uk.ac.ebi.fg.java2rdf.utils.Java2RdfUtils.urlEncode;
import static uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils.uri;

import java.util.Map;

import uk.ac.ebi.fg.java2rdf.mapping.BeanRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.OwlDatatypePropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.OwlObjPropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.urigen.RdfUriGenerator;
import uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.Service;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Feb 2016</dd></dl>
 *
 */
public class EntityRdfMapper extends BeanRdfMapper<Entity>
{
	public EntityRdfMapper ()
	{
		super ( 
			uri ( "myeq", "Entity" ), 
			new RdfUriGenerator<Entity> ()
			{
				@Override
				public String getUri ( Entity e, Map<String, Object> params )
				{
					if ( e == null ) return null;
					String uri = e.getURI ();
					if ( uri == null ) uri = uri ( 
						"myeqres", "entity:" + urlEncode ( e.getServiceName () ) + ':' + urlEncode ( e.getAccession () ) 
					);
					return uri;
				}
			}
		);
		
		this.addPropertyMapper ( "accession", new OwlDatatypePropRdfMapper<Entity, String> ( uri ( "dc-terms:identifier" ) ) );
		this.addPropertyMapper ( "releaseDate", new OwlDatatypePropRdfMapper<Entity, String> ( uri ( "dc-terms:issued" ) ) );
		this.addPropertyMapper ( "service", new OwlObjPropRdfMapper<Entity, Service> ( uri ( "myeq:has-service" ) ) );
	}

	@Override
	public boolean map ( Entity e, Map<String, Object> params )
	{
		if ( e == null || !e.isPublic () ) return false;
		return super.map ( e, params );
	}
	
}
