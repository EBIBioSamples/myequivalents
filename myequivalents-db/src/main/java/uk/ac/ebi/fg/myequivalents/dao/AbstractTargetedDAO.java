package uk.ac.ebi.fg.myequivalents.dao;

import javax.persistence.EntityManager;

/**
 * TODO: comment me!
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