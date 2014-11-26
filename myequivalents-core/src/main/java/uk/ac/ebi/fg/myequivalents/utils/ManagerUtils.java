package uk.ac.ebi.fg.myequivalents.utils;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.MyEquivalentsManager;

/**
 * Miscellanea of utility functions for the {@link MyEquivalentsManager} package.
 *
 * <dl><dt>date</dt><dd>9 Sep 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ManagerUtils
{
	/**
	 * Simply checks that outputFormat is one of the currently supported myEquivalents formats. Which currently
	 * means 'xml'. Throws an exception in case not.
	 * 
	 */
	public static String checkOutputFormat ( String outputFormat )
	{
		outputFormat = StringUtils.trimToNull ( outputFormat );
		
		if ( !"xml".equalsIgnoreCase ( outputFormat ) ) throw new IllegalArgumentException ( 
			"Unsopported output format '" + outputFormat + "'" 
		);
		
		return outputFormat.toLowerCase ();
	}
}
