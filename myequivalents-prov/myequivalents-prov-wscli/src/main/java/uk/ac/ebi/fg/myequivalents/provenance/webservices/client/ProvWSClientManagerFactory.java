package uk.ac.ebi.fg.myequivalents.provenance.webservices.client;

import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvManagerFactory;
import uk.ac.ebi.fg.myequivalents.provenance.interfaces.ProvRegistryManager;
import uk.ac.ebi.fg.myequivalents.webservices.client.WSClientManagerFactory;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>27 Oct 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvWSClientManagerFactory extends WSClientManagerFactory implements ProvManagerFactory
{

	public ProvWSClientManagerFactory ()
	{
		super ();
	}

	public ProvWSClientManagerFactory ( String baseUrl )
	{
		super ( baseUrl );
	}

	
	@Override
	public ProvRegistryManager newProvRegistryManager ( String email, String apiPassword )
	{
		ProvRegistryManager result = new ProvRegistryWSClient ( baseUrl );
		result.setAuthenticationCredentials ( email, apiPassword );
		return result;
	}

}
