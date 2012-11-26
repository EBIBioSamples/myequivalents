package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;

/**
 * The 'mapping store' command. This will use {@link DbEntityMappingManager#storeMappings(String...)}.
 *
 * <dl><dt>date</dt><dd>Aug 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MappingStoreCommandLineCommand extends LineCommand
{
	public MappingStoreCommandLineCommand ()
	{
		super ( "mapping store" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		EntityMappingManager emMgr = new DbManagerFactory ().newEntityMappingManager ();
		
		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 )
			emMgr.storeMappings ( (String[]) ArrayUtils.subarray ( args, 2, args.length ) );
		
		err.println ( "\nMapping(s) Updated" );
		return;
	}

	@Override
	public void printUsage ()
	{
		err.println ( "\n mapping store <service:accession|uri>..." );
		err.println (   "   Creates mappings between entities, which have to be listed as pairs of identifiers" );
	}

}
