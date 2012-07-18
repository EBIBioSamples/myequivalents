package uk.ac.ebi.fg.myequivalents.cmdline;
import static java.lang.System.out;
import static java.lang.System.err;


/**
 * <h2>The myequivalents command line</h2>
 * 
 * The java main's class that manages the myequivalents command line. A quick overview of the available syntax is: 
 * 
 * <pre>
 * java uk.ac.ebi.fg.myequivalents.cmdline.Main command [options]
 *   service store [xml-file]
 *   service delete name... 
 *   service get [--format|-f <format>] name...
 *   service-collection get [--format|-f <format>] name...
 *   service-collection delete name... 
 *   repository get [--format|-f <format>] name...
 *   repository delete name...
 *   mapping get [--format|-f <format>] [--raw|-r] <myeq:accession@service|uri>...
 *   mapping store <myeq:accession@service|uri>...
 *   mapping store-bundle <myeq:accession@service|uri>...
 *   mapping delete <myeq:accession@service|uri>...
 *   mapping delete-entity <myeq:accession@service|uri>...
 * </pre>
 *
 * <strong>WARNING: only 'store service' is implemented at the moment!</strong>
 *
 * <dl><dt>date</dt><dd>Jul 10, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class Main
{
	/**
	 * Used for Junit tests
	 */
	static int exitCode;
	
	/**
	 * Uses {@link LineCommand#getCommand(String...)} to understand which sub-command was passed to args, args the proper
	 * {@link LineCommand} to manage it (if there is no syntax error) and then run the sub-command. Does all the secondary
	 * activities implied in this flow (e.g., exceptions, hibernate and logger setup).
	 * 
	 */
	public static void main( String... args )
	{
		LineCommand lcmd = null; 
		
		try {
			lcmd = LineCommand.getCommand ( args );
			lcmd.run ( args );
		}
		catch ( Throwable t ) {
			t.printStackTrace ( err );
		}
		finally 
		{
			exitCode = lcmd == null ? 1 : lcmd.getExitCode ();
			out.println ( "\nThe End. Quitting Java with exit code " + exitCode + "." );

			if ( !"true".equals ( System.getProperty ( "uk.ac.ebi.fg.myequivalents.test_flag" ) ) )
				System.exit ( exitCode );
		}
	}
	
}
