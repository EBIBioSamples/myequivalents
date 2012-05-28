package uk.ac.ebi.fg.myequivalents.dao;

import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import static junit.framework.Assert.*;
import static java.lang.System.out;

/**
 * Just a foo test to verify that the BASE 64 encoding of 20 bytes yield always 28 characters of which the last one is 
 * always a padding '='.
 *
 * <dl><dt>date</dt><dd>May 26, 2012</dd></dl>
 * @author brandizi
 *
 */
public class Base64Test
{
	//@Test
	public void testBase64Shape ()
	{
		final Random random = new Random ( System.currentTimeMillis () );

		for ( int i = 0; i < 1000; i++ )
		{
			byte b[] = new byte [ 20 ];
			random.nextBytes ( b );
			String enc = Base64.encodeBase64String ( b );
			out.printf ( "  %s -> %s\n", Hex.encodeHexString ( b ).toUpperCase (), enc );
			
			assertEquals ( "Unexpected length!", 28, enc.length () );
			assertEquals ( "Last char is not '='!", '=', enc.charAt ( enc.length () -1 ) );
		}
		out.println ( "As expected!" );
	}
	
}
