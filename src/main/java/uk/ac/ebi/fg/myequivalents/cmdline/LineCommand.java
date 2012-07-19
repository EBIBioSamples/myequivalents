package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.out;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Represents one of the sub-commands available in {@link Main}. E.g., 'service store' is managed by 
 * {@link ServiceStoreCommandLineCommand}. As expected, this is based on the command pattern.
 *
 * <dl><dt>date</dt><dd>Jul 18, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
abstract class LineCommand
{
	/**
	 * All the accepted commands and how they're recognised (in the map keys). For example, 'service store' is recognised
	 * because is one of the keys of this map, and {@link ServiceStoreCommandLineCommand} is used for this command, cause
	 * it's the value corresponding to such key.
	 */
	@SuppressWarnings ( "serial" )
	public final static Map<String, Class<? extends LineCommand>> LINE_COMMANDS = 
		new HashMap<String, Class<? extends LineCommand>> ()
	{{
		put ( "service store", ServiceStoreCommandLineCommand.class );
	}};
	
	/**
	 * Which command is managed by a particular sub-class of this class.
	 */
	protected final String commandString;
	
	/**
	 * The result of {@link #parse(String...)}, use this to know what remains in the command line arguments after 
	 * having parsed the options and to gather info about the specified options. This is defined in order to ease the 
	 * overriding of {@link #run(String...)}. 
	 */
	protected CommandLine cmdLine;
	
	/**
	 * The exit code that will be returned to the operating system after the command line invocation. This is defined 
	 * to allow {@link #run(String...)} to throw exceptions, after having set the corresponding exit code.
	 */
	protected int exitCode = 0;
	
	
	/**
	 * @param commandString which command is managed by a particular sub-class of this class. 
	 */
	protected LineCommand ( String commandString )
	{
		super ();
		this.commandString = commandString;
	}

	/**
	 * Run the command. args are all the command parameters coming from the line command 
	 * (i.e., the same in {@link Main#main}. It is expected to set the {@link #exitCode} you want to return to the 
	 * operating system, after the line command execution.
	 * 
	 * The default method does nothing but: invokes {@link #parse(String[])} and, if this returns null or
	 * the --help option is specified, invokes printUsage(), sets {@link #exitCode} to 1 and returns. Otherwise returns
	 * with the exit code left at 0; This also sets {@link #cmdLine}, so your extension likely will work this way:
	 * 
	 * <pre> 
	 *   super.run ( args );
	 *   if ( this.exitCode != 0 ) return;
	 *   &lt;your command execution&gt;, which will use this.cmdLine and set {@link #exitCode} != 0 in case of error
	 * </pre>
	 * 
	 * TODO: review the exit codes.
	 */
	public void run ( String... args  ) 
	{
		if ( parse ( args ) == null || cmdLine.hasOption ( "h" ) ) {
			printUsage ();
			exitCode = 1;
		}
	}

	/**
	 * <p>Uses {@link GnuParser} to parse the command line options, doing command initialisation and possibly setup 
	 * {@link #exitCode}. Returns false if there is some parse exception or the --help option.</p> 
	 * 
	 * <p>The default implementation parses {@link #getOptions()} and returns the corresponding {@link CommandLine}
	 * generated this way. It prints an error message and returns null in case of {@link ParseException}. So you should
	 * be just fine with this implementation.</p>
	 * 
	 */
	protected CommandLine parse ( String... args ) 
	{
		CommandLineParser clparser = new GnuParser ();
		try 
		{
			cmdLine = clparser.parse ( getOptions (), args );
			return cmdLine;
		} 
		catch ( ParseException e ) {
			// Syntax error, report what the parser says and then leave run() to do printUsage()
			out.println ( "\n\n " + e.getMessage () + "\n" );
			return null;
		}		
	}
	
	
	/**
	 * Gets all the options accepted for this sub-command. This is the union of {@link #getCommonOptions()} and 
	 * {@link #getSpecificOptions()}. We make a distinction between common and 
	 * {@link #getSpecificOptions() specific} options, in order to be able to do {@link #printUsage()}.
   *
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	protected final Options getOptions()
	{
		Options opts = getCommonOptions ();
		for ( Option opt: (Collection<Option>) getSpecificOptions ().getOptions () )
			opts.addOption ( opt );
		return opts;
	}
	
	
	/**
	 * Gets those options that are common to all sub-commands. These are embedded in {@link #getOptions()}. 
	 *  
	 * We make a distinction between common and {@link #getSpecificOptions() specific} options, in order to be able to
	 * do {@link #printUsage()}.
	 */
	@SuppressWarnings ( "static-access" )
	protected static final Options getCommonOptions ()
	{
		Options opts = new Options ();

		opts.addOption ( OptionBuilder
			 	.withDescription ( 
			 		"Prints this help message"
			 	)
				.withLongOpt ( "help" )
				.create ( "h" ) 
			);
		
		return opts;
	}

	
	/**
	 * <p>Gets options that are specific to the sub-command managed by a specific extension of this class. 
	 * These are embedded in {@link #getOptions()}.</p>
	 *  
	 * <p>We make a distinction between common and {@link #getSpecificOptions() specific} options, in order to be able to
	 * do {@link #printUsage()}.</p>
	 * 
	 * <p>You'll see that at the moment this method is not overridden 
	 * in currently existing sub-classes, since it's simpler to just have a conditional code based on {@link #commandString}.</p>
	 *  
	 */
	@SuppressWarnings ( "static-access" )
	protected Options getSpecificOptions () 
	{
		Options opts = new Options ();
		
		if ( commandString.endsWith ( " get" ) )
			opts.addOption ( OptionBuilder
			 	.hasArg ( true )
				.withDescription ( 
			 		"The result output format. ** ONLY 'xml' IS SUPPORTED IN THIS VERSION"
			 	)
				.withLongOpt ( "format" )
				.withArgName ( "out-format" )
				.create ( "f" )
			);
		
		if ( "mapping get".equals ( commandString ) )
			opts.addOption ( OptionBuilder
			 	.withDescription ( 
				 		"Returns a raw result, i.e., with just the mappings and no details about services/service-collections/repositories"
				 	)
					.withLongOpt ( "raw" )
					.create ( "r" ) 
			);
		
		return opts;
	}
	
	
	/**
	 * This gets the specific {@link LineCommand} that is associated to args[0] and args[1], e.g., 
	 * returns {@link ServiceStoreCommandLineCommand} for { "service", "store" }.
	 */
	static LineCommand getCommand ( String... args )
	{
		Class<? extends LineCommand> cmdClass = HelpLineCommand.class;
		
		if ( args.length >= 2 )
		{
			String cmdStr = ( args [ 0 ].trim () + ' ' + args [ 1 ].trim () ).toLowerCase ();
			cmdClass = LINE_COMMANDS.get ( cmdStr );
			if ( cmdClass == null ) {
				out.println ( "\n  Wrong command '" + cmdStr + "'\n\n" );
				cmdClass = HelpLineCommand.class;
			}
		}
		else if ( args.length == 1 && !"--help".equalsIgnoreCase ( args [ 0 ].trim () ) )
			out.println ( "\n  Wrong command '" + args [ 0 ] + "'\n\n" );

		LineCommand result = null;
		Exception invEx = null;
		try
		{
			result = (LineCommand) ConstructorUtils.invokeConstructor ( cmdClass, null );
		} 
		catch ( NoSuchMethodException ex ) {
			invEx = ex;
		} 
		catch ( IllegalAccessException ex ) {
			invEx = ex;
		} 
		catch ( InvocationTargetException ex ) {
			invEx = ex;
		}
		catch ( InstantiationException ex ) {
			invEx = ex;
		}
		if ( invEx != null ) throw new RuntimeException (
			"Internal error while parsing the command line: " + invEx.getMessage (), invEx
		);

		return result;
	}

	/**
	 * This prints a usage message for the command. The default version reports {@link #commandString} and {@link #getSpecificOptions()}.
	 * {@link HelpLineCommand#printUsage()} reports the usage output given from the commands in {@link #LINE_COMMANDS} and also
	 * {@link #getCommonOptions()}. Finally, it also set {@link #exitCode} at 1 (TODO: suitable value).
	 */
	protected void printUsage () 
	{
		HelpFormatter helpFormatter = new HelpFormatter ();
		PrintWriter pw = new PrintWriter ( out, true );
		helpFormatter.printHelp ( pw, 100, 
			commandString, 
			"",
			getSpecificOptions (), 
			2, 4, 
			"\n", 
			false 
		);
		out.println ( "" );
	}

	/**
	 * Setup during the command argument parsing and execution with a suitable exit code value. So, you should exit with this
	 * after execution or exceptions.
	 *  
	 */
	int getExitCode () {
		return exitCode;
	}
}
