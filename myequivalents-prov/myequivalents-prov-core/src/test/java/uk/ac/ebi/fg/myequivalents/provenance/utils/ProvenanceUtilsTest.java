package uk.ac.ebi.fg.myequivalents.provenance.utils;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.*;
import static uk.ac.ebi.fg.myequivalents.provenance.utils.ProvenanceUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>12 Sep 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceUtilsTest
{
	@Test
	public void testBasics ()
	{
		List<ProvenanceRegisterEntry> provs = new ArrayList<> ();
		
		ProvenanceRegisterEntry e = new ProvenanceRegisterEntry ( 
			"foo.user", "foo.op", p ( "foo.entity", Arrays.asList ( "acc1", "acc2", "acc3" ) )
		);
		provs.add ( e );
		
		ProvenanceRegisterEntry e1 = new ProvenanceRegisterEntry ( 
			"foo.user", "foo.op", pent ( Arrays.asList ( "s1:acc4", "s2:acc5", "s3:acc6" ) )
		);
		provs.add ( e1 );
		
		ProvenanceRegisterEntry e2 = new ProvenanceRegisterEntry ( 
			"test.user", "test.op", pent ( Arrays.asList ( "s4:acc7", "s2:acc5" ) )
		);
		provs.add ( e2 );

		assertEquals ( "User not found!", 2, getUserEmails ( provs, "^foo\\..*" ).size () );
		assertEquals ( "Operations not found!", 1, getOperations ( provs, "foo.op" ).size () );
		assertEquals ( "Operations not found!", 2, getOperations ( provs, null ).size () );
		assertEquals ( "Entities not found!",  4, getAllEntityIds ( provs ).size () );
	}
	
}
