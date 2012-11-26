/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.managers.interfaces;


/**
 * The abstract factory for the MyEquivalents managers. This returns the managers needed to access the MyEquivalents
 * system. Several concrete implementations are available (e.g., BaseManagerFactory, WsCliManagerFactory TODO).  
 *
 * <dl><dt>date</dt><dd>Nov 26, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class AbstractManagerFactory
{
	public abstract EntityMappingManager newEntityMappingManager ();
	public abstract ServiceManager newServiceManager ();
}
