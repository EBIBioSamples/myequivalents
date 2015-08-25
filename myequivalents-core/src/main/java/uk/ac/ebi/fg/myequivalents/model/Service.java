package uk.ac.ebi.fg.myequivalents.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import static uk.ac.ebi.fg.myequivalents.resources.Const.*;


/**
 *
 * <p/>A service is something that to which entities of a given type, identified by accessions, can be associated. For instance,
 * <ul>
 *   <li>the 'ArrayExpress experiment service' would provide with experiment entities, given experiment's accession</li>
 *   <li>the 'AE array design service' would provide with array design records</li>
 *   <li>The PUBMED service would provide with records about scientific publications, given accessions in the form of 
 *       PUBMED IDs</li>
 *   <li>The GeneOntology service would provide with GO terms, given term accessions</li>
 *   <li>The Wikipedia service yields Wikipedia pages, starting from their identifiers or URLs/URIs</li>
 *   <li>The DBPedia service, would use the same accessions that are used for Wikipedia and would return RDF 
 *   representations of the same Wikipedia pages, instead of plain HTML</li> 
 * </ul>
 * 
 * From the examples above we can note the following.
 * <ul>
 *   <li>Services can be associated to {@link Repository repositories}, i.e., set of 
 * services provided by the same organisational unit (a web site, a database etc, see the AE examples).</li>
 *   <li>There are repositories that provides just one service (the GO example above). In such a case, they repository 
 *   name would be enough to identify its sole provided service. While we don't explicitly provide (yet?) a mechanism to go 
 *   from a repository to its service when it is unique (due to simplicity concerns), a simple way to achieve this behaviour 
 *   is to assign the same name to both the repository and the service in such a case. We also recommend that repositories
 *   and services are named consistently, e.g. all the services provided by repository1 should be named something like
 *   repository1.serviceX</li>     
 *   <li>Entities can be returned by a service either by means of accessions or by means of URIs, accessions/URIs are 
 * mapped one each other one-to-one (see {@link Entity}).</li>
 *   <li>A service returns entities of the same type (see {@link #getEntityType()}.</li>
 *   <li>A service is equivalent to a <a href = 'http://www.ebi.ac.uk/miriam/main/'>MIRAM</a> resource, we renamed them
 *   in order to avoid confusion with RDF resources (which are equivalent to our entities).</li>
 *   <li>There are certain services that uses the same accessions to return different entities (i.e., different information 
 *   for the same real-world entities). This is the case for the Wikipedia and DPPedia examples above. These services can 
 *   be grouped under {@link ServiceCollection}s, which allows you to obtain multiple entities starting from a single one
 *   (e.g., you start from a Wikipedia page, you'll get DBPedia pages as {@link EntityMapping}s). 
 *   MIRAM's data collections will be used to obtain such mappings within 
 *   service collections.</li>
 * </ul>
 * 
 * <dl><dt>date</dt><dd>Jun 20, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@Table( name = "service" )
@org.hibernate.annotations.Table ( 
	appliesTo = "service", 
	indexes = {
		@Index ( name = "service_t", columnNames = "title" ),
		@Index ( name = "service_pub_flag", columnNames = "public_flag" ), // TODO: needed?! Do we need a combined one?
		@Index ( name = "service_rel_date", columnNames = "release_date" )
	}
)
@XmlRootElement ( name = "service" )
@XmlAccessorType ( XmlAccessType.NONE )
@NamedQueries 
({
	@NamedQuery ( name = "service.findByName.publicOnly", query = 
		"SELECT s FROM uk.ac.ebi.fg.myequivalents.model.Service s LEFT JOIN s.repository r WHERE s.name = :serviceName\n" +
		"AND " + Service.HQL_IS_PUBLIC_CLAUSE
	),
		
	@NamedQuery ( name = "service.findByName", query = 
		"FROM uk.ac.ebi.fg.myequivalents.model.Service WHERE name = :serviceName"
	),
	
	@NamedQuery ( name = "service.findByUriPattern.publicOnly", query = 
		"SELECT s FROM uk.ac.ebi.fg.myequivalents.model.Service s LEFT JOIN s.repository r WHERE s.uriPattern = :uriPattern\n" +
		"AND " + Service.HQL_IS_PUBLIC_CLAUSE
	),
 
	@NamedQuery ( name = "service.findByUriPattern", query = 
 		"FROM uk.ac.ebi.fg.myequivalents.model.Service WHERE uriPattern = :uriPattern" 
	),

	@NamedQuery ( name = "service.findByUriPattern.like.publicOnly", query = 
		"SELECT s FROM uk.ac.ebi.fg.myequivalents.model.Service s LEFT JOIN s.repository r WHERE s.uriPattern LIKE :uriPattern\n" +
		"AND " + Service.HQL_IS_PUBLIC_CLAUSE
	),
	
	@NamedQuery ( name = "service.findByUriPattern.like", query = 
		"FROM uk.ac.ebi.fg.myequivalents.model.Service WHERE uriPattern LIKE :uriPattern" 
	)
})
public class Service extends Describeable
{
	@Column ( name = "entity_type", length = COL_LENGTH_S )
	@Index( name = "service_et" )
	private String entityType;

	@Column ( name = "uri_pattern", length = COL_LENGTH_URIS )
	@Index( name = "service_uri_pat" )
	private String uriPattern;
	
	@ManyToOne( cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE } )
	@LazyToOne ( LazyToOneOption.PROXY )
	@JoinColumn( name = "repository_name" )
	@Index( name = "service_r" )
	private Repository repository;
	
	@ManyToOne( cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE } )
	@LazyToOne ( LazyToOneOption.PROXY )
	@JoinColumn( name = "service_collection_name" )
	@Index( name = "service_c" )
	private ServiceCollection serviceCollection;

	public static final String UNSPECIFIED_SERVICE_NAME = "_";
	public static final Service UNSPECIFIED_SERVICE;
	
	static final String HQL_IS_PUBLIC_CLAUSE = 
		"(\n" +
    // The service has some specific visibility attribute
		"	  ( s.publicFlag = true OR s.publicFlag IS NULL AND s.releaseDate IS NOT NULL AND s.releaseDate <= current_time() )\n" +
    // if the service has nothing, check its repo has something
		"	  OR (\n" +
		"	    r IS NOT NULL AND s.publicFlag IS NULL AND s.releaseDate IS NULL\n" +
		"	    AND ( r.publicFlag = true OR r.publicFlag IS NULL AND ( r.releaseDate IS NULL OR r.releaseDate <= current_time() ))\n" +
		"	  )\n" +
		"	)"; 
	
	static 
	{
		UNSPECIFIED_SERVICE = new Service ( 
			UNSPECIFIED_SERVICE_NAME, null, "[Unspecified Service]", 
			"This is a fictitious service, used to represent those entities that are identified by means of URIs " +
			"(or other forms of universal identifiers)."
		);
		
		UNSPECIFIED_SERVICE.setUriPattern ( "$id" );
		UNSPECIFIED_SERVICE.setPublicFlag ( true );
	}
	
	protected Service () {
		super();
	}
	
	
	public Service ( String name )
	{
		super ( name );
	}

	public Service ( String name, String entityType )
	{
		super ( name );
		this.setEntityType ( entityType );
	}

	public Service ( String name, String entityType, String title, String description )
	{
		super ( name, title, description );
		this.setEntityType ( entityType );
	}

	@XmlAttribute ( name = "entity-type" )
	public String getEntityType ()
	{
		return entityType;
	}

	protected void setEntityType ( String entityType )
	{
		this.entityType = entityType;
	}

	@XmlAttribute ( name = "uri-pattern" )
	public String getUriPattern ()
	{
		return uriPattern;
	}

	public void setUriPattern ( String uriPattern )
	{
		this.uriPattern = uriPattern;
	}

	public Repository getRepository ()
	{
		return repository;
	}

	public void setRepository ( Repository repository )
	{
		this.repository = repository;
	}

	public ServiceCollection getServiceCollection ()
	{
		return serviceCollection;
	}

	public void setServiceCollection ( ServiceCollection serviceCollection )
	{
		this.serviceCollection = serviceCollection;
	}

	@Transient
	@XmlAttribute ( name = "repository-name" )
	public String getRepositoryName () {
		return getRepository () == null ? null : this.repository.getName (); 
	}

  /**
   * This is used for extensions that have to pass through the JAXB stack, default returns an exception.
   */
	protected void setRepositoryName ( String repositoryName ) 
	{
		throw new UnsupportedOperationException ( 
			"Internal error: you cannot call Service.setRepositoryName(), this is here just to make JAXB annotations working. " +
			"You need to override this setter, if you have a reasonable semantics for it" );
	}
	
	@Transient
	@XmlAttribute ( name = "service-collection-name" )
	public String getServiceCollectionName () {
		return getServiceCollection () == null ? null : this.serviceCollection.getName ();  
	}

  /**
   * This is used for extensions that have to pass through the JAXB stack, default returns an exception.
   */
	protected void setServiceCollectionName ( String serviceCollectionName ) 
	{
		throw new UnsupportedOperationException ( 
			"Internal error: you cannot call Service.setServiceCollectionName(), this is here just to make JAXB annotations working. " +
			"You need to override this setter, if you have a reasonable semantics for it" );
	}

	@Override
	public String toString ()
	{
		return String.format ( 
			"Service { name: '%s', title: '%s', entity-type: '%s', uri-pattern: '%s', " +
			"description: '%.15s', service-collection: '%s', repository: '%s', public-flag: %s, release-date: %s }", 
			this.getName (), this.getTitle (), this.getEntityType (), this.getUriPattern (),
			this.getDescription (), this.getServiceCollectionName (), this.getRepositoryName (),
			this.getPublicFlag (), this.getReleaseDate ()
		);
	}
	
	/** 
	 * Applies cascading rules to the service's repository, i.e., if the service has no visibility attribute (both 
	 * {@link #getPublicFlag()} and {@link #getReleaseDate()} are null), checks if {@link #getRepository()}.{@link Repository#isPublic()}.
	 */
	@Override
	public boolean isPublic ()
	{
		if ( this.getPublicFlag () == null && this.getReleaseDate () == null )
			return this.getRepository () == null ? false : this.repository.isPublic ();
		
		return super.isPublic ();
	}

}
