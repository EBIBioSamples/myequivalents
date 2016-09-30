package uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping;

import uk.ac.ebi.fg.myequivalents.model.Repository;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Feb 2016</dd></dl>
 *
 */
public class RepositoryRdfMapper extends DescribeableRdfMapper<Repository>
{
	public RepositoryRdfMapper () {
		super ( "Repository", "repo" );
	}
}
