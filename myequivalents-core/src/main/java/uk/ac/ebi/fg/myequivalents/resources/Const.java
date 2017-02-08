package uk.ac.ebi.fg.myequivalents.resources;

import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;

/**
 * 
 * Some commonly used constants.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Aug 2015</dd>
 *
 */
public class Const
{
	/**
	 * Prefix that should be used for every Java property about myequivalents. 
	 */
	public static final String PROP_PREFIX = "myequivalents.";
	
	/**
	 * Cached objects (e.g. Service instances fetched by {@link EntityIdResolver}) are cancelled and reloaded after
	 * this amount of time has passed after they were first loaded in memory. Default is 60 mins. 
	 * 
	 * TODO: Document this in the wiki.
	 */
	public static final String PROP_NAME_CACHE_TIMEOUT_MIN = PROP_PREFIX + "cache_timeout";
	
	/**
	 * A few components (e.g., unit tests) use this to know we're in test mode and they should behave accordingly
	 * (e.g., don't shutdown the JVM while testing a command line tool).
	 */
	public static final String PROP_NAME_TEST_FLAG = PROP_PREFIX + "test_flag";
		
	/** Length used for database character columns, e.g., accession, contact's mid title, version */
	public static final int COL_LENGTH_S = 50;

	/** Length used for database character columns, e.g., name, surname */
	public static final int COL_LENGTH_M = 100;

	/** Length used for database character columns, e.g., title */
	public static final int COL_LENGTH_L = 300;

	/** Length used for database character columns, e.g., address, citation, (short) description */
	public static final int COL_LENGTH_XL = 1000;

	/** Length used for database character columns, e.g., (medium) description */
	public static final int COL_LENGTH_XXL = 4000;

	
	/** Length used for URIs and the like */
	public static final int COL_LENGTH_URIS = 2000;

}
