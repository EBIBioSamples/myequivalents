/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.ArrayUtils;

/**
 * The 'service delete' command, a wrapper for {@link DbServiceManager#deleteServices(String...)}.
 *
 * <dl><dt>date</dt><dd>Jul 31, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class AbstractVisibilitySetLineCommand extends LineCommand
{
	public AbstractVisibilitySetLineCommand ( String commandString )
	{
		super ( commandString );
	}

	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		args = cmdLine.getArgs ();
		if ( args != null && args.length > 3 )
		{
			String publicFlagStr = cmdLine.getOptionValue ( 'p' );
			String relDateStr = cmdLine.getOptionValue ( 'd' );
			boolean cascade = cmdLine.hasOption ( 'x' );
			
			doVisibilitySet ( publicFlagStr, relDateStr, cascade, (String[]) ArrayUtils.subarray ( args, 3, args.length ) );
		}
		
		err.println ( "\nVisibility command executed." );
		return;
	}

	protected abstract void doVisibilitySet ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... entityNames );

	@Override
	public void printUsage ()
	{
		String typeStr = this.commandString.split ( " " ) [ 0 ];
		String typeStrUI = "entity".equals ( typeStr ) ? "<service:accession|uri>" : "name";
		
		err.format ( "\n %s %s...\n", this.commandString, typeStrUI );
		err.format (   "   Set %s visibility.\n", typeStr ); 
	}
}
