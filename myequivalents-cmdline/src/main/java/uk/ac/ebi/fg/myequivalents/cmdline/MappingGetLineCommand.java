package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;
import static java.lang.System.out;

import org.apache.commons.lang3.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * The 'mapping store' command. This will use {@link EntityMappingManager#getMappingsAs(String, Boolean, String...)}.
 *
 * <dl><dt>date</dt><dd>Aug 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MappingGetLineCommand extends LineCommand
{
	public MappingGetLineCommand ()
	{
		super ( "mapping get" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		EntityMappingManager emMgr =
			Resources.getInstance ().getMyEqManagerFactory ().newEntityMappingManager ( this.email, this.apiPassword );
		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 ) 
		{
			boolean wantRawResult = cmdLine.hasOption ( "raw" );
			out.print ( 
				emMgr.getMappingsAs ( this.outputFormat, wantRawResult, (String[]) ArrayUtils.subarray ( args, 2, args.length ) )
			);
		}
		
		err.println ( "\nMapping(s) Fetched" );
	}

	@Override
	public void printUsage ()
	{
		err.println ( "\n mapping get <service:accession|uri>..." );
		err.println (   "   Fetches entity mappings" );
	}

}
