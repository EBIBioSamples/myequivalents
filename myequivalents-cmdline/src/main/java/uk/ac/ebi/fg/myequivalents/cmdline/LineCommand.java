package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import java.util.ServiceLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;

/**
 * Represents one of the sub-commands available in {@link Main}. E.g., 'service store' is managed by 
 * {@link ServiceStoreLineCommand}. As expected, this is based on the command pattern.
 *
 * <dl><dt>date</dt><dd>Jul 18, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class LineCommand
{
	protected String email, apiPassword, userPassword, outputFormat;
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
			
			email = StringUtils.trimToNull ( cmdLine.getOptionValue ( 'u' ) );
			apiPassword = StringUtils.trimToNull ( cmdLine.getOptionValue ( 's' ) );
			userPassword = StringUtils.trimToNull ( cmdLine.getOptionValue ( 'w' ) );
			outputFormat = cmdLine.getOptionValue ( "format", "xml" );
			
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

		
		opts.addOption ( OptionBuilder
		 	.withDescription ( "User email to be used to login the myEquivalents repository"	)
			.withLongOpt ( "user" )
			.hasArg ( true ).withArgName ( "email" )
			.create ( "u" )
		);
		
		opts.addOption ( OptionBuilder
		 	.withDescription ( "API secret (i.e., password), used for common commands (see documentation)" )
			.withLongOpt ( "secret" )
			.hasArg ( true ).withArgName ( "value" )
			.create ( "s" ) 
		);

		opts.addOption ( OptionBuilder
		 	.withDescription ( "User password, needed for administrative/access-control/user-change operations (see documentation)"	)
			.withLongOpt ( "password" )
			.hasArg ( true ).withArgName ( "value" )
			.create ( "w" ) 
		);
		
		if ( commandString.equals ( "user store" ) )
		{
			opts.addOption ( OptionBuilder
			 	.withDescription ( 
		 				"When issuing 'user store', creates a first user (typically and admin) in an empty database, bypassing "
		 				+ "authentication and permission checking (requires the database backend, see documentation)"
		 		)
				.withLongOpt ( "first-user" )
				.create ( "y" ) 
			);
		}

		
		if ( commandString.endsWith ( " get" ) )
		{
			opts.addOption ( OptionBuilder
			 	.hasArg ( true )
				.withDescription ( 
			 		"The result output format. ** ONLY 'xml' IS SUPPORTED IN THIS VERSION **"
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
	 * Which command is managed by a particular sub-class of this class, eg 'service get'. This is used to parse 
	 * a command line
	 */
	public String getCommandString ()
	{
		return commandString;
	}
			
	/**
	 * This gets the specific {@link LineCommand} that is associated to args[0] and args[1], e.g., 
	 * returns {@link ServiceStoreLineCommand} for { "service", "store" }. This uses {@link #getCommand(Class)}.
	 */
	static LineCommand getCommand ( String... args )
	{
		if ( args.length >= 2 )
		{
			String cmdStr = ( args [ 0 ].trim () + ' ' + args [ 1 ].trim () ).toLowerCase ();
			if ( "set".matches ( args [ 1 ].trim () ) )
				// commands like 'user set role' or 'entity set visibility', they'll be further checked below
				cmdStr += ' ' + args [ 2 ].trim ().toLowerCase (); 
			
			for ( LineCommand command: ServiceLoader.load ( LineCommand.class ) )
				if ( cmdStr.equals ( command.getCommandString () ) ) {
					return command;
			}
			
			err.println ( "\n  Wrong command '" + cmdStr + "'\n\n" );
			return new HelpLineCommand ();
		}
		else if ( args.length == 1 && !"--help".equalsIgnoreCase ( args [ 0 ].trim () ) )
			err.println ( "\n  Wrong command '" + args [ 0 ] + "'\n\n" );
		
		return new HelpLineCommand ();
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
