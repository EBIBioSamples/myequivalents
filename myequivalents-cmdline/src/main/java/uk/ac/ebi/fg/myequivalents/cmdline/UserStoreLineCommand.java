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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.cli.CommandLine;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.dao.access_control.UserDao;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * The line command for {@link AccessControlManager#storeUserFromXml(Reader)}.
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
			
		  if ( cmdLine.hasOption ( "y" ) )
		  {
		  	// This option works only when a database back end is available and allows for the creation of the first 
		  	// admin user in an empty database. 
		  	Resources res = Resources.getInstance ();
		  	ManagerFactory mf = res.getMyEqManagerFactory ();
		  	if ( ! ( mf instanceof DbManagerFactory ) ) throw new SecurityException ( 
		  		"The --first-user option is only available when the myEquivaents command line is configured with a database "
		  		+ "backend. Please read the documentation." 
		  	);
		   
		  	// Managers don't allow us to bypass security
		  	//
				JAXBContext context = JAXBContext.newInstance ( User.class );
				Unmarshaller u = context.createUnmarshaller ();
				User user = (User) u.unmarshal ( in );
				
		  	EntityManager em = ((DbManagerFactory) mf).getEntityManagerFactory ().createEntityManager ();
		  	EntityTransaction ts = em.getTransaction ();
		  	ts.begin ();
		  	UserDao udao = new UserDao ( em );
		  	udao.storeUnauthorized ( user );
		  	ts.commit ();
		  	em.close ();
		  }
		  else
		  {
		  	// else, do a regular operation, passing through standard managers, authentication, permission control
				AccessControlManager accMgr =
					Resources.getInstance ().getMyEqManagerFactory ().newAccessControlManagerFullAuth ( this.email, this.userPassword );
				
				accMgr.storeUserFromXml ( in );
		  }
		} 
		catch ( FileNotFoundException ex ) 
		{
			exitCode = 1; // TODO: Better reporting needed
			throw new RuntimeException ( "Error: cannot find the input file '" + inFile.getAbsolutePath () + "'" );
		} 
		catch ( JAXBException ex ) {
			throw new RuntimeException ( "Error while reading user description from XML: " + ex.getMessage (), ex );
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
