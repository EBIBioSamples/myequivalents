package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.SortedSet;
import java.util.stream.Stream;

import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;

/**
 * A format handler is able to serialise myEq data to some format and load data from such format. 
 * This is used by {@link BackupManager} to import/export myEq data. This kind of arrangement allows for
 * separation of the core I/O operations from those about (un)serialisation.
 * 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Sep 2016</dd></dl>
 *
 */
public interface FormatHandler
{	
	/**
	 * Tags to identify the format (e.g., rdf, xml).
	 */
	public SortedSet<String> getShortTypes ();
	
	/**
	 * <a href = "https://en.wikipedia.org/wiki/Media_type">MIME types</a> associated to this format to be used for 
	 * content negotiation.
	 */
	public SortedSet<String> getContentTypes ();
	
	/**
	 * Takes a stream of myEquivalents objects (services, repository, mapping bundles) and serialise them
	 * using the format supported by the handler implementation.
	 * 
	 * @return a count of the processed objects. 
	 */
	public int serialize ( Stream<MyEquivalentsModelMember> in, OutputStream out );
	
	/**
	 * Reads myEq objects from an input stream that is assumed to be formatted according to the format supported by
	 * the handler.
	 */
	public Stream<MyEquivalentsModelMember> read ( InputStream in );

	/**
	 * This is a wrapper of {@link FormatHandlerFactory}.
	 */
	public static <H extends FormatHandler> H of ( String typeTag, boolean failOnNoMatch ) {
		return FormatHandlerFactory.of ( typeTag, failOnNoMatch );
	}

	/**
	 * See {@link FormatHandlerFactory}.
	 */
	public static <H extends FormatHandler> H of ( String typeTag ) {
		return FormatHandlerFactory.of ( typeTag );
	}

}
