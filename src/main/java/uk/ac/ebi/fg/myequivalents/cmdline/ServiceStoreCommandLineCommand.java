package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.bind.JAXBException;

import uk.ac.ebi.fg.myequivalents.managers.ServiceManager;

/**
 * The 'service store' command. Syntax is: service store [xml-file]. This will use 
 * {@link ServiceManager#storeServicesFromXML(Reader)}. See an example of usage in {@link MainTest#testServiceStore()}. 
 *
 * <dl><dt>date</dt><dd>Jul 18, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceStoreCommandLineCommand extends LineCommand
{
	private String inFileName = null;
	
	public ServiceStoreCommandLineCommand ()
	{
		super ( "service store" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( exitCode != 0 ) return;

		File inFile = inFileName == null ? null : new File ( inFileName );
		
		try
		{
			Reader in = new BufferedReader ( inFileName == null 
				? new FileReader ( inFile ) 
			  : new InputStreamReader ( System.in )
			);
			
			ServiceManager servMgr = new ServiceManager ();
			servMgr.storeServicesFromXML ( in );
		} 
		catch ( FileNotFoundException ex ) {
			exitCode = 1; // TODO: Better reporting needed
			throw new RuntimeException ( "Error: cannot find the input file '" + inFile.getAbsolutePath () + "'" );
		} 
		catch ( JAXBException ex ) 
		{
			exitCode = 1; // TODO: Better reporting needed
			throw new RuntimeException ( String.format ( 
				"Error while tryint to read %s: %s", 
				inFile == null ? "std input" : "'" + inFile.getAbsolutePath () + "'", 
				ex.getMessage () 
			));
		}
		
		out.println ( "\nServices Updated" );
		return;
	}

	/**
	 * Parses and then setup the input file (or the standard input).
	 */
	@Override
	protected boolean parse ( String... args )
	{
		boolean result = super.parse ( args );
		if ( !result ) return false;
		args = cmdLine.getArgs (); 
		if ( args.length > 0 ) inFileName = args [ 0 ];
		return true;
	}

	@Override
	public void printUsage ()
	{
		out.println ( "\n service store [xml-file]" );
		out.println (   "   Creates/Updates service definitions and related entities (service-collections, repositories)" );
		out.println (   "   Reads from the standard input if the file is omitted. See the documentation and tests for the XML format to use." );
	}

}
