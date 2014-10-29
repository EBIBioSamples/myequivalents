package uk.ac.ebi.fg.myequivalents.provenance.cmdline;

import static java.lang.System.err;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import uk.ac.ebi.fg.myequivalents.cmdline.LineCommand;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import static uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter.*;

/**
 * TODO: comment me! 
 *  
 * {@code provenance-find [--prov-user <user>] [--prov-operation <op>] 
 *   [--prov-from <YYYYMMDD[-HHMMSS]]> [--prov-to <YYYYMMDD[-HHMMSS]]> [--prov-param <type:value[:extraValue]>} 
 *
 * <dl><dt>date</dt><dd>2 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenanceFindCommand extends LineCommand
{
	
	@SuppressWarnings ( "static-access" )
	public final static Option PROV_TO_OPT = OptionBuilder
	 	.withDescription ( "provenance find/purge, period to search"	)
		.hasArg ( true )
		.withLongOpt ( "prov-to" )
		.withArgName ( DATE_FMT_REPRESENTATION )
		.create ( 'b' );
	
	
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
		
		String[] paramsOption = this.cmdLine.getOptionValues ( "prov-param" );
		List<ProvenanceRegisterParameter> params = null;
		if ( paramsOption != null && paramsOption.length != 0 )
		{
			params = new ArrayList<> ();
			for ( String paramOpt: paramsOption )
				params.add ( ProvenanceRegisterParameter.p ( paramOpt ) );
		}
		
		
		ProvManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( this.email, this.apiPassword );

		System.out.println ( regMgr.findAs ( this.outputFormat, email, op, from, to, params ) );
		err.println ( "\nProvenance info Fetched" );
	}



	@Override
	@SuppressWarnings ( "static-access" )
	protected Options getOptions ()
	{
		return super.getOptions ()
			.addOption ( "e", "prov-user", true, "provenance find, searched user email/login" )
			.addOption ( "o", "prov-operation", true, "provenance find, operation to search" )
			.addOption ( newProvFromOption () )
			.addOption ( newProvToOption ()	)
			.addOption ( 
				OptionBuilder
					.withDescription ( "provenance find, operation parameters to search (option can be repeated)" )
					.withLongOpt ( "prov-param" )
					.hasArg ()
					.withArgName ( "type:value[:extraValue]" )
					.create ( 'm' )
			);
	}



	@Override
	protected void printUsage ()
	{
		err.println ( "\n provenance find ..." );
		err.println (   "   Finds provenance records, '%' can be used as wildcard" );
	}
	
	
	@SuppressWarnings ( "static-access" )
	public final static Option newProvFromOption ()
	{
		return OptionBuilder
		 	.withDescription ( 
		 		"provenance find/purge, period to search, use something like $(date -v -1y +%Y%m%d) for calculating 1 year ago"	)
			.hasArg ( true )
			.withLongOpt ( "prov-from" )
			.withArgName ( DATE_FMT_REPRESENTATION )
			.create ( 'a' );
	}

	@SuppressWarnings ( "static-access" )
	public final static Option newProvToOption ()
	{
		return OptionBuilder
		 	.withDescription ( "provenance find/purge, period to search"	)
			.hasArg ( true )
			.withLongOpt ( "prov-to" )
			.withArgName ( DATE_FMT_REPRESENTATION )
			.create ( 'b' );
	}

}
