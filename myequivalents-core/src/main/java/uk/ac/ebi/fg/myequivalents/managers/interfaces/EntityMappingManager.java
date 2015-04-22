package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.io.Reader;

import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.utils.EntityMappingUtils;

/**
 * <h2>The Entity Mapping Manager Interface</h2>
 * 
 * <p>This is the interface used to manage mappings between entities.</p>
 * 
 * <p>In general, you should assume that implementations of this interface are not thread-safe. The idea is that you 
 * create a new instance per thread, do some operations, release, all whitin the same thread.</p>
 *  
 * <dl><dt>date</dt><dd>Oct 1, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface EntityMappingManager extends MyEquivalentsManager
{

	/**
	 * Stores mappings between entities. The parameter consists of a list of pairs, where every pair contains two entity IDs.
	 * (see {@link EntityMappingUtils#parseEntityId(String)}). This call manages automatically the transitivity and symmetry 
	 * of the mapping (i.e., equivalence) relationship, which means if any of the two entities are already linked to other 
	 * entities, the latter are automatically linked to the other entity given to this call. It leaves the database unchanged 
	 * if the mapping already exists.  You'll get an exception if any of the named services doesn't exist.
	 */
	public void storeMappings ( String ... entityIds );

	/**
	 * Stores a mapping bundle. The parameter consists of a list of entity IDs (see {@link EntityMappingUtils#parseEntityId(String)}). 
	 * It also manages the symmetry and transitivity of the equivalence/mapping relationship, which means if any of the 
	 * entities passed as parameter are already linked to some other entities, the latter becomes part of the same 
	 * equivalence set too. It leaves the back-end storage unchanged if this exact mapping set already exists. 
	 * 
	 * You'll get an exception if any of the referred services doesn't exist.
	 */
	public void storeMappingBundle ( String ... entityIds );
	
	/**
	 * <p>Store a set of mapping bundles, saved inside an {@link EntityMappingSearchResult} object.
	 * This is intended to speed up things, when you have many bundles to store, including details
	 * like entity's release dates or public visibility. For instance, this is used to support 
	 * data dump/restore functions.</p> 
	 * 
	 * <p>You can store {@link Service services} and other non-entity items in the 'mappings' parameter. However, 
	 * the best performance is usually achieved when you store these objects once and then you call this method
	 * with the entities only.</p>
	 *  
	 * <p>WARNING: still to be tested (TODO).</p>
	 */
	public void storeMappingBundles ( EntityMappingSearchResult mappings );
	
	/**
	 * A version of {@link #storeMappingBundles(EntityMappingSearchResult)}, which reads bundle definitions from 
	 * an XML document.
	 */
	public void storeMappingBundlesFromXML ( Reader reader );

	/**
	 * Deletes mappings between entities. The parameter consists of a list of  
	 * {@link EntityMappingUtils#parseEntityId(String) entityIds}. This deletes all the mappings that involve an entity, 
	 * i.e., the whole equivalence class it belongs to.
   *
	 * @return the number of entities (including the parameter) that were in the same equivalence relationship and are
	 * now deleted. Returns 0 if no such mapping exists.
	 * 
	 */
	public int deleteMappings ( String ... entityIds );

	/**
	 * Deletes entities. The parameter consists of a list of entity references, where every entity is given by a pair
	 * of service/accession. The entities are removed from any mapping it belonged to (i.e., they disappear altogether). 
	 */
	public int deleteEntities ( String ... entityIds );
	
	/**
	 * Gets all the mappings to which the parameter entities are associated. 
	 * The parameter consists of a list of entity references, where every entity is given by a pair of service/accession, 
	 * as specified in {@link EntityMappingUtils#parseEntityId(String)}. 
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
	 * Tells where an entity is mapped onto a target service, if anywhere. This might be useful when you want to report
	 * only the links to a source entity in a target service (e.g., tell me if this resource is same-as some Wikipedia web
	 * page). Generally speaking, this query can return multiple results, cause the source entity could be mapped into multiple
	 * ones in the same target repository.
	 * 
	 * Parameters and result are like {@link #getMappings(Boolean, String...)}.
	 */
	public EntityMappingSearchResult getMappingsForTarget ( Boolean wantRawResult, String targetServiceName, String entityId );
	
	
	/**
	 * Returns the result of {@link #getMappings(boolean, String...)} in the specified format. At the moment this is 
	 * only 'xml' and the JAXB mapping of {@link EntityMappingSearchResult} is used for this. 
	 * 
	 * We plan formats like RDF or JSON for the future. See the documentation for more details.
	 * 
	 * <b>WARNING</b>: due to the sake of performance, the output <b>is not</b> guaranteed to be pretty-printed, i.e. having
	 * indentation and alike. Use proper tools for achieving that (e.g., <a href = 'http://tinyurl.com/nuue8ql'>xmllint</a>).
	 */
	public String getMappingsAs ( String outputFormat, Boolean wantRawResult, String ... entityIds );

	/**
	 * Returns the result of {@link #getMappingsForTarget(Boolean, String, String)}, wrapped into the specified format. At 
	 * the moment this is only 'xml' and the JAXB mapping of {@link EntityMappingSearchResult} is used for this. 
	 * 
	 * We plan formats like RDF or JSON for the future. See the documentation for more details.  
	 * 
	 * <b>WARNING</b>: due to the sake of performance, the output <b>is not</b> guaranteed to be pretty-printed, i.e. having
	 * indentation and alike. Use proper tools for achieving that (e.g., <a href = 'http://tinyurl.com/nuue8ql'>xmllint</a>).
	 */
	public String getMappingsForTargetAs ( String outputFormat, Boolean wantRawResult, String targetServiceName, String entityId );

	/**
	 * Does close/clean-up operations. There is no guarantee that a manager can be used after the invocation to this method.
	 * You may want to invoke this call in {@link Object#finalize()} in the implementation of this interface.
	 */
	public void close ();
}
