package uk.ac.ebi.fg.myequivalents.provenance.interfaces;

import java.util.Date;
import java.util.List;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.MyEquivalentsManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;

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
		String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params 
	);

	public String findAs (
		String outputFormat, String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params 
	);
	
	public int purge ( Date from, Date to );
}
