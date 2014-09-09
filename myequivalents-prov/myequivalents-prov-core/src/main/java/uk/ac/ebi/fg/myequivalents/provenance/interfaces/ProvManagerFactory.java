package uk.ac.ebi.fg.myequivalents.provenance.interfaces;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;

/**
 * An extended version of the base {@link ManagerFactory}, which additionally returns {@link ProvRegistryManager}. This
 * can be put in a myEquivalents bean configuration file, the client has to be aware of the extension if it wants to 
 * use the provenance manager (or, it can transparently ignore it, if it doesn't need that). 
 *
 * <dl><dt>date</dt><dd>1 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface ProvManagerFactory extends ManagerFactory
{
	public ProvRegistryManager newProvRegistryManager ( String email, String apiPassword );
}
