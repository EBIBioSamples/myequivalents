package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.model.Repository;

public class RepositoryDAO extends DescribeableDAO<Repository>
{
	public RepositoryDAO ( EntityManager entityManager )
	{
		super ( entityManager );
	}
}
