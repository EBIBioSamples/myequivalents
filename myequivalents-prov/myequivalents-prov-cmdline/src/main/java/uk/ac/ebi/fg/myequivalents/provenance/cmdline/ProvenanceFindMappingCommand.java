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
 * Implementation of {@link ProvRegistryManager#findMappingProvAs(String, String, String, List)}.
 *
 * {@code provenance find-mapping <entityId> <entityId> [<user-email>...]}
 *
 *
 * <dl><dt>date</dt><dd>2 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceFindMappingCommand extends LineCommand
{
	
	public ProvenanceFindMappingCommand ()
	{
		super ( "provenance find-mapping" );
	}

	
	
	@Override
	public void run ( String ... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;
		
		args = this.cmdLine.getArgs ();
		if ( args == null || args.length < 4 )
		{
			this.exitCode = 1; // TODO: proper code
			throw new RuntimeException ( "Must specify two entity-ids" );
		}
		String xEntityId = args [ 2 ], yEntityId = args [ 3 ];
		
		List<String> validUsers = null;
		if ( args.length > 4 )
			// Eliminates: 'provenance find-mapping x y'
			validUsers = new ArrayList<> ( Arrays.asList ( ArrayUtils.subarray ( args, 4, args.length ) ) );
		
		ProvManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( this.email, this.apiPassword );

		System.out.println ( regMgr.findMappingProvAs ( this.outputFormat, xEntityId, yEntityId, validUsers ) );
		err.println ( "\nProvenance info Fetched" );
	}



	@Override
	protected void printUsage ()
	{
		err.println ( "\n provenance find-entity <entityId> <entityId> [<user-email>...]" );
		err.println (   "   Finds all the mapping operations which contributed to define the mapping between the two entities" );
		err.println (   "   If user emails are specified, it returns only provenance entries created by the specified users" );
	}

}
