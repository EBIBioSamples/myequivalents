package uk.ac.ebi.fg.myequivalents.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>9 Sep 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ManagerUtils
{
	public static String checkOutputFormat ( String outputFormat )
	{
		outputFormat = StringUtils.trimToNull ( outputFormat );
		
		if ( !"xml".equalsIgnoreCase ( outputFormat ) ) throw new IllegalArgumentException ( 
			"Unsopported output format '" + outputFormat + "'" 
		);
		
		return outputFormat.toLowerCase ();
	}
}
