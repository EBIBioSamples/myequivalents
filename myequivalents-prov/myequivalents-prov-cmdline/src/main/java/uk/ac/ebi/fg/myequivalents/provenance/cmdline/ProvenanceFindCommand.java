package uk.ac.ebi.fg.myequivalents.provenance.cmdline;

import static java.lang.System.err;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import uk.ac.ebi.fg.myequivalents.cmdline.LineCommand;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import static uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter.*;



/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>2 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceFindCommand extends LineCommand
{
	
	public ProvenanceFindCommand ()
	{
		super ( "provenance find" );
	}

	
	
	@Override
	public void run ( String ... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;
		
		String email = this.cmdLine.getOptionValue ( "prov-user" );
		String op = this.cmdLine.getOptionValue ( "prov-operation" );
		Date from = STR2DATE.unmarshal ( this.cmdLine.getOptionValue ( "prov-from" ) ), 
				 to = STR2DATE.unmarshal ( this.cmdLine.getOptionValue ( "prov-to" ) );
		List<String> paramPairs = Arrays.asList ( this.cmdLine.getOptionValues ( "prov-param" ) );
		
		this.cmdLine.getOptionValues ( 'x' );
		
		ProvManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( this.email, this.apiPassword );

		System.out.println ( regMgr.findAs ( "xml", email, op, from, to, paramPairs ) );
		err.println ( "\nMapping(s) Fetched" );
	}



	@Override
	@SuppressWarnings ( "static-access" )
	protected Options getOptions ()
	{
		return super.getOptions ()
			.addOption ( "e", "prov-user", true, "provenance find, searched user email/login" )
			.addOption ( "o", "prov-operation", true, "provenance find, operation to search" )
			.addOption ( OptionBuilder
			 	.withDescription ( "provenance find, period to search"	)
				.hasArg ( true )
				.withLongOpt ( "prov-from" )
				.withArgName ( DATE_FMT_REPRESENTATION )
				.create ( 'a' ) 
			)
			.addOption ( OptionBuilder
			 	.withDescription ( "provenance find, period to search"	)
				.hasArg ( true )
				.withLongOpt ( "prov-to" )
				.withArgName ( DATE_FMT_REPRESENTATION )
				.create ( 'b' ) 
			)
			.addOption ( 
				OptionBuilder
					.withDescription ( "provenance find, operation parameters to search (option can be repeated)" )
					.withLongOpt ( "prov-param" )
					.hasArgs ( 2 )
					.withArgName ( "type:value" )
					.withValueSeparator ( ':' )
					.create ( 'm' )
			);
	}



	@Override
	protected void printUsage ()
	{
		err.println ( "\n provenance find ..." );
		err.println (   "   Finds provenance records, '%' can be used as wildcard" );
	}

}
