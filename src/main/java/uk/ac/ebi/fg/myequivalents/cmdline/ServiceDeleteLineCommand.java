/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.out;
import static java.lang.System.err;


import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.ServiceManager;

/**
 * The 'service delete' command, a wrapper for {@link ServiceManager#deleteServices(String...)}.
 *
 * <dl><dt>date</dt><dd>Jul 31, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceDeleteLineCommand extends LineCommand
{
	public ServiceDeleteLineCommand () {
		super ( "service delete" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		ServiceManager servMgr = new ServiceManager ();

		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 )
			servMgr.deleteServices ( (String[]) ArrayUtils.subarray ( args, 2, args.length ) );
		
		err.println ( "\nServices Deleted" );
		return;
	}


	@Override
	public void printUsage ()
	{
		err.println ( "\n service delete name..." );
		err.println (   "   Deletes services, identified by their symbolic names" );
		err.println (   "   Will generate an error if any of the service is being referred by some entity." );
	}	

}
