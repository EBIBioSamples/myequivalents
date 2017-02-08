package uk.ac.ebi.fg.myequivalents.cmdline;

import static java.lang.System.err;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

/**
 * Counts all the entities in the myEquivalents backend. This might be useful for parallel
 * exports, done via cloud/cluster. We plan to develop scripts to do so in future.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Oct 2016</dd></dl>
 *
 */
public class BackupCountEntitiesLineCommand extends LineCommand
{	
	public BackupCountEntitiesLineCommand ()
	{
		super ( "backup count-entities" );
	}
	
	@Override
	public void run ( String... args )
	{
		super.run ( args );
		if ( this.exitCode != 0 ) return;
								
		BackupManager bkpMgr = 
			Resources.getInstance ().getMyEqManagerFactory ().newBackupManager ( this.email, this.apiPassword );
					
		System.out.println ( bkpMgr.countEntities () );
		err.println ( "\nFinished" );
	}

	
	@Override
	public void printUsage ()
	{
		err.println ( "\n backup count-entities" );
		err.println (   "   Counts all the entities in the myEquivalents backend" );
		err.println (   "   (will be used in future for parallel dump scripts)." );
	}

}
