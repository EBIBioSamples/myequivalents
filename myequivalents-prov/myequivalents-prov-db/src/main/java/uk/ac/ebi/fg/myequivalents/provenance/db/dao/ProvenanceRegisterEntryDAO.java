package uk.ac.ebi.fg.myequivalents.provenance.db.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.Validate;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>31 Mar 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceRegisterEntryDAO
{
	private EntityManager entityManager;
	
	public ProvenanceRegisterEntryDAO ( EntityManager em ) {
		this.entityManager = em;
	}

	public void create ( ProvenanceRegisterEntry provEntry ) 
	{
		Validate.notNull ( provEntry, "Cannot save a null entry in the provenance register" );
		this.entityManager.persist ( provEntry );
	}
	
	@SuppressWarnings ( "unchecked" )
	public List<ProvenanceRegisterEntry> find ( 
		String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params 
	)
	{
		Session sess = (Session) this.entityManager.getDelegate ();
		DetachedCriteria crit = DetachedCriteria.forClass ( ProvenanceRegisterEntry.class, "prove" );
		crit.setProjection ( Projections.distinct ( Projections.id () ) );
		
		if ( userEmail != null )  crit.add ( Restrictions.like ( "userEmail", userEmail ) );
		if ( operation != null )  crit.add ( Restrictions.like ( "operation", operation ) );
				
		if ( from != null ) 
			crit.add ( to != null 
				? Restrictions.between ( "timestamp", from, to )
				: Restrictions.ge ( "timestamp", from )
			);
		else if ( to != null ) crit.add ( Restrictions.le ( "timestamp", to ) ); 

		if ( params != null && !params.isEmpty () )
		{
			
			Disjunction paramCrit = Restrictions.disjunction ();
			
			for ( ProvenanceRegisterParameter param: params  )
			{
				String ptype = param.getValueType (), pval = param.getValue (), extra = param.getExtraValue ();
				if ( ptype == null && pval == null && extra == null ) continue;
						
				Conjunction and = Restrictions.and ();
				if ( ptype != null ) and.add ( Restrictions.like ( "param.valueType", ptype ) );
				if ( pval != null ) and.add ( Restrictions.like ( "param.value", pval ) );
				if ( extra != null ) and.add ( Restrictions.like ( "param.extraValue", extra ) );
				paramCrit.add ( and );
			}

			crit.createAlias ( "prove.parameters", "param" );
			crit.add ( paramCrit );
		}
				
		return sess.createCriteria ( ProvenanceRegisterEntry.class )
			.add ( Property.forName ( "id" ).in ( crit ) )
			.list ();
	}
	
	/**
	 * Uses JodaTime, this ease queries like '10 days ago': new DateTime ().minusDays ( 10 ) 
	 */
	public List<ProvenanceRegisterEntry> find ( 
		String userEmail, String operation, DateTime from, DateTime to, List<ProvenanceRegisterParameter> parameterPairs )
	{
		return find ( userEmail, operation, from == null ? null : from.toDate (), to == null ? null : to.toDate (), parameterPairs );
	}
	
	public List<ProvenanceRegisterEntry> find ( 
		String userEmail, String operation, List<ProvenanceRegisterParameter> parameterPairs ) 
	{
		return this.find ( userEmail, operation, (Date) null, (Date) null, parameterPairs );
	}
	
	

	public int purge ( Date from, Date to )
	{
		List<ProvenanceRegisterEntry> removedEntries = this.find ( null, null, from, to, null );
		if ( removedEntries == null ) return 0;
		
		for ( ProvenanceRegisterEntry prove: removedEntries )
			this.entityManager.remove ( prove );
		return removedEntries.size ();
	}

}
