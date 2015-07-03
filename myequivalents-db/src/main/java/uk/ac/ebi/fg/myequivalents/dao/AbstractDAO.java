package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.myequivalents.utils.DbEntityIdResolver;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Jul 2015</dd>
 *
 * @param <T>
 */
public abstract class AbstractDAO
{
	protected EntityManager entityManager;
	protected EntityIdResolver entityIdResolver;


	protected AbstractDAO ( EntityManager entityManager )
	{
		this.setEntityManager ( entityManager );
	}

	public EntityManager getEntityManager () {
		return entityManager;
	}

	public void setEntityManager ( EntityManager entityManager ) {
		this.entityManager = entityManager;
	}


	/**
	 * This is null by default, {@link DbEntityIdResolver} is the typical instance we use at the moment, 
	 * you can define a custom resolver here.
	 */
	public EntityIdResolver getEntityIdResolver () {
		return entityIdResolver;
	}

	public void setEntityIdResolver ( EntityIdResolver entityIdResolver ) {
		this.entityIdResolver = entityIdResolver;
	}
	
}