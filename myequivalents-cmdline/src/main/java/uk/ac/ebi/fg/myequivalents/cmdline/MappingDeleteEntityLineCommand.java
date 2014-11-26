package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import org.apache.commons.lang3.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * The line command for {@link EntityMappingManager#deleteEntities(String...)}.
 *
 * <dl><dt>date</dt><dd>Aug 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MappingDeleteEntityLineCommand extends LineCommand
{
	public MappingDeleteEntityLineCommand ()
	{
		super ( "mapping delete-entity" );
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
