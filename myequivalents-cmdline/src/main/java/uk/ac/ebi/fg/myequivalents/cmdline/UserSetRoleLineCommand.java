/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * The 'user set role' command, a wrapper for 
 * {@link AccessControlManager#setUserRole(String, uk.ac.ebi.fg.myequivalents.access_control.model.User.Role)}.
 *
 * <dl><dt>date</dt><dd>Aug 22, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UserSetRoleLineCommand extends LineCommand
{
	public UserSetRoleLineCommand () {
		super ( "user set role" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		AccessControlManager accMgr =
			Resources.getInstance ().getMyEqManagerFactory ().newAccessControlManagerFullAuth ( this.email, this.userPassword );

		args = cmdLine.getArgs ();
		if ( args != null && args.length >= 5 ) accMgr.setUserRole ( args [ 3 ], User.Role.valueOf ( args [ 4 ].toUpperCase () ) );
		
		err.println ( "\nUser's role Updated" );
		return;
	}


	@Override
	public void printUsage ()
	{
		String rstr = "", sep = "";
		for ( User.Role r: User.Role.values () ) {
			rstr += sep + r.toString ().toLowerCase ();
			sep = "|";
		}
		
		err.println ( "\n user set role email <" + rstr + ">" );
		err.println (   "   Changes the user role (requires admin powers)" );
	}	

}
