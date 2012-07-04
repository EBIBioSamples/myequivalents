package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

public class ServiceCollectionDAO extends DescribeableDAO<ServiceCollection>
{
	public ServiceCollectionDAO ( EntityManager entityManager )
	{
		super ( entityManager );
	}
}
