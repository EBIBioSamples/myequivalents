/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import javax.management.RuntimeErrorException;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

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
			
			doVisibilitySet ( publicFlagStr, relDateStr, cascade, (String[]) ArrayUtils.subarray ( args, 2, args.length ) );
		}
		
		err.println ( "\nVisibility command executed." );
		return;
	}

	protected abstract void doVisibilitySet ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceNames );

	@Override
	public void printUsage ()
	{
		err.println ( "\n service set visibility service-name..." );
		err.println (   "   Set the service visibility." ); 
	}

	@Override
	protected Options getOptions ()
	{
		// TODO Auto-generated method stub
		return super.getOptions ();
	}	

	
}
