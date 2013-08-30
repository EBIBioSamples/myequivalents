/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * The 'user get' command, a wrapper for {@link AccessControlManager#getUserAs(String, String)}.
 *
 * <dl><dt>date</dt><dd>Jul 31, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UserGetLineCommand extends LineCommand
{
	public UserGetLineCommand () {
		super ( "user get" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		AccessControlManager accMgr =
			Resources.getInstance ().getMyEqManagerFactory ().newAccessControlManager ( this.email, this.apiPassword );

		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 )
			System.out.print ( accMgr.getUserAs ( this.outputFormat, args [ 2 ] ) );
		
		err.println ( "\nUser Fetched" );
		return;
	}


	@Override
	public void printUsage ()
	{
		err.println ( "\n user get email" );
		err.println (   "   Gets user details" );
	}	

}
