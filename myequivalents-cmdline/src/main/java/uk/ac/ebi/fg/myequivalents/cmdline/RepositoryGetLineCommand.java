/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * The 'repository get' command, a wrapper for {@link DbServiceManager#getRepositoriesAs(String, String...)}.
 *
 * <dl><dt>date</dt><dd>Aug 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class RepositoryGetLineCommand extends LineCommand
{
	public RepositoryGetLineCommand () {
		super ( "repository get" );
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
			System.out.print ( servMgr.getRepositoriesAs ( fmtTag, (String[]) ArrayUtils.subarray ( args, 2, args.length ) ) );
		}
		
		err.println ( "\nRepository(ies) Fetched" );
		return;
	}


	@Override
	public void printUsage ()
	{
		err.println ( "\n repository get name..." );
		err.println (   "   Gets repository info, repositories identified by name" );
	}	

}
