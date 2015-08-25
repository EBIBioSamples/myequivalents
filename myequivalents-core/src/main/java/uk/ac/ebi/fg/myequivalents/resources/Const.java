package uk.ac.ebi.fg.myequivalents.resources;

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
