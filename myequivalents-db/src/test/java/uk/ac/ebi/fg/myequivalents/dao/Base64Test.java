package uk.ac.ebi.fg.myequivalents.dao;

import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

/**
 * Just a foo test to verify that the BASE 64 encoding of 20 bytes yield always 28 characters of which the last one is 
 * always a padding '='.
 *
 * <dl><dt>date</dt><dd>May 26, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class Base64Test
{
	//@Test
	public void testBase64Shape () throws NoSuchAlgorithmException
	{
		final Random random = new Random ( System.currentTimeMillis () );
		MessageDigest digest = MessageDigest.getInstance ( "SHA1" );
		
		for ( int i = 0; i < 1000; i++ )
		{
			byte b[] = new byte [ random.nextInt ( 256 ) ];
			random.nextBytes ( b );
			byte[] hash = digest.digest ( b ); 
			
			String enc = DatatypeConverter.printBase64Binary ( hash );
			out.printf ( "  %s -> %s\n", DatatypeConverter.printHexBinary ( hash ), enc );

			assertEquals ( "Unexpected length for hash!", 20, hash.length );
			assertEquals ( "Unexpected length!", 28, enc.length () );
			assertEquals ( "Last char is not '='!", '=', enc.charAt ( enc.length () - 1 ) );
		}
		out.println ( "As expected!" );
	}
	
}
