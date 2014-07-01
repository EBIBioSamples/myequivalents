package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.ServiceLoader;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


/**
 * This is a fall-back 'pseudo-command'. When the syntax is wrong, no sub-command is found in 
 * {@link LineCommand#getCommand(String...)}, or the --help option is specified, this command is invoked.
 * It essentially calls {@link #printUsage()}, which reports instructions about all commands.
 *
 * <dl><dt>date</dt><dd>Jul 18, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
class HelpLineCommand extends LineCommand
{

	public HelpLineCommand () {
		super ( "" );
	}

	

	/**
	 * Does {@link #printUsage()} and set {@link LineCommand#exitCode exitCode} to 1, independently on args (we've already 
	 * fallen back here, so we must do this any way). 
	 */
	@Override
	public void run ( String ... args )
	{
		parse ( args );
		printUsage ();
		exitCode = 1;
	}



	/**
	 * Reports usage instructions about all the sub-commands available in {@link LineCommand#getCommand(String...)}, 
	 * together with {@link LineCommand#getCommonOptions()}. 
	 *  
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public void printUsage ()
	{
		err.println ();

		err.println ( "\n\n *** The MyEquivalents Command Line Interface ***" );
		err.println ( "\nCommand-line access to several functions in the MyEquivalents Infrastructure." );

		err.println ( "\nGeneral Syntax:" );
		err.println ( "\n myeq.sh <command> [dash options] [further options]" );
				
		err.println ( "\nAvailable Commands (and specific options)" );
		
		Options allOpts = new Options ();
		for ( LineCommand lcmd: ServiceLoader.load ( LineCommand.class ) )
		{
			lcmd.printUsage ();
			for ( Option opt: (Collection<Option>) lcmd.getOptions ().getOptions () )
				if ( !allOpts.hasOption ( opt.getOpt () ))
					allOpts.addOption ( opt ); 
		}
		
		err.println ( "\nOptions (to be given after 'command' and before further options):" );
		
		HelpFormatter helpFormatter = new HelpFormatter ();
		PrintWriter pw = new PrintWriter ( err, true );
		helpFormatter.printOptions ( pw,100, allOpts, 2, 4 );
		err.println ();
	}
	
}
