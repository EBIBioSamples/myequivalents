package uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping;

import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Feb 2016</dd></dl>
 *
 */
public class ServCollRdfMapper extends DescribeableRdfMapper<ServiceCollection>
{
	public ServCollRdfMapper () {
		super ( "ServiceCollection", "servcoll" );
	}
}
