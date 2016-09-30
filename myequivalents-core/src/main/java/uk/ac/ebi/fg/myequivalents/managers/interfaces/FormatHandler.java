package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.SortedSet;
import java.util.stream.Stream;

import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Sep 2016</dd></dl>
 *
 */
public interface FormatHandler
{	
	public SortedSet<String> getShortTypes ();
	public SortedSet<String> getContentTypes ();
	public int serialize ( Stream<MyEquivalentsModelMember> in, OutputStream out );
	public Stream<MyEquivalentsModelMember> read ( InputStream in );
	
	public static <H extends FormatHandler> H of ( String typeTag, boolean failOnNoMatch ) {
		return FormatHandlerFactory.of ( typeTag, failOnNoMatch );
	}

	public static <H extends FormatHandler> H of ( String typeTag ) {
		return FormatHandlerFactory.of ( typeTag );
	}

}
