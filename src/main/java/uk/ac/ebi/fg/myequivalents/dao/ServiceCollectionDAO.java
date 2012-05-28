package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.model.Repository;

public class ServiceCollectionDAO extends DescribeableDAO<Repository>
{
	public ServiceCollectionDAO ( EntityManager entityManager )
	{
		super ( entityManager );
	}
}
