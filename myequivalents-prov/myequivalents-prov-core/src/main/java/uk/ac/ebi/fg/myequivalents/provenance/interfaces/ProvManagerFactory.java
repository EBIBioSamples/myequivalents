package uk.ac.ebi.fg.myequivalents.provenance.interfaces;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>1 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface ProvManagerFactory extends ManagerFactory
{
	public ProvRegistryManager newProvRegistryManager ( String email, String apiPassword );
}
