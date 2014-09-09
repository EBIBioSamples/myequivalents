package uk.ac.ebi.fg.myequivalents.provenance.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Set;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.MyEquivalentsManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;

/**
 * Facade functionality to access the provenance registry and enquire about provenance operations occurred in myEq.
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
	
	
	public List<ProvenanceRegisterEntry> findEntityMappingProv ( String entityId, List<String> validUsers );

	public String findEntityMappingProvAs ( String outputFormat, String entityId, List<String> validUsers );

	
	
	public Set<List<ProvenanceRegisterEntry>> findMappingProv ( String xEntityId, String yEntityId, List<String> validUsers );

	public String findMappingProvAs ( String outputFormat, String xEntityId, String yEntityId, List<String> validUsers );
	
	
	public int purge ( Date from, Date to );
}
