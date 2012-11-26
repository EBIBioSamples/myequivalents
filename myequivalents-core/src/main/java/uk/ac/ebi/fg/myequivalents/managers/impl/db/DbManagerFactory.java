/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.managers.impl.db;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.AbstractManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;

/**
 * Returns managers that are based on direct connection to a relational database. 
 * 
 * <dl><dt>date</dt><dd>Nov 26, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DbManagerFactory extends AbstractManagerFactory
{
	@Override
	public EntityMappingManager newEntityMappingManager ()
	{
		return new DbEntityMappingManager ();
	}

	@Override
	public ServiceManager newServiceManager ()
	{
		return new DbServiceManager ();
	}
}
