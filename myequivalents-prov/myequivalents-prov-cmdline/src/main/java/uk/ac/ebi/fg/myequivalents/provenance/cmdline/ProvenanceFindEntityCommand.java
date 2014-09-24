package uk.ac.ebi.fg.myequivalents.provenance.cmdline;

import static java.lang.System.err;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.cmdline.LineCommand;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;



/**
 * TODO: Comment me!
 *
 * {@code provenance find-entity <entityId> [<user-email>...]}
 *
 * <dl><dt>date</dt><dd>2 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceFindEntityCommand extends LineCommand
{
	
	public ProvenanceFindEntityCommand ()
	{
		super ( "provenance find-entity" );
	}

	
	
	@Override
	public void run ( String ... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;
		
		args = this.cmdLine.getArgs ();
		if ( args == null || args.length < 3 )
		{
			this.exitCode = 1; // TODO: proper code
			throw new RuntimeException ( "Must specify an entity-id" );
		}
		String entityId = args [ 2 ];
		
		List<String> validUsers = null;
		if ( args.length > 3 )
			// Eliminates: 'provenance find-mapping entityId'
			validUsers = new ArrayList<> ( Arrays.asList ( ArrayUtils.subarray ( args, 3, args.length ) ) );
		
		ProvManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( this.email, this.apiPassword );

		System.out.println ( regMgr.findEntityMappingProvAs ( this.outputFormat, entityId, validUsers ) );
		err.println ( "\nProvenance info Fetched" );
	}



	@Override
	protected void printUsage ()
	{
		err.println ( "\n provenance find-entity <entityId> [<user-email>...]" );
		err.println (   "   Finds all the mapping operations about entity-id, which contributed to define the mappings entityId belongs to." );
		err.println (   "   If user emails are specified, it returns only entries created by the specified users" );
	}

}
