package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>11 Jul 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class EntityVisibilitySetLineCommand extends AbstractVisibilitySetLineCommand
{

	public EntityVisibilitySetLineCommand ()
	{
		super ( "entity set visibility" );
	}

	@Override
	protected void doVisibilitySet ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceNames )
	{
		AccessControlManager acMgr = Resources.getInstance ().getMyEqManagerFactory ().newAccessControlManager ( this.email, this.apiPassword );
		acMgr.setEntityVisibility ( publicFlagStr, releaseDateStr, serviceNames );
	}

}
