package uk.ac.ebi.fg.myequivalents.rdf.java2rdf.mapping;

import static java.lang.String.format;
import static uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils.getNamespaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map.Entry;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import uk.ac.ebi.fg.java2rdf.mapping.urigen.RdfUriGenerator;
import uk.ac.ebi.fg.java2rdf.utils.NamespaceUtils;
import uk.ac.ebi.fg.java2rdf.utils.test.SparqlBasedTester;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingSearchResult.Bundle;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.test.TestModel;


/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Feb 2016</dd></dl>
 *
 */
public class MappingTest
{
	@Test
	public void testMockupModel () throws OWLOntologyStorageException, FileNotFoundException, OWLOntologyCreationException
	{
		TestModel tm = new TestModel ( "", "http://test.rdf.net/" );
		MyEqRdfMapperFactory.init ();
		
		OWLOntologyManager owlMgr = OWLManager.createOWLOntologyManager ();
		OWLOntology kb = owlMgr.createOntology ( IRI.create ( "http://test.rdf.net" ) );

		MyEqRdfMapperFactory mapFact = new MyEqRdfMapperFactory ( kb );
		
		for ( Bundle b: tm.mappings.getBundles () )
			mapFact.map ( b );
		
		// Save
		PrefixOWLOntologyFormat fmt = new TurtleOntologyFormat ();
		for ( Entry<String, String> nse: getNamespaces ().entrySet () )
			fmt.setPrefix ( nse.getKey (), nse.getValue () );
		String outPath = "target/mapping_test.ttl"; 
		owlMgr.saveOntology ( kb, fmt, new FileOutputStream ( new File ( outPath ) ));
		
		
		// Test
		SparqlBasedTester tester = new SparqlBasedTester ( outPath, NamespaceUtils.asSPARQLProlog () ); 
			
		tester.testRDFOutput ( "service1 not found!", 
			"ASK {\n"
			+ "  myeqres:service_" + tm.service1.getName () + " a myeq:Service;\n"
			+ "    dc-terms:identifier '" + tm.service1.getName () + "';\n"
			+ "    dc-terms:title '" + tm.service1.getTitle () + "';\n"
			+ "    dc-terms:description '" + tm.service1.getDescription () + "';\n"
			+ "    myeq:has-uri-pattern '" + tm.service1.getUriPattern () + "';\n"
			+ "    dc:type '" + tm.service1.getEntityType () + "';\n"
			+ "    myeq:has-repository myeqres:repo_" + tm.service1.getRepositoryName () + ";\n"
			+ "    myeq:has-service-collection myeqres:servcoll_" + tm.service1.getServiceCollectionName () + ".\n"
			+ "}\n"		
		);
		
		tester.testRDFOutput ( "service2 not found!", 
			"ASK {\n"
			+ "  myeqres:service_" + tm.service2.getName () + " a myeq:Service.\n"
			+ "}\n"		
		);

		tester.testRDFOutput ( "repo1 not found!", 
			"ASK {\n"
			+ "  myeqres:repo_" + tm.repo1.getName () + " a myeq:Repository;\n"
			+ "    dc-terms:identifier '" + tm.repo1.getName () + "';\n"
			+ "    dc-terms:title '" + tm.repo1.getTitle () + "';\n"
			+ "    dc-terms:description '" + tm.repo1.getDescription () + "'.\n"
			+ "}\n"		
		);
		
		tester.testRDFOutput ( "sc1 not found!", 
			"ASK {\n"
			+ "  myeqres:servcoll_" + tm.sc1.getName () + " a myeq:ServiceCollection;\n"
			+ "    dc-terms:identifier '" + tm.sc1.getName () + "';\n"
			+ "    dc-terms:title '" + tm.sc1.getTitle () + "';\n"
			+ "    dc-terms:description '" + tm.sc1.getDescription () + "'.\n"
			+ "}\n"		
		);
		
		
		// Test the mappings 
		RdfUriGenerator<Entity> eUriGen = mapFact.getRdfUriGenerator ( Entity.class );
		
		for ( Bundle bundle: tm.mappings.getBundles () )
		{
			Entity[] es = bundle.getEntities ().toArray ( new Entity [ 0 ] );
			
			for ( int i = 0; i < es.length; i++ )
			{
				Entity ei = es [ i ];
				String urii = eUriGen.getUri ( ei );
				
				tester.testRDFOutput ( "Entity <" + urii + "> not found!", 
					"ASK {\n"
							+ "  <" + urii + "> a myeq:Entity;\n"
							+ "    dc-terms:identifier '" + ei.getAccession () + "';\n"
							+ "    myeq:has-service myeqres:service_" + ei.getServiceName () + ".\n"
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
				}
			}
		}
	
	}
}
