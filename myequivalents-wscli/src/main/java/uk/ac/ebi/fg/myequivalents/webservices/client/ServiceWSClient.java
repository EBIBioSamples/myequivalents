package uk.ac.ebi.fg.myequivalents.webservices.client;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

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
 * 
 * TODO: Comment me!
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
	public void storeServicesFromXML ( Reader reader )
	{
		try 
		{
		  Form req = prepareReq ();
		  req.add ( "service-items-xml", IOUtils.readInputFully ( reader ) );
			invokeVoidWsReq ( "/store", req );
		}
		catch ( IOException ex ) {
			throw new RuntimeException ( "Error while invoking the myEq web service for 'store': " + ex.getMessage (), ex );
		}
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
	  
	  return invokeWsReq ( "/get", req, ServiceSearchResult.class );
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
	
	private void invokeStoreReq ( ServiceSearchResult serviceItems )
	{
		try
		{
			// Add the service collection and reposotory that the service refers to
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
			
			storeServicesFromXML ( new StringReader ( xmlw.toString () ) );
		} 
		catch ( JAXBException ex ) {
			throw new RuntimeException ( "Error while invoking the myEq web service for 'store':" + ex.getMessage (), ex );
		} 
	}
	
}
