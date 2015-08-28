package uk.ac.ebi.fg.myequivalents.provenance.db.dao;

import static uk.ac.ebi.utils.sql.HqlUtils.parameterizedRangeBinding;
import static uk.ac.ebi.utils.sql.HqlUtils.parameterizedRangeClause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.Validate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.dao.AbstractTargetedDAO;
import uk.ac.ebi.fg.myequivalents.model.EntityId;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.utils.DbEntityIdResolver;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;

/**
 * DB Storage management for {@link ProvenanceRegisterEntry} 
 *
 * <dl><dt>date</dt><dd>31 Mar 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceRegisterEntryDAO extends AbstractTargetedDAO<ProvenanceRegisterEntry>
{
	private final Logger log = LoggerFactory.getLogger ( this.getClass () );

	
	public ProvenanceRegisterEntryDAO ( EntityManager em )
	{
		super ( em, ProvenanceRegisterEntry.class );
		this.setEntityIdResolver ( new DbEntityIdResolver ( em ) );
	}

	public void create ( ProvenanceRegisterEntry provEntry ) 
	{
		Validate.notNull ( provEntry, "Cannot save a null entry in the provenance register" );
		this.entityManager.persist ( provEntry );
	}

	/**
	 * Implements {@link ProvRegistryManager#find(String, String, Date, Date, List)} as a search into the myEquivalents
	 * relational database.
	 */
	@SuppressWarnings ( "unchecked" )
	public List<ProvenanceRegisterEntry> find ( 
		String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params 
	)
	{
		StringBuilder sql = new StringBuilder ( "SELECT DISTINCT * FROM provenance_register prove WHERE 1=1\n" );
		
		if ( userEmail != null ) sql.append ( "AND user_email LIKE :email\n" );
		if ( operation != null ) sql.append ( "AND operation LIKE :operation\n" );
		
		if ( from != null ) sql.append ( "AND timestamp >= :from\n" );
		if ( to != null ) sql.append ( "AND timestamp <= :to\n" );

		// All the params, in AND.
		if ( params != null && !params.isEmpty () )
		{
			int i = 0;
			for ( ProvenanceRegisterParameter param: params  )
			{
				String ptype = param.getValueType (), pval = param.getValue (), extra = param.getExtraValue ();
				String sqlParam = "";
				
				if ( ptype != null ) sqlParam += " AND valuetype LIKE :ptype" + i;
				if ( pval != null ) sqlParam += " AND value LIKE :pval" + i;
				if ( extra != null ) sqlParam += " AND extravalue LIKE :pextra" + i;

				i++;
				
				if ( sqlParam.length () == 0 ) continue;
				
				sqlParam = "1=1" + sqlParam;
				sql.append ( "AND id IN ( SELECT prov_entry_id FROM provenance_register_parameter WHERE " + sqlParam + " )\n" );
			}
		}

		// And now redo the same, to fill the parameters
		Session sess = (Session) this.entityManager.getDelegate ();
		SQLQuery query = sess.createSQLQuery ( sql.toString () );
		
		if ( userEmail != null ) query.setParameter ( "email", userEmail );
		if ( operation != null ) query.setParameter ( "operation", operation );
		if ( from != null ) query.setParameter ( "from", from );
		if ( to != null ) query.setParameter ( "to", to );

		if ( params != null && !params.isEmpty () )
		{
			int i = 0;
			for ( ProvenanceRegisterParameter param: params  )
			{
				String ptype = param.getValueType (), pval = param.getValue (), pextra = param.getExtraValue ();
				
				if ( ptype != null ) query.setParameter ( "ptype" + i, ptype ); 
				if ( pval != null ) query.setParameter ( "pval" + i, pval ); 
				if ( pextra != null ) query.setParameter ( "pextra" + i, pextra ); 

				i++;
			}
		}
		
		// Whee! Now materialise it!
		return query.addEntity ( ProvenanceRegisterEntry.class ).list ();
	}
	
	/**
	 * Uses JodaTime, this ease queries like '10 days ago': new DateTime ().minusDays ( 10 ) 
	 */
	public List<ProvenanceRegisterEntry> find ( 
		String userEmail, String operation, DateTime from, DateTime to, List<ProvenanceRegisterParameter> parameterPairs )
	{
		return find ( userEmail, operation, from == null ? null : from.toDate (), to == null ? null : to.toDate (), parameterPairs );
	}
	
	/**
	 * Searches over any date. 
	 */
	public List<ProvenanceRegisterEntry> find ( 
		String userEmail, String operation, List<ProvenanceRegisterParameter> parameterPairs ) 
	{
		return this.find ( userEmail, operation, (Date) null, (Date) null, parameterPairs );
	}
	
	
	
	/**
	 * Implements {@link ProvRegistryManager#findEntityMappingProv(String, List)} as a search into the myEquivalents
	 * relational database.
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	public List<ProvenanceRegisterEntry> findEntityMappingProv ( String entityId, List<String> validUsers )
	{
		EntityIdResolver idresolver = this.getEntityIdResolver ();
		EntityId eidObj = idresolver.doall ( entityId );
		
		// SELECT DISTINCT e FROM ProvenanceRegisterEntry AS e JOIN e.parameters AS x
		// WHERE e.operation IN ( 'mapping.storeMappings', 'mapping.storeMappingBundle' ) AND x.valueType = 'entity'
		// AND x.value = 'service' AND x.extraValue = 'acc'
		// AND e.userEmail IN ( ... )

		Session sess = (Session) this.entityManager.getDelegate ();
		DetachedCriteria crit = DetachedCriteria.forClass ( ProvenanceRegisterEntry.class, "e" );
		crit.setProjection ( Projections.distinct ( Projections.id () ) );
		crit.createAlias ( "e.parameters", "x" );
		crit.add ( Restrictions.in ( "e.operation", new String[] { "mapping.storeMappings", "mapping.storeMappingBundle" } ) );
		crit.add ( Restrictions.eq ( "x.valueType", "entity" ) );
		crit.add ( Restrictions.eq ( "x.value", eidObj.getServiceName () ) );
		crit.add ( Restrictions.eq ( "x.extraValue", eidObj.getAcc () ) );
		if ( validUsers != null && !validUsers.isEmpty () ) crit.add ( Restrictions.in ( "e.userEmail", validUsers ) );
		
		
		return sess.createCriteria ( ProvenanceRegisterEntry.class )
			.add ( Property.forName ( "id" ).in ( crit ) )
			.addOrder ( Order.desc ( "timestamp" ) )
			.list ();
	}
	
	/**
	 * Implements {@link ProvRegistryManager#findMappingProv(String, String, List)} as a search into the myEquivalents
	 * relational database.
	 */
	public Set<List<ProvenanceRegisterEntry>> findMappingProv ( String xEntityId, String yEntityId, List<String> validUsers )
	{
		log.trace ( "doing public findMappingProv ( '{}', '{}', ...)", xEntityId, yEntityId );
		Set<List<ProvenanceRegisterEntry>> result = new HashSet<> ();
		List<List<ProvenanceRegisterEntry>> dupedResult = 
			findMappingProv ( xEntityId, yEntityId, validUsers, new HashSet<ProvenanceRegisterEntry> () );
		result.addAll ( dupedResult );
		log.trace ( "returning from public findMappingProv():\n{}", result );
		return result;
	}

	/**
	 * Does the recursion over closer pair of entities, considering what has already been visited. 
	 */
	private List<List<ProvenanceRegisterEntry>> findMappingProv ( 
		String xEntityId, String yEntityId, List<String> validUsers, Set<ProvenanceRegisterEntry> visitedEntities )
	{
		log.trace ( "doing findMappingProv ( '{}', '{}', ...)", xEntityId, yEntityId );
		
		List<List<ProvenanceRegisterEntry>> result = new ArrayList<> ();
		if ( xEntityId.equals ( yEntityId ) ) return result; // Just to prevent trivial cases coming from the clients 
		
		// What can you reach from x?
		List<ProvenanceRegisterEntry> xlinks = findEntityMappingProv ( xEntityId, validUsers );
		log.trace ( "xlinks are: {}", xlinks );
		
		for ( ProvenanceRegisterEntry opx1: xlinks )
		{
			if ( visitedEntities.contains ( opx1 ) ) continue; // do not loop on yourself
			visitedEntities.add ( opx1 ); // Do not dead-end on already-visited entries
			
			// Examine the direct links to x
			for ( ProvenanceRegisterParameter px1: opx1.getParameters () )
			{
				String x1 = px1.getValue () + ":" + px1.getExtraValue ();
				if ( xEntityId.equals ( x1 ) ) continue; // ignore yourself
				
				if ( yEntityId.equals ( x1 ) )
				{
					// x-y is a direct link, save the result.
					List<ProvenanceRegisterEntry> opx1l = new ArrayList<> (); opx1l.add ( opx1 );
					result.add ( opx1l ); // add a direct link as a (modifiable) solution
					log.trace ( "Solution added: {}", opx1l );
					continue;
				}

				// Recursively expand the graph from x1, to try to reach y
				List<List<ProvenanceRegisterEntry>> chains = findMappingProv ( x1, yEntityId, validUsers, visitedEntities );
				for ( List<ProvenanceRegisterEntry> chain: chains )
				{
					// For each solution starting from x1, add x->x1 up front and then you have an x-y solution 
					chain.add ( 0, opx1 );
					result.add ( chain );
					log.trace ( "Solution added: {}", chain );
				}
			} // for px1
		} // for opx1
		
		return result;
	}
	
	
	
	/**
	 * Implements {@link ProvRegistryManager#purge(Date, Date)} as a search into the myEquivalents
	 * relational database.
	 */
	@SuppressWarnings ( "unchecked" )
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
		
		// The last param in a list of myEq objects
		String firstParamStr = null;
		for ( Object[] row: (List<Object[]>) qsel.getResultList () )
		{
			if ( log.isTraceEnabled () ) log.trace ( "--- analysing entry/param: " + Arrays.asList ( row ) );
			String paramStr = (String) row [ 1 ] + row [ 2 ] + row [ 3 ];
			if ( paramStr.equals ( firstParamStr ) )
			{
				// Keep removing entries of the same type that appears after the first
				toDelete.add ( (ProvenanceRegisterEntry) row [ 0 ] ); 
			}
			else
			{
				ProvenanceRegisterEntry e = (ProvenanceRegisterEntry) row [ 0 ];
				if ( e.getOperation ().contains ( "delete" ) )
					// We delete operations anyway
					toDelete.add ( e );
				else
					// Keep the last operation about the current parameter
					toKeep.add ( (ProvenanceRegisterEntry) row [ 0 ] );
				
				// Consider all subsequent operations about the same parameter
				firstParamStr = paramStr;
			}
		}
		
		// So, we found which ones to delete, do it
		// We need to track the entries to keep and to delete by means of the to corresponding sets, because they might
		// be repeated in multiple provenance entries
		int result = 0;
		for ( ProvenanceRegisterEntry prove: toDelete )
			if ( !toKeep.contains ( prove ) ) 
			{
				this.entityManager.remove ( prove );
				result++;
			}
		
		return result;
	}

	/**
	 * Deletes all the provenance entries in a range, even if this will make any provenance information about a myEq object
	 * to disappear. This should only be used for testing purposes and you should prefer {@link #purge(Date, Date)} for 
	 * production cleaning. In fact, this facility is purposely not available in the
	 * {@link ProvRegistryManager provenance registry manager}.
	 * 
	 */
	public int purgeAll ( Date from, Date to )
	{
		List<ProvenanceRegisterEntry> removedEntries = this.find ( null, null, from, to, null );
		if ( removedEntries == null ) return 0;
		
		for ( ProvenanceRegisterEntry prove: removedEntries )
			this.entityManager.remove ( prove );
		
		return removedEntries.size ();
	}
}
