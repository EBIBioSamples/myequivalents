package uk.ac.ebi.fg.myequivalents.rdf.export;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.java2rdf.mapping.urigen.RdfUriGenerator;
import uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils;
import uk.ac.ebi.fg.java2rdf.utils.test.SparqlBasedTester;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.BackupManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.FormatHandler;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ServiceManager;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;
import uk.ac.ebi.fg.myequivalents.model.Repository;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping.MyEqRdfMapperFactory;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fg.myequivalents.test.MappingsGenerator;


/**
 * Tests for RDF exporting.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Feb 2016</dd></dl>
 *
 */
public class ExporterTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Rule
	public MappingsGenerator mapGenerator = new MappingsGenerator (){{
		// mapGenerator.nmappingBundles = 2000;
		// mapGenerator.maxMappingId = Integer.MAX_VALUE - 1;		
	}};

	
	private DbManagerFactory mgrFact = Resources.getInstance ().getMyEqManagerFactory ();	
	private BackupManager bkpMgr;
	
	
	@Before
	public void init () 
	{
		bkpMgr = mgrFact.newBackupManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET
		);		
	}

	@Test
	public void testBasics () throws IOException 
	{
		ServiceManager servMgr = mgrFact.newServiceManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET 
		);
		
		EntityMappingManager mapMgr = mgrFact.newEntityMappingManager ( 
			MappingsGenerator.ADMIN_USER.getEmail (), MappingsGenerator.ADMIN_SECRET 
		);

		servMgr.storeServices ( Service.UNSPECIFIED_SERVICE );
		servMgr.close ();

		mapMgr.storeMappingBundle ( ":<http://a.test.uri.1>", ":<http://a.test.uri.2>" );
		mapMgr.close ();
		
		
		// We'll need them later to verify
		List<MyEquivalentsModelMember> generatedElems = 
			bkpMgr.dump ( null, null ).collect ( Collectors.toCollection ( LinkedList::new  ) );
		
		String outPath = "target/dump.ttl"; 
		OutputStream out = new FileOutputStream ( new File ( outPath) );
		
		// Here we go
		bkpMgr.dump ( out, FormatHandler.of ( "ttl" ), null, null );
		out.close ();

		
		// Check the XML with SPARQL
		MyEqRdfMapperFactory mapFact = new MyEqRdfMapperFactory ( null );
		RdfUriGenerator<Entity> eUriGen = mapFact.getRdfUriGenerator ( Entity.class );
		SparqlBasedTester tester = new SparqlBasedTester ( outPath, NamespaceUtils.asSPARQLProlog () ); 

		
		// They should be all in the XML
		//
		generatedElems
		.stream()
		.filter ( elem -> elem instanceof Bundle )
		.forEach ( elem -> 
		{
			Bundle bundle = (Bundle) elem;
			Entity[] es = bundle.getEntities ().toArray ( new Entity [ 0 ] );
			
			for ( int i = 0; i < es.length; i++ )
			{
				Entity ei = es [ i ];
				String urii = eUriGen.getUri ( ei );
				
				tester.testRDFOutput ( "Entity <" + urii + "> not found!", 
					"ASK {\n"
							+ "  <" + urii + "> a myeq:Entity;\n"
							+ "    dc-terms:identifier '" + ei.getAccession () + "';\n"
							+ "    myeq:has-service myeqres:service:" + ei.getServiceName () + ".\n"
							+ "}\n"		
				);
				
				for ( int j = i + 1; j < es.length; j++ )
				{
					Entity ej = es [ j ];
					String urij = eUriGen.getUri ( ej );
					
					tester.testRDFOutput ( "Mapping ( <" + urii + ">, <" + urij + "> not found!",  
						"ASK {\n"
						+ format ( 
								// Use this when your engine doesn't support property paths
							  // "  { %1$s owl:sameAs %2$s } UNION { %2$s owl:sameAs %1$s } UNION { ?x owl:sameAs %1$s, %2$s }\n",
								"%s (owl:sameAs|^owl:sameAs){1,2} %s",
							  '<' + urii + '>', '<' + urij + '>' 
							) 
						+ "}\n"		
					);
				} // for entity j
			} // for entity i 	
		}); // forEach bundle
		
		
		// Same for mapping containers
		generatedElems
		.stream()
		.filter ( elem -> elem instanceof Describeable )
		.forEach ( elem -> 
		{
			Describeable d = (Describeable) elem;
			String idPrefx, owlClassId, additionalSparql;
			
			if ( d instanceof Repository ) {
				idPrefx = "repo"; owlClassId = "Repository"; additionalSparql = "";
			}
			else if ( d instanceof ServiceCollection ) {
				idPrefx = "servcoll"; owlClassId = "ServiceCollection"; additionalSparql = "";				
			}
			else if ( d instanceof Service )
			{
				Service s = (Service) d;
				idPrefx = "service"; owlClassId = "Service";
				
				additionalSparql = Optional
				.ofNullable ( s.getUriPattern () )
				.map ( uriPattern -> "    myeq:has-uri-pattern '" + uriPattern + "';\n" )
				.orElse ( "" );
				
				additionalSparql += Optional
				.ofNullable ( s.getEntityType () )
				.map ( etype -> "    dc:type '" + etype + "';\n" )
				.orElse ( "" ); 
				
				additionalSparql += Optional
				.ofNullable ( s.getRepositoryName () )
				.map ( repoName -> "    myeq:has-repository myeqres:repo:" + repoName + ";\n" )
				.orElse ( "" ); 

				additionalSparql += Optional
				.ofNullable ( s.getServiceCollectionName () )
				.map ( scName -> "    myeq:has-service-collection myeqres:servcoll:" + scName + ".\n" )
				.orElse ( "" ); 

			}
			else
				throw new RuntimeException ( "Don't know how to deal with type " + d.getClass ().getSimpleName () );
			
			String askSparql =
				"ASK {\n"
						+ "  myeqres:" + idPrefx + ":" + d.getName () + " a myeq:" + owlClassId + ";\n"
						+ "    dc-terms:identifier '" + d.getName () + "';\n"
						+ "    dc-terms:title '" + d.getTitle () + "';\n"
						+ "    dc-terms:description '" + d.getDescription () + "';\n"
						+ additionalSparql
						+ "}\n";		
			
			//log.debug ( "The SPARQL ASK: \n{}", askSparql );
			tester.testRDFOutput ( owlClassId + " " + d.getName () +  " not found!", askSparql );
			
		});	// forEach Describeable
	} // testBasics ()
}
