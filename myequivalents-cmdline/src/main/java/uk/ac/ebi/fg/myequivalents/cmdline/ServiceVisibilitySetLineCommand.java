package uk.ac.ebi.fg.myequivalents.cmdline;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * 
 * The line command for {@link AccessControlManager#setServicesVisibility(String, String, boolean, String...)}.
 *
 * <dl><dt>date</dt><dd>11 Jul 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceVisibilitySetLineCommand extends AbstractVisibilitySetLineCommand
{

	public ServiceVisibilitySetLineCommand ()
	{
		super ( "service set visibility" );
	}

	@Override
	protected void doVisibilitySet ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceNames )
	{
		AccessControlManager acMgr = Resources.getInstance ().getMyEqManagerFactory ().newAccessControlManager ( this.email, this.apiPassword );
		acMgr.setServicesVisibility ( publicFlagStr, releaseDateStr, cascade, serviceNames );
	}

}
