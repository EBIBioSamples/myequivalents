package uk.ac.ebi.fg.myequivalents.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;

/**
 * 
 * This is the class used to format the responses returned by the {@link EntityMappingManager}. For instance, the SOAP-based
 * web service uses this class to format its output in XML (thanks to JAXB mappings).  
 * 
 * <dl><dt>date</dt><dd>Jun 11, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@XmlRootElement ( name = "mappings" )
@XmlAccessorType ( XmlAccessType.NONE )
@XmlType ( name = "", propOrder = { "services", "repositories", "serviceCollections", "bundles" } )
public class EntityMappingSearchResult
{
	/**
	 * This represents a set of entities that cross-reference each-other, it's the thing returned by 
	 * {@link EntityMappingSearchResult#getBundles()}. Having this class in between is a bit cumbersome, 
	 * but required for proper mapping via JAXB.
	 *
	 * <dl><dt>date</dt><dd>Jul 18, 2012</dd></dl>
	 * @author Marco Brandizi
	 *
	 */
	@XmlRootElement ( name = "bundle" )
	@XmlAccessorType ( XmlAccessType.FIELD )
	public static class Bundle
	{
		protected Bundle () {
		}
		
		@XmlElement ( name = "entity" )
		private Set<Entity> entities = new HashSet<Entity> ();

		private void addEntity ( Entity entity ) {
			this.entities.add ( entity );
		}

		public Set<Entity> getEntities ()
		{
			return entities;
		}
	}
	
	private final boolean wantRawResult;
	
	private final Set<Service> services;
	private final Set<ServiceCollection> serviceCollections;
	private final Set<Repository> repositories;

	private final Map<String, Bundle> bundles = new HashMap<String, Bundle> ();
	
	
	EntityMappingSearchResult ()
	{
		this ( false );
	}

	/**
	 * @param wantRawResult if true, only bundles will be stored into this object. See 
	 * {@link EntityMappingManager#getMappings(boolean, String...)}.
	 */
	EntityMappingSearchResult ( boolean wantRawResult )
	{
		super ();
		this.wantRawResult = wantRawResult;
		
		if ( wantRawResult ) {
			services = null; serviceCollections = null; repositories = null;
		}
		else {
			services = new HashSet<Service> ();
			serviceCollections = new HashSet<ServiceCollection> ();
			repositories =  new HashSet<Repository> ();
		}
	}

	@XmlElementWrapper ( name = "services" )
	@XmlElement ( name = "service" ) 
	public Set<Service> getServices ()
	{
		return services;
	}
		
	@XmlElementWrapper ( name = "service-collections" )
	@XmlElement ( name = "service-collection" ) 
	public Set<ServiceCollection> getServiceCollections ()
	{
		return serviceCollections;
	}
	
	@XmlElementWrapper ( name = "repositories" )
	@XmlElement ( name = "repository" ) 
	public Set<Repository> getRepositories ()
	{
		return repositories;
	}

	@XmlElementWrapper ( name = "bundles" )
	@XmlElement ( name = "bundle" )
	public Collection<Bundle> getBundles ()
	{
		return bundles.values ();
	}
	
	/**
	 * This adds the entity to the proper {@link Bundle} and, if not in raw-result mode, adds related {@link Service}s, 
	 * {@link ServiceCollection}s and {@link Repository repositories} to this result.
	 * 
	 */
	void addEntityMapping ( EntityMapping em ) 
	{
		String bundleId = em.getBundle ();
		Bundle bundle = bundles.get ( em.getBundle () );
		if ( bundle == null ) {
			bundles.put ( bundleId, bundle = new Bundle () );
		}
		bundle.addEntity ( em.getEntity () );
		Service service = em.getService ();
		if ( wantRawResult ) return;
		
		services.add ( service );
		ServiceCollection serviceCollection = service.getServiceCollection ();
		if ( serviceCollection != null ) serviceCollections.add ( serviceCollection );
		
		Repository repo = service.getRepository ();
		if ( repo != null ) repositories.add ( repo );
	}
	
	/**
	 * This is just a wrapper of {@link #addEntityMapping(EntityMapping)}.
	 */
	void addAllEntityMappings ( Collection<EntityMapping> mappings ) {
		for ( EntityMapping em: mappings ) addEntityMapping ( em );
	}

	@Override
	public String toString ()
	{
		StringBuilder sb = new StringBuilder ( "EntityMappingResult {\n" );
		
		if ( !wantRawResult )
		{
			sb.append ( "  services: {\n" );
			for ( Service service: services )
				sb.append ( "    " ).append ( service.toString () + "\n");
			sb.append ( "  }\n" );
			
			sb.append ( "  repositories: {\n" );
			for ( Repository repo: repositories )
				sb.append ( "    " ).append ( repo.toString () );
			sb.append ( "  }\n" );
	
			sb.append ( "  service-collections: {\n" );
			for ( ServiceCollection sc: serviceCollections )
				sb.append ( "    " ).append ( sc.toString () );
			sb.append ( "  }\n" );
		}

		for ( Bundle bundle: this.getBundles () )
		{
			sb.append ( "    {\n" );
			for ( Entity entity: bundle.getEntities () )
				sb.append ( "      " + entity.toString () + "\n" );
			sb.append ( "    }\n" );
		}
		sb.append ( "  }\n" );
		
		return sb.toString ();
	}
}
