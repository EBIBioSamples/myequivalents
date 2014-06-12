package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.util.ArrayList;
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
 * This is the class used to format the responses returned by the {@link EntityMappingManager}. For instance, the REST-based
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
	@XmlAccessorType ( XmlAccessType.NONE )
	public static class Bundle
	{
		protected Bundle () {
		}
		
		private Set<Entity> entities = new HashSet<Entity> ();

		private void addEntity ( Entity entity ) {
			this.entities.add ( entity );
		}

		@XmlElement ( name = "entity", type = ExposedEntity.class )
		public Set<Entity> getEntities () {
			return entities;
		}

		protected void setEntities ( Set<Entity> entities ) {
			this.entities = entities;
		}
		
	}
	
	private final boolean wantRawResult;
		
	private Set<Service> services;
	private Set<ServiceCollection> serviceCollections;
	private Set<Repository> repositories;

	private final Map<String, Bundle> bundles = new HashMap<String, Bundle> ();
	
	
	EntityMappingSearchResult ()
	{
		this ( false );
	}

	/**
	 * @param wantRawResult if true, only bundles will be stored into this object. See 
	 * {@link DbEntityMappingManager#getMappings(boolean, String...)}.
	 * 
	 * Usually you don't want to instantiate this yourself, you should leave it to the {@link EntityMappingManager} you're
	 * using.
	 */
	public EntityMappingSearchResult ( boolean wantRawResult )
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
	@XmlElement ( name = "service", type = ExposedService.class ) 
	public Set<Service> getServices ()
	{
		return services;
	}
		
	/** Needed by JAXB for un-marshalling */
	protected void setServices ( Set<Service> services ) {
		this.services = services;
	}

	
	@XmlElementWrapper ( name = "service-collections" )
	@XmlElement ( name = "service-collection" ) 
	public Set<ServiceCollection> getServiceCollections ()
	{
		return serviceCollections;
	}
	
	/** Needed by JAXB for un-marshalling */
	protected void setServiceCollections ( Set<ServiceCollection> serviceCollections ) {
		this.serviceCollections = serviceCollections;
	}

	
	@XmlElementWrapper ( name = "repositories" )
	@XmlElement ( name = "repository" ) 
	public Set<Repository> getRepositories ()
	{
		return repositories;
	}

	/** Needed by JAXB for unmarshalling */
	protected void setRepositories ( Set<Repository> repositories ) {
		this.repositories = repositories;
	}

	
	@XmlElementWrapper ( name = "bundles" )
	@XmlElement ( name = "bundle" )
	public Collection<Bundle> getBundles ()
	{
		return new ArrayList<EntityMappingSearchResult.Bundle> ( this.bundles.values () );
	}
	
	/** Needed by JAXB for un-marshalling */
	protected void setBundles ( Collection<Bundle> bundles ) 
	{
		for ( Bundle bundle: bundles )
			this.bundles.put ( Integer.toString ( this.bundles.size () ), bundle );
	}
	
	/**
	 * This adds the entity to the proper {@link Bundle} and, if not in raw-result mode, adds related {@link Service}s, 
	 * {@link ServiceCollection}s and {@link Repository repositories} to this result.
	 * 
	 */
	private void addEntityMapping ( EntityMapping em ) 
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
	 * Usually you don't want to use this method, it is intended to be used by the {@link EntityMappingManager} that serves
	 * this result.
	 * 
	 */
	public void addAllEntityMappings ( Collection<EntityMapping> mappings ) {
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

		sb.append ( "  bundles: {\n" );
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
