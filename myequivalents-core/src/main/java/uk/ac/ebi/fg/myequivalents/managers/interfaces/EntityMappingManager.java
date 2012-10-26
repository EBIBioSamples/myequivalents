/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * <h2>The Entity Mapping Manager Interface</h2>
 * 
 * This is the interface used to manage mappings between entities. Note that, as explained in package.html, for the moment 
 * this is the only manager that is split into interface + multiple implementations, we will migrate into this same
 * architecture in the near future.
 *
 * <dl><dt>date</dt><dd>Oct 1, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface EntityMappingManager
{

	/**
	 * Stores mappings between entities. The parameter consists of a list of quadruples, where every quadruple is a pair
	 * of service/accession. This uses {@link EntityMappingManager#storeMappings(String...)} (see there for details) 
	 * and wraps it with the transaction management. You'll get an exception if any of the named services doesn't exist.
	 */
	public void storeMappings ( String ... entityIds );

	/**
	 * Stores a mapping bundle. The parameter consists of a list of entity references, where every entity is given by a pair
	 * of service/accession. This uses {@link EntityMappingManager#storeMappingBundle(String...)} (see there for details) 
	 * and wraps it with the transaction management. You'll get an exception if any of the named services doesn't exist.
	 */
	public void storeMappingBundle ( String ... entityIds );

	/**
	 * Deletes mappings between entities. The parameter consists of a list of quadruples, where every quadruple is a pair
	 * of service/accession. This uses {@link EntityMappingManager#storeMappings(String...)} (see there for details) and
	 * wraps it with the transaction management.
	 */
	public int deleteMappings ( String ... entityIds );

	/**
	 * Deletes entities. The parameter consists of a list of entity references, where every entity is given by a pair
	 * of service/accession. The entities are removed from any mapping it belonged to (i.e., they disappear altogether). 
	 * This uses {@link EntityMappingManager#deleteEntities(String...)} (see there for details) 
	 * and wraps it with the transaction management. 
	 */
	public int deleteEntities ( String ... entityIds );

	/**
	 * Gets all the mappings to which the parameter entities are associated. 
	 * The parameter consists of a list of entity references, where every entity is given by a pair of service/accession. 
	 *  
	 * @param wantRawResult if true, omits service-related objects from the result ({@link Service}, {@link ServiceCollection}, 
	 * {@link Repository}), it only reports bundles of entity mappings. This will be faster if you just needs links.
	 * 
	 * @return An instance of {@link EntityMappingSearchResult}, containing the resulting mappings, which can be accessed 
	 * through methods like {@link EntityMappingSearchResult#getBundles()}. The results <b>does include the method parameters</b>.
	 * It returns an empty list if either parameter is empty. It never returns null.	 
	 */
	public EntityMappingSearchResult getMappings ( Boolean wantRawResult, String ... entityIds );

	/**
	 * Returns the result of {@link #getMappings(boolean, String...)} in the specified format. At the moment this is 
	 * only 'xml' and {@link #getRepositoriesAsXml(String...)} is used for this. We plan formats like RDF or JSON for 
	 * the future.
	 */
	public String getMappingsAs ( String outputFormat, Boolean wantRawResult, String ... entityIds );

}
