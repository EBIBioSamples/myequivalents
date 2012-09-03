package uk.ac.ebi.fg.myequivalents.cmdline;
import static java.lang.System.err;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;

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
 *   mapping get [--format|-f <format>] [--raw|-r] [--uris] <serviceName accession|uri>...
 *   mapping store serviceName accession|<uri>...
 *   mapping store-bundle serviceName accession|<uri>...
 *   mapping delete serviceName accession|<uri>...
 *   mapping delete-entity serviceName accession|<uri>...
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
			exitCode = lcmd == null ? 1 : lcmd.getExitCode ();
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
			if ( !"true".equals ( System.getProperty ( "uk.ac.ebi.fg.myequivalents.test_flag" ) ) ) 
			{
				// Sounds like we need to shutdown HSQLDB, due to System.exit()
				//
				EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
				EntityManager em = emf.createEntityManager ();
				((Session) em.getDelegate ()).doWork ( new Work() 
				{
					@Override
					public void execute ( Connection connection ) throws SQLException
					{
						DatabaseMetaData dbmsMeta = connection.getMetaData ();
						String dbmsName = dbmsMeta.getDatabaseProductName ().toLowerCase ();
						if ( dbmsName.contains ( "hsql" ) ) {
							connection.createStatement ().executeUpdate ( "SHUTDOWN;" );
							connection.commit ();
						}
					}
				});
				// Just in case, let's do this too
				emf.close ();
				
				err.println ( "\nThe End. Quitting Java with exit code " + exitCode + "." );
				System.exit ( exitCode );
			}
		}
	}
}
