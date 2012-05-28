package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.model.Repository;

public class EntityCollectionDAO extends DescribeableDAO<Repository>
{
	public EntityCollectionDAO ( EntityManager entityManager )
	{
		super ( entityManager );
	}
}
