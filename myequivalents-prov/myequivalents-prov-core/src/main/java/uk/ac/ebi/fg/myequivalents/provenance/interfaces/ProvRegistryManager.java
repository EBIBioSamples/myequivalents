package uk.ac.ebi.fg.myequivalents.provenance.interfaces;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.MyEquivalentsManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>1 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface ProvRegistryManager extends MyEquivalentsManager
{
	public List<ProvenanceRegisterEntry> find (
		String userEmail, String operation, Date from, Date to, List<String> parameterPairs 
	);
	
	public int purge ( Date from, Date to );
}
