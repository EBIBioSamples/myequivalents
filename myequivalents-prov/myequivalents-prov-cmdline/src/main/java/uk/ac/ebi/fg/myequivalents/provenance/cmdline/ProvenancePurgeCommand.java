package uk.ac.ebi.fg.myequivalents.provenance.cmdline;

import static java.lang.System.err;
import static java.lang.System.out;
import static uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter.STR2DATE;

import java.util.Date;

import org.apache.commons.cli.Options;

import uk.ac.ebi.fg.myequivalents.cmdline.LineCommand;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * Implementation of {@link ProvRegistryManager#purge(Date, Date)}.
 *   
 * {@code provenance purge  
 *   [--prov-from <YYYYMMDD[-HHMMSS]]> [--prov-to <YYYYMMDD[-HHMMSS]]>} 
 *   
 * <dl><dt>date</dt><dd>2 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvenancePurgeCommand extends LineCommand
{
	
	public ProvenancePurgeCommand ()
	{
		super ( "provenance purge" );
	}

	
	
	@Override
	public void run ( String ... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;
		
		Date from = STR2DATE.unmarshal ( this.cmdLine.getOptionValue ( "prov-from" ) ), 
				 to = STR2DATE.unmarshal ( this.cmdLine.getOptionValue ( "prov-to" ) );
		
		ProvManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();
		ProvRegistryManager regMgr = mgrFact.newProvRegistryManager ( this.email, this.apiPassword );

		int result = regMgr.purge ( from, to );
		out.println ( "\nDone, " + result + " provenance entries removed." );
	}



	@Override
	protected Options getOptions ()
	{
		return super.getOptions ()
			.addOption ( ProvenanceFindCommand.newProvFromOption ()	)
			.addOption ( ProvenanceFindCommand.newProvToOption ()	);
	}



	@Override
	protected void printUsage ()
	{
		err.println ( "\n provenance purge [--prov-from <YYYYMMDD[-HHMMSS]]> [--prov-to <YYYYMMDD[-HHMMSS]]" );
		err.println ( "   Remove old provenance entries in a given date range. For each parameter found in the range," );
		err.println ( "   all the entries about such parameter are removed, except the most recent one, in order to keep " );
		err.println ( "   track of what/who produced a given record)" );
	}

}
