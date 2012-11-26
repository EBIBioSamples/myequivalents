/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;


import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;

/**
 * 
 * The 'repository delete' command, a wrapper for {@link DbServiceManager#deleteRepositories(String...)}.
 *
 * <dl><dt>date</dt><dd>Aug 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class RepositoryDeleteLineCommand extends LineCommand
{
	public RepositoryDeleteLineCommand () {
		super ( "repository delete" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		ServiceManager servMgr = new DbManagerFactory ().newServiceManager ();

		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 )
			servMgr.deleteRepositories ( (String[]) ArrayUtils.subarray ( args, 2, args.length ) );
		
		err.println ( "\nRepository(ies) Deleted" );
		return;
	}


	@Override
	public void printUsage ()
	{
		err.println ( "\n repository delete name..." );
		err.println (   "   Deletes repositories, identified by their symbolic names" );
		err.println (   "   Will generate an error if any of the repository is being referred by some other entity." );
	}	

}
