package uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping;

import static uk.ac.ebi.fg.java2rdf.utils.Java2RdfUtils.urlEncode;
import static uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils.uri;

import java.util.Map;

import uk.ac.ebi.fg.java2rdf.mapping.BeanRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.properties.OwlDatatypePropRdfMapper;
import uk.ac.ebi.fg.java2rdf.mapping.urigen.RdfUriGenerator;
import uk.ac.ebi.fg.myequivalents.model.Describeable;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Feb 2016</dd></dl>
 *
 */
public abstract class DescribeableRdfMapper<D extends Describeable> extends BeanRdfMapper<D>
{

	public DescribeableRdfMapper ( String owlClassName, final String rdfNamePrefix )
	{
		super ( 
			uri ( "myeq", owlClassName ), 
			new RdfUriGenerator<D>() {
				@Override public String getUri ( D d, Map<String, Object> params ) {
					return uri ( "myeqres", rdfNamePrefix + "_" + urlEncode ( d.getName () ) );
				}
			}
		);
		
		this.addPropertyMapper ( "name", new OwlDatatypePropRdfMapper<D, String> ( uri ( "dc-terms", "identifier" ) ) );
		this.addPropertyMapper ( "title", new OwlDatatypePropRdfMapper<D, String> ( uri ( "dc-terms", "title" ) ) );
		this.addPropertyMapper ( "description", new OwlDatatypePropRdfMapper<D, String> ( uri ( "dc-terms", "description" ) ) );
		this.addPropertyMapper ( "releaseDate", new OwlDatatypePropRdfMapper<D, String> ( uri ( "dc-terms", "issued" ) ) );
	}

	@Override
	public boolean map ( D d, Map<String, Object> params )
	{
		if ( d == null || !d.isPublic () ) return false;
		return super.map ( d, params );
	}
	
}
