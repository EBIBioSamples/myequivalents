package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.model.Service;

public class ServiceDAO extends DescribeableDAO<Service>
{
	public ServiceDAO ( EntityManager entityManager )
	{
		super ( entityManager );
	}
}
