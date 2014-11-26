/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * The line command for {@link AccessControlManager#deleteUser(String)}.
 *
 * <dl><dt>date</dt><dd>Jul 31, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UserDeleteLineCommand extends LineCommand
{
	public UserDeleteLineCommand () {
		super ( "user delete" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		AccessControlManager accMgr =
			Resources.getInstance ().getMyEqManagerFactory ().newAccessControlManagerFullAuth ( this.email, this.userPassword );

		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 ) accMgr.deleteUser ( args [ 2 ] );
		
		err.println ( "\nUser Removed" );
		return;
	}


	@Override
	public void printUsage ()
	{
		err.println ( "\n user delete email" );
		err.println (   "   Removes the given user (requires admin powers)" );
	}	

}
