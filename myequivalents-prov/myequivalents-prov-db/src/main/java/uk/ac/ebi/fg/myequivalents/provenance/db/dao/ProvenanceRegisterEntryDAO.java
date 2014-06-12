package uk.ac.ebi.fg.myequivalents.provenance.db.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import org.apache.commons.lang3.Validate;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.transform.AliasToBeanResultTransformer;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegistryParameter;

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
	public List<ProvenanceRegisterEntry> find ( String userEmail, String operation, List<String> parameterPairs )
	{
		Session sess = (Session) this.entityManager.getDelegate ();
		DetachedCriteria crit = DetachedCriteria.forClass ( ProvenanceRegisterEntry.class, "prove" );
		//crit.setResultTransformer ( crit.DISTINCT_ROOT_ENTITY );
		crit.setProjection ( Projections.distinct ( Projections.id () ) );
		
		if ( userEmail != null )  crit.add ( Restrictions.like ( "userEmail", userEmail ) );
		if ( operation != null )  crit.add ( Restrictions.like ( "operation", operation ) );

		if ( parameterPairs != null && !parameterPairs.isEmpty () )
		{
			if ( parameterPairs.size () % 2 != 0 ) throw new RuntimeException ( 
				"Internal error: cannot search over provenance register with an uneven number of string pairs" 
			);
			
			Disjunction paramCrit = Restrictions.disjunction ();
			
			for ( Iterator<String> itr = parameterPairs.iterator (); itr.hasNext ();  )
			{
				String ptype = itr.next (), pval = itr.next ();
				if ( ptype == null ) { if ( pval == null ) continue; else ptype = "%"; }
				else if ( pval == null ) pval = "%";
						
				paramCrit.add ( Restrictions.and ( 
					Restrictions.like ( "param.valueType", ptype ), 
					Restrictions.like ( "param.value", pval ) 
				));
			}

			crit.createAlias ( "prove.parameters", "param" );
			crit.add ( paramCrit );
		}
		
		return sess.createCriteria ( ProvenanceRegisterEntry.class )
			.add ( Property.forName ( "id" ).in ( crit ) )
			.list ();
	}
}
