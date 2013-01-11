package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * The {@link ServiceCollection} DAO.
 *
 * <dl><dt>date</dt><dd>Jul 19, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceCollectionDAO extends DescribeableDAO<ServiceCollection>
{
	public ServiceCollectionDAO ( EntityManager entityManager )
	{
		super ( entityManager );
	}
}
