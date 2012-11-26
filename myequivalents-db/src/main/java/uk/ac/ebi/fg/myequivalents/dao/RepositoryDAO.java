package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.model.Repository;

/**
 * The {@link Repository} DAO. 
 *
 * <dl><dt>date</dt><dd>Jul 19, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class RepositoryDAO extends DescribeableDAO<Repository>
{
	public RepositoryDAO ( EntityManager entityManager )
	{
		super ( entityManager );
	}
}
