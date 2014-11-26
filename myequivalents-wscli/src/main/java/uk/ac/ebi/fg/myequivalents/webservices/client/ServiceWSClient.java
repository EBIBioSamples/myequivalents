package uk.ac.ebi.fg.myequivalents.webservices.client;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceSearchResult;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.utils.io.IOUtils;

import com.sun.jersey.api.representation.Form;

/**
 * The web service client implementation of {@link ServiceManager}.
 *
 * <dl><dt>date</dt><dd>29 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ServiceWSClient extends MyEquivalentsWSClient implements ServiceManager
{

	public ServiceWSClient ()
	{
		super ();
	}


	public ServiceWSClient ( String baseUrl )
	{
		super ( baseUrl );
	}

	
	@Override
	protected String getServicePath () {
		return "/service";
	}


	@Override
	public void storeServices ( Service ... services )
	{
		ServiceSearchResult serviceItems = new ServiceSearchResult ();
	  for ( Service service: services ) serviceItems.addService ( service );

	  invokeStoreReq ( serviceItems );
	}	
	
	
	@Override
	public int deleteServices ( String ... names )
	{
		Form req = prepareReq ();
	  for ( String serviceName: names ) req.add ( "service", serviceName );
	  
	  return invokeIntWsReq ( "/delete", req );
	}


	@Override
	public ServiceSearchResult getServices ( String ... names )
	{
		Form req = prepareReq ();
	  for ( String serviceName: names ) req.add ( "service", serviceName );
	  
	  ServiceSearchResult result = invokeWsReq ( "/get", req, ServiceSearchResult.class );
	  if ( result == null ) return null;
	  
	  Map<String, Repository> repos = new HashMap<String, Repository> ();
	  for ( Repository repo: result.getRepositories () ) repos.put ( repo.getName (), repo );
	  
	  Map<String, ServiceCollection> scs = new HashMap<String, ServiceCollection> ();
	  for ( ServiceCollection sc: result.getServiceCollections () ) scs.put ( sc.getName (), sc );
	  
	  // Now we have to reconstruct the links from services
	  for ( Service service: result.getServices () )
	  {
	  	String repoName = service.getRepositoryName ();
	  	if ( repoName != null ) service.setRepository ( repos.get ( repoName ) );
	  	
	  	String scName = service.getServiceCollectionName ();
	  	if ( scName != null ) service.setServiceCollection ( scs.get ( scName ) );
	  }
	  
	  return result;
	}


	@Override
	public String getServicesAs ( String outputFormat, String ... names )
	{
		Form req = prepareReq ();
	  return getRawResult ( "/get", req, outputFormat );
	}


	@Override
	public void storeServiceCollections ( ServiceCollection ... servColls )
	{
		ServiceSearchResult serviceItems = new ServiceSearchResult ();
	  for ( ServiceCollection sc: servColls ) serviceItems.addServiceCollection ( sc );

	  invokeStoreReq ( serviceItems );
	}


	@Override
	public int deleteServiceCollections ( String ... names )
	{
		Form req = prepareReq ();
	  for ( String name: names ) req.add ( "service-coll", name );
	  
	  return invokeIntWsReq ( "/service-collection/delete", req );
	}


	@Override
	public ServiceSearchResult getServiceCollections ( String ... names )
	{
		Form req = prepareReq ();
	  for ( String name: names ) req.add ( "service-coll", name );
	  
	  return invokeWsReq ( "/service-collection/get", req, ServiceSearchResult.class );
	}


	@Override
	public String getServiceCollectionsAs ( String outputFormat, String ... names )
	{
		Form req = prepareReq ();
	  return getRawResult ( "/service-collection/get", req, outputFormat );
	}


	@Override
	public void storeRepositories ( Repository ... repos )
	{
		ServiceSearchResult serviceItems = new ServiceSearchResult ();
	  for ( Repository repo: repos ) serviceItems.addRepository ( repo );

	  invokeStoreReq ( serviceItems );
	}


	@Override
	public int deleteRepositories ( String ... names )
	{
		Form req = prepareReq ();
	  for ( String name: names ) req.add ( "repository", name );
	  
	  return invokeIntWsReq ( "/repository/delete", req );
	}


	@Override
	public ServiceSearchResult getRepositories ( String ... names )
	{
		Form req = prepareReq ();
	  for ( String name: names ) req.add ( "repository", name );
	  
	  return invokeWsReq ( "/repository/get", req, ServiceSearchResult.class );
	}


	@Override
	public String getRepositoriesAs ( String outputFormat, String ... names )
	{
		Form req = prepareReq ();
	  return getRawResult ( "/repository/get", req, outputFormat );
	}

	
	@Override
	public void storeServicesFromXML ( Reader reader )
	{
		try {
			storeServicesFromXML ( IOUtils.readInputFully ( reader ) );
		}
		catch ( IOException ex ) {
			throw new RuntimeException ( "Error while invoking the myEq web service for 'store': " + ex.getMessage (), ex );
		}
	}

	/**
	 * Used to implement {@link #storeServicesFromXML(Reader). It invokes a corresponding operation's on the server side.
	 */
	private void storeServicesFromXML ( String serviceSearchResultXml )
	{
		// TODO: This should be an interface method and we should use AOP or Java 8 to provide default implementations to 
		// the interface.
		//
	  Form req = prepareReq ();
	  req.add ( "service-items-xml", serviceSearchResultXml );
		invokeVoidWsReq ( "/store", req );
	}
	
	/**
	 * An helper for storeXXX() methods.
	 */
	private void invokeStoreReq ( ServiceSearchResult serviceItems )
	{
		try
		{
			// Add the service collection and repository that the service refers to
		  for ( Service service: serviceItems.getServices () )
		  {
		  	Repository repo = service.getRepository ();
		  	if ( repo != null ) serviceItems.addRepository ( repo );
		  	
		  	ServiceCollection sc = service.getServiceCollection ();
		  	if ( sc != null ) serviceItems.addServiceCollection ( sc );
		  }
		  
			if ( log.isTraceEnabled () ) 
		  	log.trace ( "Requesting web service: {}\n: {}", getServicePath () + "/store", serviceItems );
				  
			StringWriter xmlw = new StringWriter ();
			
			JAXBContext context = JAXBContext.newInstance ( ServiceSearchResult.class );
			Marshaller m = context.createMarshaller ();
			m.marshal ( serviceItems,  xmlw );
			
			storeServicesFromXML ( xmlw.toString () );
		} 
		catch ( JAXBException ex ) {
			throw new RuntimeException ( "Error while invoking the myEq web service for 'store':" + ex.getMessage (), ex );
		} 
	}
	
}
