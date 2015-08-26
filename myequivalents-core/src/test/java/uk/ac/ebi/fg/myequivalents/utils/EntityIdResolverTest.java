package uk.ac.ebi.fg.myequivalents.utils;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.ac.ebi.fg.myequivalents.model.Service.UNSPECIFIED_SERVICE_NAME;
import static uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver.breakUri;
import static uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver.buildUriFromAcc;
import static uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver.extractAccession;
import static uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver.getDomain;

import org.junit.Test;

import uk.ac.ebi.fg.myequivalents.model.EntityId;

/**
 * Some tests for {@link EntityIdResolver}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Jun 2015</dd>
 *
 */
public class EntityIdResolverTest
{
	@Test
	public void testParseNonUris ()
	{
		EntityIdResolver res = new EntityIdResolver ();
		
		String eids[][] = {
			{ "service", "acc" }, { "service\\:1", "acc" }, { "service::1", "acc" }, { "service", "acc:1" }
		};
		
		for ( String eid[]: eids )
		{
			String serviceName = eid [ 0 ], acc = eid [ 1 ];
			EntityId oeid = res.parse ( serviceName + ':' + acc );
			assertEquals ( "parse() didn't work (serviceName)!", oeid.getServiceName (), serviceName );
			assertEquals ( "parse() didn't work (acc)!", oeid.getAcc (), acc );
		}

		
		catchException ( res ).parse ( "service1_acc2" );
		assertNotNull (  "Should have a parse error here!", caughtException () );
	}
	
	@Test
	public void testParseUris ()
	{
		EntityIdResolver res = new EntityIdResolver ();
		
		String uri = "http://www.some.foo.test/acc1";
		EntityId eid = res.parse ( '<' + uri + '>' );
		
		assertEquals ( "URI not parsed!", uri, eid.getUri () );
		assertNull ( "service != null !", eid.getServiceName () );
		assertNull ( "acc != null !", eid.getAcc () );
	
		
		String sname = "serv1";
		String eidStr = sname + ":<" + uri + ">";
		eid = res.parse ( eidStr );

		assertEquals ( "URI not parsed!", uri, eid.getUri () );
		assertEquals ( "service not parsed!", sname, eid.getServiceName () );
		assertNull ( "acc != null !", eid.getAcc () );

		
		eidStr = ":<" + uri + ">";
		eid = res.parse ( eidStr );

		assertEquals ( "URI not parsed!", uri, eid.getUri () );
		assertEquals ( "service not parsed!", UNSPECIFIED_SERVICE_NAME, eid.getServiceName () );
		assertNull ( "acc != null !", eid.getAcc () );
	}
	
	@Test
	public void testBreakUri ()
	{
		String dom = "http://www.somewhere.net";
		String ns = dom + "/path/to/";
		
		assertEquals ( "beakUri() wrong!", ns + "$id", breakUri ( "123", ns + "123" ) );
		assertEquals ( "beakUri() wrong!", ns + "$id", breakUri ( ns + "123" ) );
		
		
		ns = "http://www.somewhere.net/path/to/#";
		assertEquals ( "beakUri() wrong!", ns + "$id", breakUri ( ns + "123" ) );
		
		assertEquals ( "getDomain() wrong!", dom, getDomain ( ns + "123" ) );
	}
	
	@Test
	public void testBuildUri ()
	{
		String dom = "http://www.somewhere.net";
		String ns = dom + "/path/to/#";
		String utpl = ns + "$id";
		
		assertEquals ( "buildUriFromAcc() didn't work!", ns + "123", buildUriFromAcc ( "123", utpl ) );		
	}
	
	@Test
	public void testExtractAccession ()
	{
		String testCases[][] = new String [][] {
			{ "http://www.somewhere.net/path/to/#FOO_123", "http://www.somewhere.net/path/to/#$id", "FOO_123" },
			{ "http://www.somewhere.net/path/to/#123?format=rdf", "http://www.somewhere.net/path/to/#$id?format=rdf", "123" }
		};
		
		for ( String testCase[]: testCases )
			assertEquals ( 
				"testExtractAccession() didn't work!",
				testCase [ 2 ],
				extractAccession ( testCase [ 0 ], testCase [ 1 ] ) 
		);
	}
}
