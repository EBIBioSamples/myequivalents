package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * 
 * The implementation of {@link BackupManager#dump(OutputStream, Integer, Integer)} as command line.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Mar 2015</dd>
 *
 */
public class BackupDumpLineCommand extends LineCommand
{
	private String outFilePath = null;
	
	public BackupDumpLineCommand ()
	{
		super ( "backup dump" );
	}
	
	@Override
	public void run ( String... args )
	{
		try
		{
			super.run ( args );
			if ( this.exitCode != 0 ) return;
			
			OutputStream out = outFilePath == null ? System.out : new FileOutputStream ( outFilePath );
			
			Integer offset = Integer.valueOf ( cmdLine.getOptionValue ( "offset", "-1" ) );
			if ( offset == -1 ) offset = null;

			Integer limit = Integer.valueOf ( cmdLine.getOptionValue ( "limit", "-1" ) );
			if ( limit == -1 ) limit = null;
			
			BackupManager bkpMgr = 
				Resources.getInstance ().getMyEqManagerFactory ().newBackupManager ( this.email, this.apiPassword );
			
			int result = bkpMgr.dump ( out, offset, limit );
			err.printf ( "\nDump finished, %d item(s) dumped\n", result );
		}
		catch ( FileNotFoundException ex )
		{
			throw new RuntimeException ( "Error file: '" + outFilePath + "' not found", ex );
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
		if ( args.length > 2 ) outFilePath = args [ 2 ];
		return cmdLine;
	}
	
	
	@Override
	@SuppressWarnings ( "static-access" )
	protected Options getOptions ()
	{
		return super.getOptions ()
		.addOption ( OptionBuilder
		 	.withDescription ( "The item to start from"	)
			.withLongOpt ( "offset" )
			.hasArg ( true )
			.withArgName ( "0-n" )
			.create ( 'o' ) 
		)
		.addOption ( OptionBuilder
		 	.withDescription ( "How many items to dump"	)
			.withLongOpt ( "limit" )
			.hasArg ( true )
			.withArgName ( "0-n" )
			.create ( 'l' ) 
		);
	}

	@Override
	public void printUsage ()
	{
		err.println ( "\n backup dump [xml file]" );
		err.println (   "   Dumps all the data from the current myEquivalents store" );
		err.println (   "   Output the XML results into an XML file, or, if nothing is specified, on the standard output." );
	}

}
