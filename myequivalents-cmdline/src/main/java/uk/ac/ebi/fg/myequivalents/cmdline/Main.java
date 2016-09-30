package uk.ac.ebi.fg.myequivalents.cmdline;
import static java.lang.System.err;

import javax.persistence.EntityManagerFactory;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.resources.Resources;


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
 *   mapping get [--format|-f <format>] [--raw|-r] <serviceName:accession|uri>...
 *   mapping store serviceName:accession|<uri>...
 *   mapping store-bundle serviceName:accession|<uri>...
 *   mapping delete serviceName:accession|<uri>...
 *   mapping delete-entity serviceName:accession|<uri>...
 *   user delete email
 *   user get email
 *   user set role email role
 *   user store [xml file]
 * </pre>
 * 
 * TODO: The <uri> support is not implemented yet. 
 *
 * <dl><dt>date</dt><dd>Jul 10, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class Main
{
	/**
	 * Used for JUnit tests
	 */
	static int exitCode = 0;
	
	/**
	 * Uses {@link LineCommand#getCommand(String...)} to understand which sub-command was passed to args, args the proper
	 * {@link LineCommand} to manage it (if there is no syntax error) and then run the sub-command. Does all the secondary
	 * activities implied in this flow (e.g., exceptions, Hibernate and logger setup).
	 * 
	 */
	public static void main( String... args )
	{
		LineCommand lcmd = null; 
		
		try {
			lcmd = LineCommand.getCommand ( args );
			lcmd.run ( args );
			exitCode = lcmd.getExitCode ();
		}
		catch ( Throwable t ) 
		{
			exitCode = lcmd == null ? 1 : lcmd.getExitCode ();
			if ( exitCode == 0 )
				// If it is still 0, despite the error condition, force it to a non-zero code
				exitCode = 1;
			t.printStackTrace ( err );
		}
		finally 
		{
			// This brutality has to be disabled during Junit tests
			// 
			if ( !"true".equals ( System.getProperty ( "uk.ac.ebi.fg.myequivalents.test_flag" ) ) ) 
			{
				ManagerFactory mgrf = Resources.getInstance ().getMyEqManagerFactory ();
				
				if ( mgrf instanceof DbManagerFactory )
				{
					EntityManagerFactory emf = ( (DbManagerFactory) mgrf).getEntityManagerFactory ();

					// Just in case, let's do this too
					emf.close ();
				} // if mgrf
				
				err.println ( "\nThe End. Quitting Java with exit code " + exitCode + "." );
				System.exit ( exitCode );
				
			} // if test_flag
		}
	}
}
