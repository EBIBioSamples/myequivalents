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
public class ServiceCollectionVisibilitySetLineCommand extends AbstractVisibilitySetLineCommand
{

	public ServiceCollectionVisibilitySetLineCommand ()
	{
		super ( "service-collection set visibility" );
	}

	@Override
	protected void doVisibilitySet ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceNames )
	{
		AccessControlManager acMgr = Resources.getInstance ().getMyEqManagerFactory ().newAccessControlManager ( this.email, this.apiPassword );
		acMgr.setServiceCollectionsVisibility ( publicFlagStr, releaseDateStr, cascade, serviceNames );
	}

}
