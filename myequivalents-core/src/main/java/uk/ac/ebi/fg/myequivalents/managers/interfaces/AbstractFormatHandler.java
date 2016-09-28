package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Sep 2016</dd></dl>
 *
 */
public abstract class AbstractFormatHandler implements FormatHandler
{
	protected final SortedSet<String> SHORT_TYPES;
	protected final SortedSet<String> CONTENT_TYPES; 

	protected AbstractFormatHandler ( String[] shortTypes, String[] contentTypes )
	{
		SHORT_TYPES = Collections.unmodifiableSortedSet ( 
			Stream
			.of ( shortTypes )
			.collect ( Collectors.toCollection ( TreeSet::new ) )
		);
		CONTENT_TYPES = Collections.unmodifiableSortedSet ( 
			Stream
			.of ( contentTypes )
			.collect ( Collectors.toCollection ( TreeSet::new ) )
		);				
	}
	
	@Override
	public SortedSet<String> getShortTypes ()
	{
		return this.SHORT_TYPES;
	}
	
	
	@Override
	public SortedSet<String> getContentTypes ()
	{
		return this.CONTENT_TYPES;
	}	
}
