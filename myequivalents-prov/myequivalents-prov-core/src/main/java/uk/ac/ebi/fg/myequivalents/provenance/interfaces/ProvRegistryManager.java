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
	/**
		* Finds {@link ProvenanceRegisterEntry provenance entries} matching the user and parameters, in a date range. 
		* You can use the '%' wildcard, as in SQL. You should return an empty list in place of null.
		*/
	public List<ProvenanceRegisterEntry> find (
		String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params 
	);

	/**
	 * Invokes {@link #find(String, String, Date, Date, List)} and format the result in a given format. XML is the only
	 * supported at the moment, via JAXB and {@link ProvRegisterEntryList}.
	 *  
	 */
	public String findAs (
		String outputFormat, String userEmail, String operation, Date from, Date to, List<ProvenanceRegisterParameter> params 
	);
	
	
	/**
	 * Returns {@link ProvenanceRegisterEntry provenance records} regarding entityId mapping definitions to other entities
	 * and performed by any user in validUsers. Doesn't filter based on the user if none is specified. 
	 * 
	 */
	public List<ProvenanceRegisterEntry> findEntityMappingProv ( String entityId, List<String> validUsers );

	/**
	 * Invokes {@link #findEntityMappingProv(String, List)} and format the result in a given format. XML is the only
	 * supported at the moment, via JAXB and {@link ProvRegisterEntryList}.
	 */
	public String findEntityMappingProvAs ( String outputFormat, String entityId, List<String> validUsers );

	
	/**
	 * Finds all the {@link ProvenanceRegisterEntry provenance records}, performed by any of the validUsers 
	 * (no filter applied if empty), which contributed to the creation of the link between xEntityId and yEntityId. 
	 * 
	 * This means that the method recursively call itself until it has reconstructed all the mapping operations that
	 * built the mapping bundle the two entities belong to.
	 * 
	 * Each list item in the resulting set contains a path of operations from xEntityId to yEntityId, which 
	 * are about the chain of entities transitively linking the two entities.
	 *    
	 */
	public Set<List<ProvenanceRegisterEntry>> findMappingProv ( String xEntityId, String yEntityId, List<String> validUsers );

	/**
	 * Invokes {@link #findMappingProv(String, String, List)} and format the result in a given format. XML is the only
	 * supported at the moment, via JAXB and {@link ProvRegisterEntryList}.
	 */
	public String findMappingProvAs ( String outputFormat, String xEntityId, String yEntityId, List<String> validUsers );
	
	/**
	 * Remove old provenance entries in a given date range. For each parameter found in the range, all the entries about
	 * such parameter are removed, except the most recent one. This allows one to keep the most updated provenance information
	 * about the entry, while the older one is removed.
	 */
	public int purge ( Date from, Date to );
	
}
