package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
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
		new LinkedHashMap<String, Class<? extends LineCommand>> ()
	{{
		put ( "service store", ServiceStoreCommandLineCommand.class );
		put ( "service delete", ServiceDeleteLineCommand.class );
		put ( "service get", ServiceGetLineCommand.class );
		put ( "service-collection delete", ServiceCollectionDeleteLineCommand.class );
		put ( "service-collection get", ServiceCollectionGetLineCommand.class );
		put ( "repository delete", RepositoryDeleteLineCommand.class );
		put ( "repository get", RepositoryGetLineCommand.class );
		put ( "mapping store", MappingStoreCommandLineCommand.class );
		put ( "mapping store-bundle", MappingStoreBundleCommandLineCommand.class );
		put ( "mapping delete", MappingDeleteCommandLineCommand.class );
		put ( "mapping delete-entity", MappingDeleteEntityCommandLineCommand.class );
		put ( "mapping get", MappingGetCommandLineCommand.class );
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
			err.println ( "\n\n " + e.getMessage () + "\n" );
			return null;
		}		
	}
	
	
	/**
   * Gets the command line options. This default implementations should be fine for all the commands (and it's quicker 
   * to implement it here, see its internals).
	 * 
	 */
	@SuppressWarnings ( { "static-access" } )
	protected Options getOptions()
	{
		Options opts = new Options ();

		opts.addOption ( OptionBuilder
		 	.withDescription ( "Prints this help message"	)
			.withLongOpt ( "help" )
			.create ( "h" ) 
		);
		
		if ( commandString.endsWith ( " get" ) )
		{
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
		} // if ' get'
		
		return opts;
	}
	
	

	

	/**
	 * Instantiates a line command class, providing common checks over reflection errors. This is used in both 
	 * {@link #getCommand(String...)} and elsewhere.
	 * 
	 */
	static LineCommand getCommand ( Class<? extends LineCommand> lineCmdClass )
	{
		LineCommand result = null;
		Exception invEx = null;
		try
		{
			result = (LineCommand) ConstructorUtils.invokeConstructor ( lineCmdClass, null );
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
	 * This gets the specific {@link LineCommand} that is associated to args[0] and args[1], e.g., 
	 * returns {@link ServiceStoreCommandLineCommand} for { "service", "store" }. This uses {@link #getCommand(Class)}.
	 */
	static LineCommand getCommand ( String... args )
	{
		Class<? extends LineCommand> cmdClass = HelpLineCommand.class;
		
		if ( args.length >= 2 )
		{
			String cmdStr = ( args [ 0 ].trim () + ' ' + args [ 1 ].trim () ).toLowerCase ();
			cmdClass = LINE_COMMANDS.get ( cmdStr );
			if ( cmdClass == null ) {
				err.println ( "\n  Wrong command '" + cmdStr + "'\n\n" );
				cmdClass = HelpLineCommand.class;
			}
		}
		else if ( args.length == 1 && !"--help".equalsIgnoreCase ( args [ 0 ].trim () ) )
			err.println ( "\n  Wrong command '" + args [ 0 ] + "'\n\n" );
		
		return getCommand ( cmdClass );
	}

	/**
	 * This prints a usage message for the command. 
	 */
	protected abstract void printUsage (); 

	/**
	 * Setup during the command argument parsing and execution with a suitable exit code value. So, you should exit with this
	 * after execution or exceptions.
	 *  
	 */
	int getExitCode () {
		return exitCode;
	}
}
