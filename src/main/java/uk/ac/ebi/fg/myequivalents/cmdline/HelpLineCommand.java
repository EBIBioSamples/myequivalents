package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.out;

/**
 * This is a fall-back 'pseudo-command'. When the syntax is wrong, no sub-command is found in 
 * {@link LineCommand#LINE_COMMANDS}, or the --help option is specified, this command is invoked. It essentially 
 * calls {@link #printUsage()}, which reports instructions about all commands.
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
	 * Reports usage instructions about all the sub-commands available in {@link LineCommand#LINE_COMMANDS}, together with
	 * {@link LineCommand#getCommonOptions()}. 
	 *  
	 */
	@Override
	public void printUsage ()
	{
		out.println ();

		out.println ( "\n\n *** The MyEquivalents Command Line Interface ***" );
		out.println ( "\nCommand-line access to several functions in the MyEquivalents Infrastructure.\n" );

		out.println ( "General Syntax:" );
		out.println ( "\n <command> [options]" );
		
		out.println ( "\nAvailable Commands: " );
		
		for ( String cmd[]: new String[][] {{ "service", "store" }} )
			LineCommand.getCommand ( cmd [ 0 ], cmd [ 1 ] ).printUsage ();
		
		out.println ( "\n --help" );
		out.println (   "   Prints this help message\n" );
	}
	
}
