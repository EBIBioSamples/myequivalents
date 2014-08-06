package uk.ac.ebi.fg.myequivalents.provenance.db.dao;

import static uk.ac.ebi.utils.sql.HqlUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.utils.EntityMappingUtils;
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
	private final Logger log = LoggerFactory.getLogger ( this.getClass () );

	
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
	
	
	
	
	public List<ProvenanceRegisterEntry> findEntityMappingProv ( String entityId, List<String> validUsers )
	{
		String[] echunks = EntityMappingUtils.parseEntityId ( entityId );
		// SELECT DISTINCT e FROM ProvenanceRegisterEntry AS e JOIN e.parameters AS x
		// WHERE e.operation LIKE 'mapping.storeMapping%' AND x.valueType = 'entity'
		// AND x.value = 'service' AND x.extraValue = 'acc'
		// AND e.userEmail IN ( ... )

		Session sess = (Session) this.entityManager.getDelegate ();
		DetachedCriteria crit = DetachedCriteria.forClass ( ProvenanceRegisterEntry.class, "e" );
		crit.setProjection ( Projections.distinct ( Projections.id () ) );
		crit.createAlias ( "e.parameters", "x" );
		crit.add ( Restrictions.like ( "e.operation", "mapping.storeMapping%" ) );
		crit.add ( Restrictions.eq ( "x.valueType", "entity" ) );
		crit.add ( Restrictions.eq ( "x.value", echunks [ 0 ] ) );
		crit.add ( Restrictions.eq ( "x.extraValue", echunks [ 1 ] ) );
		if ( validUsers != null && !validUsers.isEmpty () ) crit.add ( Restrictions.in ( "e.userEmail", validUsers ) );
		
		return sess.createCriteria ( ProvenanceRegisterEntry.class )
			.add ( Property.forName ( "id" ).in ( crit ) )
			.list ();
	}
	
	public Set<List<ProvenanceRegisterEntry>> findMappingProv ( String xEntityId, String yEntityId, List<String> validUsers )
	{
		Set<List<ProvenanceRegisterEntry>> result = new HashSet<> ();
		List<List<ProvenanceRegisterEntry>> dupedResult =
			findMappingProv ( xEntityId, yEntityId, validUsers, new HashSet<ProvenanceRegisterEntry> () );
		result.addAll ( dupedResult );
		return result;
	}

	
	private List<List<ProvenanceRegisterEntry>> findMappingProv ( 
		String xEntityId, String yEntityId, List<String> validUsers, Set<ProvenanceRegisterEntry> visitedPaths )
	{
		log.trace ( "doing findMappingProv ( '{}', '{}', ...)", xEntityId, yEntityId );
		
		List<List<ProvenanceRegisterEntry>> result = new ArrayList<> ();
		
		// What can you reach from x?
		List<ProvenanceRegisterEntry> xlinks = findEntityMappingProv ( xEntityId, validUsers ), ylinks = null;
		log.trace ( "xlinks are: {}", xlinks );
		
		for ( ProvenanceRegisterEntry opx1: xlinks )
		{
			if ( visitedPaths.contains ( opx1 ) ) continue;
			
			for ( ProvenanceRegisterParameter px1: opx1.getParameters () )
			{
				String x1 = px1.getValue () + ":" + px1.getExtraValue ();
				if ( xEntityId.equals ( x1 ) ) continue; // ignore yourself
				if ( yEntityId.equals ( x1 ) ) 
				{
					List<ProvenanceRegisterEntry> opx1l = new ArrayList<> (); opx1l.add ( opx1 );
					result.add ( opx1l ); // add a direct link as a (modifiable) solution
					log.trace ( "Solution added: {}", opx1 );
					continue;
				}
				
				// Consider what it goes to y and try a shorter path
				if ( ylinks == null ) {
					ylinks = findEntityMappingProv ( yEntityId, validUsers );
					log.trace ( "ylinks are: {}", ylinks );
				}
				
				for ( ProvenanceRegisterEntry opy1: ylinks )
				{
					if ( visitedPaths.contains ( opy1 ) ) continue;
					
					for ( ProvenanceRegisterParameter py1: opy1.getParameters () )
					{
						String y1 = py1.getValue () + ":" + py1.getExtraValue ();
						if ( x1.equals ( y1 ) )
						{
							// Add (x, x1) (x1, y1) (y1, y)
							List<ProvenanceRegisterEntry> opx1y1 = new ArrayList<> (); 
							opx1y1.add ( opx1 );
							opx1y1.add ( opy1 );
							result.add ( opx1y1 );
							log.trace ( "Solution added: {}", opx1y1 );
							continue;
						}
						
						// Let's check the shorter path
						for ( List<ProvenanceRegisterEntry> chain: findMappingProv ( x1, y1, validUsers, visitedPaths ) )
						{
							chain.add ( 0, opx1 );
							chain.add ( opy1 );
							result.add ( chain );
							log.trace ( "Solution added: {}", chain );
						}
					} // py1

					visitedPaths.add ( opy1 );
				} // opy1
			} // px1

			visitedPaths.add ( opx1 );
		} // opx1
		
		return result;
	}
	
	
	

	public int purge ( Date from, Date to )
	{
		Set<ProvenanceRegisterEntry> toDelete = new HashSet<> (), toKeep = new HashSet<> ();
		
		// Search entries, joined with params and order by param type/value, then date
		String hqlSel = "SELECT DISTINCT e, p.valueType AS ptype, p.value AS pval, p.extraValue AS extra\n"
				+ "FROM ProvenanceRegisterEntry e JOIN e.parameters AS p\n"
				+ "WHERE " 
				+ parameterizedRangeClause ( "e.timestamp", "from", "to", from, to ) + "\n"
				+ "ORDER BY p.valueType, p.value, p.extraValue, e.timestamp DESC, e.id";
		
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
			}
			else
			{
				// Move to another sequence about the current parameter
				toKeep.add ( (ProvenanceRegisterEntry) row [ 0 ] );
				firstParamStr = paramStr;
			}
		}
		
		int result = 0;

		for ( ProvenanceRegisterEntry prove: toDelete )
			if ( !toKeep.contains ( prove ) ) 
			{
				this.entityManager.remove ( prove );
				result++;
			}
		
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
