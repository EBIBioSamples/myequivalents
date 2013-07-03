/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * The 'service get' command, a wrapper for {@link DbServiceManager#getServicesAs(String, String...)}.
 *
 * <dl><dt>date</dt><dd>Jul 31, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceGetLineCommand extends LineCommand
{
	public ServiceGetLineCommand () {
		super ( "service get" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		ServiceManager servMgr =
			Resources.getInstance ().getMyEqManagerFactory ().newServiceManager ( this.email, this.apiPassword );

		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 )
		{
			String fmtTag = cmdLine.getOptionValue ( "format", "xml" );
			System.out.print ( servMgr.getServicesAs ( fmtTag, (String[]) ArrayUtils.subarray ( args, 2, args.length ) ) );
		}
		
		err.println ( "\nService(s) Fetched" );
		return;
	}


	@Override
	public void printUsage ()
	{
		err.println ( "\n service get name..." );
		err.println (   "   Gets service info, services identified by name" );
	}	

}
