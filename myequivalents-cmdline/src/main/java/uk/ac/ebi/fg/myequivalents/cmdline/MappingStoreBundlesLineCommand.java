package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.cli.CommandLine;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * Implements {@link EntityMappingManager#storeMappingBundlesFromXML(Reader)} as a command line.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Mar 2015</dd>
 *
 */
public class MappingStoreBundlesLineCommand extends LineCommand
{
	private String inFilePath = null;
	
	public MappingStoreBundlesLineCommand ()
	{
		super ( "mapping store-bundles" );
	}
	
	@Override
	public void run ( String... args )
	{
		try
		{
			super.run ( args );
			if ( this.exitCode != 0 ) return;
			
			Reader in = inFilePath == null ? new InputStreamReader ( System.in ) : new FileReader ( inFilePath );
			
			EntityMappingManager mapMgr = 
				Resources.getInstance ().getMyEqManagerFactory ().newEntityMappingManager ( this.email, this.apiPassword );
			
			mapMgr.storeMappingBundlesFromXML ( in );
			err.println ( "\nUpload finished" );
		}
		catch ( FileNotFoundException ex )
		{
			throw new RuntimeException ( "Error file: '" + inFilePath + "' not found", ex );
		}
	}

	/**
	 * Parses and then setup the input file (or the standard output).
	 */
	@Override
	protected CommandLine parse ( String... args )
	{
		if ( super.parse ( args ) == null ) return null;
		args = this.cmdLine.getArgs (); 
		if ( args.length > 2 ) inFilePath = args [ 2 ];
		return cmdLine;
	}
	

	@Override
	public void printUsage ()
	{
		err.println ( "\n mapping store-bundles [xml file]" );
		err.println (   "   Stores a whole set of bundles" );
		err.println (   "   The input format is like the result given by 'mapping get' (). ." );
	}

}
