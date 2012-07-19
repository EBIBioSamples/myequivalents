package uk.ac.ebi.fg.myequivalents.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.utils.reflection.ReflectionUtils;

/**
 * Generic DAO for the {@link Describeable} types.
 *
 * <dl><dt>date</dt><dd>Jul 19, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class DescribeableDAO<D extends Describeable>
{
	private EntityManager entityManager;
	protected final Class<? super D> targetClass;
	
	
	protected DescribeableDAO ( EntityManager entityManager, Class<? super D> targetClass )
	{
		super ();
		this.entityManager = entityManager;
		this.targetClass = targetClass;
	}

	/** 
	 * Setup the class corresponding to D automatically, extracting it by means of reflection.
	 * 
	 * WARNING: This method won't work if the DAO extension you invoke it from still has a generic for D (which extends it).
	 * If you have such case, you need to either forbid this initialisation approach, or to declare the DAO abstract 
	 * and use anonymous classes to instantiate specific DAOs.
	 * 
	 */
	protected DescribeableDAO ( EntityManager entityManager )
	{
		this.entityManager = entityManager;
		this.targetClass = ReflectionUtils.getTypeArgument ( DescribeableDAO.class, this.getClass(), 0 );
		if ( targetClass == null ) 
			throw new PersistenceException ( "Internal error: getTypeArgument() returns null for " + this.getClass ().getName () );
	}
	
	public void store ( D describeable )
	{
		if ( describeable == null )
			throw new NullPointerException ( "Cannot update a null object" );
		entityManager.merge ( describeable );
	}
		
	public void deleteAll ()
	{
		entityManager.createQuery ( "DELETE from " + targetClass.getName () ).executeUpdate ();
	}
	
	
	public D findByName ( String describeableName )
	{
		describeableName = StringUtils.trimToNull ( describeableName );
		if ( describeableName == null ) return null;
		
		Query q = entityManager.createQuery ( 
			"FROM " + targetClass.getName () + " WHERE name = '" + describeableName + "'" );

		@SuppressWarnings ( "unchecked" )
		List<D> results = q.getResultList ();
		
		return results.isEmpty () ? null : results.iterator ().next ();
	}

	public D findByName ( D describeable )
	{
		if ( describeable == null ) return null;
		return findByName ( describeable.getName () );
	}

	
	public boolean exists ( String describeableName )
	{
		describeableName = StringUtils.trimToNull ( describeableName );
		if ( describeableName == null ) return false;

		Query q = entityManager.createQuery ( 
			"SELECT name FROM " + targetClass.getName () + " WHERE name = '" + describeableName + "'" );
		
		@SuppressWarnings ( "unchecked" )
		List<String> names = q.getResultList ();
		
		return !names.isEmpty ();
	}

	public boolean exists ( D describeable )
	{
		if ( describeable == null ) return false;
		return exists ( describeable.getName () );
	}

	public EntityManager getEntityManager ()
	{
		return entityManager;
	}

	public boolean delete ( String describeableName )
	{
		describeableName = StringUtils.trimToNull ( describeableName );
		if ( describeableName == null ) return false;
		
		Query q = entityManager.createQuery ( 
			"DELETE from " + targetClass.getName () + " WHERE name = '" + describeableName + "'" );
		return q.executeUpdate () > 0;
	}

	public boolean delete ( D describeable )
	{
		if ( describeable == null ) return false;
		return delete ( describeable.getName () );
	}

	public void setEntityManager ( EntityManager entityManager )
	{
		this.entityManager = entityManager;
	}

}
