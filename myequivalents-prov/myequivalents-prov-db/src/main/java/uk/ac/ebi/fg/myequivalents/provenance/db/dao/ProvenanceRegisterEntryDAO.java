package uk.ac.ebi.fg.myequivalents.provenance.db.dao;

import static uk.ac.ebi.utils.sql.HqlUtils.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

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
import uk.ac.ebi.utils.sql.HqlUtils;
import uk.ac.ebi.utils.sql.SqlUtils;

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
		int result = 0;

		Set<ProvenanceRegisterEntry> toDelete = new HashSet<> (), toKeep = new HashSet<> ();
		
		// Search entries, joined with params and order by param type/value, then date
		String hqlSel = "SELECT DISTINCT e, p.valueType AS ptype, p.value AS pval, p.extraValue AS extra\n"
				+ "FROM ProvenanceRegisterEntry e JOIN e.parameters AS p\n"
				+ "WHERE " 
				+ parameterizedRangeClause ( "e.timestamp", "from", "to", from, to ) + "\n"
				+ "ORDER BY p.valueType, p.value, p.extraValue, e.timestamp DESC";
		
		Query qsel = this.entityManager.createQuery ( hqlSel );
		parameterizedRangeBinding ( qsel, "from", "to", from, to );
		
		String firstParamStr = null;
		for ( Object[] row: (List<Object[]>) qsel.getResultList () )
		{
			// System.out.println ( "--- analysing: " + Arrays.asList ( row ) );
			String paramStr = (String) row [ 1 ] + row [ 2 ] + row [ 3 ];
			if ( paramStr.equals ( firstParamStr ) )
			{
				// Keep removing entries of the same type that appears after the first
				toDelete.add ( (ProvenanceRegisterEntry) row [ 0 ] ); 
				result++;
			}
			else
			{
				// Move to another sequence about the current parameter
				toKeep.add ( (ProvenanceRegisterEntry) row [ 0 ] );
				firstParamStr = paramStr;
			}
		}
		
		for ( ProvenanceRegisterEntry prove: toDelete )
			if ( !toKeep.contains ( prove ) ) this.entityManager.remove ( prove );
		
		return result;
	}

	public int purgeAll ( Date from, Date to )
	{
		List<ProvenanceRegisterEntry> removedEntries = this.find ( null, null, from, to, null );
		if ( removedEntries == null ) return 0;
		
		for ( ProvenanceRegisterEntry prove: removedEntries )
			this.entityManager.remove ( prove );
		return removedEntries.size ();
	}
}
