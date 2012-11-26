package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbEntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;

/**
 * The 'mapping store' command. This will use {@link DbEntityMappingManager#deleteEntities(String...)}.
 *
 * <dl><dt>date</dt><dd>Aug 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MappingDeleteEntityCommandLineCommand extends LineCommand
{
	public MappingDeleteEntityCommandLineCommand ()
	{
		super ( "mapping delete-entity" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		EntityMappingManager emMgr = new DbEntityMappingManager ();
		args = cmdLine.getArgs ();
		if ( args != null && args.length > 2 )
			emMgr.deleteEntities ( (String[]) ArrayUtils.subarray ( args, 2, args.length ) );
		
		err.println ( "\nEntity(ies) Deleted" );
		return;
	}

	@Override
	public void printUsage ()
	{
		err.println ( "\n mapping delete-entity <service:accession|uri>..." );
		err.println (   "   Deletes entities from any mapping they are involved in, entities have to be listed as pairs of identifiers" );
	}

}
