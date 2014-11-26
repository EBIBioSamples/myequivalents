/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.ArrayUtils;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter;

/**
 * This is a base class used for 'set visibility ..." commands in {@link AccessControlManager}.
 *
 * <dl><dt>date</dt><dd>Jul 31, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class AbstractVisibilitySetLineCommand extends LineCommand
{
	public AbstractVisibilitySetLineCommand ( String commandString )
	{
		super ( commandString );
	}

	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;

		args = cmdLine.getArgs ();
		if ( args != null && args.length > 3 )
		{
			String publicFlagStr = cmdLine.getOptionValue ( 'p' );
			String relDateStr = cmdLine.getOptionValue ( 'd' );
			boolean cascade = cmdLine.hasOption ( 'x' );
			
			doVisibilitySet ( publicFlagStr, relDateStr, cascade, (String[]) ArrayUtils.subarray ( args, 3, args.length ) );
		}
		
		err.println ( "\nVisibility command executed." );
		return;
	}

	protected abstract void doVisibilitySet ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... entityNames );

	
	
	
	@Override
	@SuppressWarnings ( "static-access" )
	protected Options getOptions ()
	{
		return super.getOptions ()
				
			.addOption ( OptionBuilder
			 	.withDescription ( "Public flag (visibility commands, see documentation)"	)
				.withLongOpt ( "public-flag" )
				.hasArg ( true )
				.withArgName ( "true|false|null" )
				.create ( 'p' ) 
			)

			.addOption ( OptionBuilder
			 	.withDescription ( "Release date (visibility commands, see documentation)"	)
				.hasArg ( true )
				.withLongOpt ( "release-date" )
				.withArgName ( DateJaxbXmlAdapter.DATE_FMT_REPRESENTATION )
				.create ( 'd' ) 
			)

			.addOption ( OptionBuilder
			 	.withDescription ( "Cascades the visibility settings to referring elements (e.g., from services to entitities)"	)
				.withLongOpt ( "cascade" )
				.create ( 'x' ) 
			);
	}


	@Override
	public void printUsage ()
	{
		String typeStr = this.commandString.split ( " " ) [ 0 ];
		String typeStrUI = "entity".equals ( typeStr ) ? "<service:accession|uri>" : "name";
		
		err.format ( "\n %s %s...\n", this.commandString, typeStrUI );
		err.format (   "   Set %s visibility.\n", typeStr ); 
	}
}
