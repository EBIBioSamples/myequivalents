package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbEntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;

/**
 * The 'mapping store' command. This will use {@link DbEntityMappingManager#storeMappings(String...)}.
 *
 * <dl><dt>date</dt><dd>Aug 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MappingGetCommandLineCommand extends LineCommand
{
	public MappingGetCommandLineCommand ()
	{
		super ( "mapping get" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		EntityMappingManager emMgr = new DbEntityMappingManager ();
		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 ) 
		{
			String fmtTag = cmdLine.getOptionValue ( "format", "xml" );
			boolean wantRawResult = cmdLine.hasOption ( "raw" );
			System.out.print ( 
				emMgr.getMappingsAs ( fmtTag, wantRawResult, (String[]) ArrayUtils.subarray ( args, 2, args.length ) )
			);
		}
		
		err.println ( "\nMapping(s) Fetched" );
		return;
	}

	@Override
	public void printUsage ()
	{
		err.println ( "\n mapping get <service:accession|uri>..." );
		err.println (   "   Fetches entity mappings" );
	}

}
