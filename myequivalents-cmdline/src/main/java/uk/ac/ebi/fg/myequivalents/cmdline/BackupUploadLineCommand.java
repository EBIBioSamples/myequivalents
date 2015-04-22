package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * 
 * The implementation of {@link BackupManager#upload(InputStream)} as command line.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Mar 2015</dd>
 *
 */
public class BackupUploadLineCommand extends LineCommand
{
	private String inFilePath = null;
	
	public BackupUploadLineCommand ()
	{
		super ( "backup upload" );
	}
	
	@Override
	public void run ( String... args )
	{
		try
		{
			super.run ( args );
			if ( this.exitCode != 0 ) return;
			
			InputStream in = inFilePath == null ? System.in : new FileInputStream ( inFilePath );
			
			BackupManager bkpMgr = 
				Resources.getInstance ().getMyEqManagerFactory ().newBackupManager ( this.email, this.apiPassword );
			
			int result = bkpMgr.upload ( in );
			err.printf ( "\nUpload finished, %d item(s) uploaded\n", result );
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
		err.println ( "\n backup upload [dump xml file]" );
		err.println (   "   Uploads dump data to the current myEquivalents store" );
		err.println (   "   Reads from the standard input, if not input file is specified." );
	}

}
