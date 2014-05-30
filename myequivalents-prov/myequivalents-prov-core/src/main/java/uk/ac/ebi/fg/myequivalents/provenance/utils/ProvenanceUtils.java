package uk.ac.ebi.fg.myequivalents.provenance.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry.Operation;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>1 Apr 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceUtils
{
	public static String getOperationString ( String operation, Describeable ...parameters )
	{
		String[] names = new String [ parameters.length ];
		int i = 0;
		for ( Describeable p: parameters )
			names [ i++ ] = p.getName ();
		return getOperationString ( operation, names );
	}

	public static String getOperationString ( String operation, String ...parameters )
	{
		StringBuilder topOp = new StringBuilder ( operation + " (" );
		String sep = " ";
		for ( String p: parameters )
			topOp.append ( sep ).append ( p );
		if ( sep.length () != 0 ) topOp.append ( " " );
		
		topOp.append ( ")" );
		return topOp.toString ();
	}
}
