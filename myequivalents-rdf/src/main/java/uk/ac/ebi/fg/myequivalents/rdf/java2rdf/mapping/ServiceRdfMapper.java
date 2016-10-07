package uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping;

import static uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils.uri;

import uk.ac.ebi.fg.java2rdf.mapping.properties.OwlDatatypePropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.OwlObjPropRdfMapper;
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
public class ServiceRdfMapper extends DescribeableRdfMapper<Service>
{

	public ServiceRdfMapper () 
	{
		super ( "Service", "service" );

		this.addPropertyMapper ( "entityType", new OwlDatatypePropRdfMapper<Service, String> ( uri ( "dc", "type" ) ) );
		this.addPropertyMapper ( "uriPattern", new OwlDatatypePropRdfMapper<Service, String> ( uri ( "myeq", "has-uri-pattern" ) ) );
		this.addPropertyMapper ( "repository", new OwlObjPropRdfMapper<Service, Repository> ( uri ( "myeq", "has-repository" ) ) );
		this.addPropertyMapper ( "serviceCollection", new OwlObjPropRdfMapper<Service, ServiceCollection> ( uri ( "myeq", "has-service-collection" ) ) );
	}

}
