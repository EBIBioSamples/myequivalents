package uk.ac.ebi.fg.myequivalents.provenance.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;


/**
 * Various utility functions about the data provenance in myEquivalents.
 * 
 * <dl><dt>date</dt><dd>1 Apr 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceUtils
{
	/**
	 * All the user emails in this collection of entries that match the regular expression emailRe (all of them if that's null).
	 * Results are added to {@code result} and returned. This variable is initialised with an empty set, if null.
	 */
	public static Set<String> getUserEmails ( Collection<ProvenanceRegisterEntry> provEntries, String emailRe, Set<String> result )
	{
		if ( result == null ) result = new HashSet<> ();
		if ( provEntries == null || provEntries.isEmpty () ) return result;
		
		for ( ProvenanceRegisterEntry prove: provEntries )
		{
			String email = StringUtils.trimToNull ( prove.getUserEmail () );
			if ( emailRe == null || email != null && email.matches ( emailRe ) ) 
				result.add ( email );
		}
		
		return result;
	}
	
	/**
	 * Wraps {@link #getUserEmails(Collection, String, Set)} with {@code result = null}. 
	 */
	public static Set<String> getUserEmails ( Collection<ProvenanceRegisterEntry> provEntries, String emailRe )
	{
		return getUserEmails ( provEntries, null, null );
	}

	
	/**
	 * All the operations in this collection of entries that match opRe (all of them if that's null).
	 * Results are added to {@code result} and returned. This variable is initialised with an empty set, if null.
	 */
	public static Set<String> getOperations ( Collection<ProvenanceRegisterEntry> provEntries, String opRe, Set<String> result )
	{
		if ( result == null ) result = new HashSet<> ();
		if ( provEntries == null || provEntries.isEmpty () ) return result;
		
		for ( ProvenanceRegisterEntry prove: provEntries )
		{
			String op = StringUtils.trimToNull ( prove.getOperation () );
			if ( opRe == null || op != null && op.matches ( opRe ) ) result.add ( op );
		}
		
		return result;
	}
	
	/**
	 * Wraps {@link #getOperations(Collection, String, Set)} with {@code result = null}. 
	 */
	public static Set<String> getOperations ( Collection<ProvenanceRegisterEntry> provEntries, String opRe )
	{
		return getOperations ( provEntries, opRe, null );
	}

	
	/**
	 * All the entityIds in this collection of entries It works like 
	 * {@link #getParamValues(Collection, String, Set) getParamValues(params, "^entity$", result)}.
	 */
	public static Set<String> getEntityIds ( Collection<ProvenanceRegisterParameter> params, Set<String> result )
	{
		return getParamValues ( params, "^entity$", result );
	}

	/**
	 * Wraps {@link #getParamValues(Collection, String, Set)} with {@code result = null} 
	 */
	public static Set<String> getEntityIds ( Collection<ProvenanceRegisterParameter> params )
	{
		return getEntityIds ( params, null );
	}

	
	/**
	 * {@link #getAllEntityIds(Collection, Set)} with {@code result = null}
	 */
	public static Set<String> getAllEntityIds ( Collection<ProvenanceRegisterEntry> provEntries )
	{
		return getAllEntityIds ( provEntries, null );
	}
	
	/**
	 * Calls {@link #getAllParamValues(Collection, String, Set)} with {@code paramTypeRe = "^entity$"} 
	 */
	public static Set<String> getAllEntityIds ( Collection<ProvenanceRegisterEntry> provEntries, Set<String> result )
	{
		return getAllParamValues ( provEntries, "^entity$", result );
	}


	
	
	/**
	 * Calls {@link #getParamValues(Collection, String, Set)} with all the parameters in the {@code provEntries}.
	 */
	public static Set<String> getAllParamValues ( Collection<ProvenanceRegisterEntry> provEntries, String paramTypeRe, Set<String> result )
	{
		if ( result == null ) result = new HashSet<> ();
		if ( provEntries == null || provEntries.isEmpty () ) return result;
		
		for ( ProvenanceRegisterEntry prove: provEntries )
			getParamValues ( prove.getParameters (), paramTypeRe, result );
		
		return result;
	}

	/**
	 * Calls {@link #getAllParamValues(Collection, String, Set)} with {@code result = null}. 
	 */
	public static Set<String> getAllParamValues ( Collection<ProvenanceRegisterEntry> provEntries, String paramTypeRe )
	{
		return getAllParamValues ( provEntries, paramTypeRe, null );
	}

	
	
	/**
	 * All the parameter values in params, which of type match {@code paramTypeRe}. if a parameter has an 
	 * {@link ProvenanceRegisterParameter#getExtraValue() extra value}, that's added to the main value, using a two-colon
	 * as separator, ie, it's composed like an {@link Entity entity's id}
	 */
	public static Set<String> getParamValues ( Collection<ProvenanceRegisterParameter> params, String paramTypeRe, Set<String> result )
	{
		if ( result == null ) result = new HashSet<> ();
		if ( params == null || params.isEmpty () ) return result;
		
		for ( ProvenanceRegisterParameter param: params )
		{
			String vtype = param.getValueType ();
			if ( ! ( paramTypeRe == null || vtype != null && vtype.matches ( paramTypeRe ) ) ) continue;
			
			String val = param.getValue ();
			String val1 = param.getExtraValue ();
			if ( val1 != null ) val += ":" + val1;
			result.add ( val );
		}
		
		return result;
	}
	
	/**
	 * Wraps {@link #getParamValues(Collection, String, Set)} with {@code result = null}
	 */
	public static Set<String> getParamValues ( Collection<ProvenanceRegisterParameter> params, String paramTypeRe )
	{
		return getParamValues ( params, paramTypeRe, null );
	}

}
