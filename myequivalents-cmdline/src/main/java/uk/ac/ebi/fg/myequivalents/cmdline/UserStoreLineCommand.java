/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.cli.CommandLine;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * The 'user store' command, a wrapper for {@link AccessControlManager#storeUserFromXml(Reader)}.
 *
 * <dl><dt>date</dt><dd>Aug 22, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UserStoreLineCommand extends LineCommand
{
	private String inFileName = null;

	public UserStoreLineCommand () {
		super ( "user store" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		File inFile = null; 
		try
		{
			Reader in = null;
			if ( inFileName == null )
				in = new InputStreamReader ( System.in );
			else 
			{
				inFile = new File ( inFileName );
				in = new FileReader ( inFile );
			}
		  in = new BufferedReader ( in );
			
			AccessControlManager accMgr =
				Resources.getInstance ().getMyEqManagerFactory ().newAccessControlManagerFullAuth ( this.email, this.userPassword );
			
			accMgr.storeUserFromXml ( in );
		} 
		catch ( FileNotFoundException ex ) 
		{
			exitCode = 1; // TODO: Better reporting needed
			throw new RuntimeException ( "Error: cannot find the input file '" + inFile.getAbsolutePath () + "'" );
		} 
		
		err.println ( "\nUser Updated" );
		return;		
	}

	/**
	 * Parses and then setup the input file (or the standard input).
	 */
	@Override
	protected CommandLine parse ( String... args )
	{
		if ( super.parse ( args ) == null ) return null;
		args = this.cmdLine.getArgs (); 
		if ( args.length > 2 ) inFileName = args [ 2 ];
		return cmdLine;
	}

	@Override
	public void printUsage ()
	{
		err.println ( "\n user store [xml file]" );
		err.println (   "   Creates/Updates a user definition." );
		err.println (   "   Reads from the standard input if the file is omitted. See the documentation and tests for the XML format to use." );
	}
}
