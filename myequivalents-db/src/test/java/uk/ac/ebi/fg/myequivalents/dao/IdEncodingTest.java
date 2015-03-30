package uk.ac.ebi.fg.myequivalents.dao;

import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * A few foo tests about ID enconding options. 
 * 
 * <dl><dt>date</dt><dd>May 26, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class IdEncodingTest
{
	/**
	 * Just a foo test to verify that the BASE 64 encoding of 20 bytes yield always 28 characters of which the last one is 
	 * always a padding '='.
	 * 
	 */
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
			assertEquals ( "Unexpected length for encoding!", 28, enc.length () );
			assertEquals ( "Last char is not '='!", '=', enc.charAt ( enc.length () - 1 ) );
		}
		out.println ( "As expected!" );
	}
	
	//@Test
	public void testUUIDShape ()
	{
		for ( int i = 0; i < 1000; i++ )
		{
			UUID uuid = UUID.randomUUID ();
			String hashStr = uuid.toString ();
			byte[] hash = hashStr.getBytes ();
						
			String enc = DatatypeConverter.printBase64Binary ( hash );
			String hexEnc = DatatypeConverter.printHexBinary ( hash );

			ByteBuffer buf = ByteBuffer.allocate ( 2 * Long.SIZE / 8);
			buf.putLong ( uuid.getMostSignificantBits () );
			buf.putLong ( uuid.getLeastSignificantBits () );
			byte[] shortHash = buf.array ();
			String shortHashB64 = DatatypeConverter.printBase64Binary ( shortHash );
			String shortHashHex = DatatypeConverter.printHexBinary ( shortHash );
			buf.clear ();
			
			
			out.printf (
				  "  string: %s (%d)\n  bytes: %s (%d)\n  hex-bytes: %s (%d)\n"
				+ "  short-hash-b64: %s (%d)\n  short-hash-hex: %s (%d)\n\n", 
				hashStr, hashStr.length (), 
				hexEnc, hexEnc.length (), 
				enc, enc.length (), 
				shortHashB64, shortHashB64.length (),
				shortHashHex, shortHashHex.length ()
			);
			
			assertEquals ( "Base64 encoding of UUID is of unexpected length!", 24, shortHashB64.length () );
			assertEquals ( "Base64 encoding of UUID hasn't '==' at the end!", "==", StringUtils.substring ( shortHashB64, -2 ) );
		}
		out.println ( "As expected!" );
	}

}
