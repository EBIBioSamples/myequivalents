package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

/**
 * A generic DAO, having functions for the management of a single entity type. There are other DAOs managing 
 * combinations of related entity types (yes, this is not fully compliant with the usual DAO design pattern).
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 Feb 2015</dd>
 *
 * @param <T>
 */
public abstract class AbstractTargetedDAO<T>
{
	protected EntityManager entityManager;
	protected Class<? super T> targetClass;

	public AbstractTargetedDAO ( EntityManager entityManager, Class<? super T> targetClass )
	{
		this.entityManager = entityManager;
		this.targetClass = targetClass;
	}
	
	/**
	 * The number of entities of type {@link #targetClass} available in the database.
	 */
	public long count ()
	{
		return (Long) entityManager.createQuery ( "SELECT COUNT( * ) FROM " + this.targetClass.getName () + " T" )
			.getSingleResult ();
	}
	
	public void setEntityManager ( EntityManager entityManager )
	{
		this.entityManager = entityManager;
	}

}