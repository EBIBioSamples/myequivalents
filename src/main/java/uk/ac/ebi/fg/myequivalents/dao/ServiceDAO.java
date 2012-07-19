package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.model.Service;

/**
 * the {@link Service} DAO. 
 *
 * <dl><dt>date</dt><dd>Jul 19, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceDAO extends DescribeableDAO<Service>
{
	public ServiceDAO ( EntityManager entityManager )
	{
		super ( entityManager );
	}
}
