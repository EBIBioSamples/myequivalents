package uk.ac.ebi.fg.myequivalents.provenance.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;

/**
 * Defines a JAXB wrapper for a collection of {@link ProvenanceRegisterEntry} elements.
 *
 * <dl><dt>date</dt><dd>3 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
@XmlRootElement ( name = "provenance" )
@XmlAccessorType ( XmlAccessType.NONE )
@XmlType ( name = "" )
public class ProvRegisterEntryList
{
	@XmlRootElement ( name = "provenance-entry-lists" )
	@XmlAccessorType ( XmlAccessType.NONE )
	@XmlType ( name = "" )
	public static class ProvRegisterEntryNestedList
	{		
		private List<ProvRegisterEntryList> entryLists;
		
		protected ProvRegisterEntryNestedList ()
		{
			this ( new ArrayList<List<ProvenanceRegisterEntry>> () );
		}

		
		public ProvRegisterEntryNestedList ( Collection<List<ProvenanceRegisterEntry>> entryLists )
		{
			super ();
			this.entryLists = new ArrayList<> ();
			for ( List<ProvenanceRegisterEntry> list: entryLists )
				this.entryLists.add ( new ProvRegisterEntryList ( list ) );
		}
		
		
		@XmlElement ( name = "entries" )
		public List<ProvRegisterEntryList> getEntryLists ()
		{
			return entryLists;
		}
		
		public Set<List<ProvenanceRegisterEntry>> getEntryListsUnwrapped ()
		{
			if ( this.entryLists == null ) return Collections.emptySet ();
			Set<List<ProvenanceRegisterEntry>> result = new HashSet<> ();
			for ( ProvRegisterEntryList el: this.entryLists )
				result.add ( el.getEntries () );
			return result;
		}
	}
	
	
	
	private List<ProvenanceRegisterEntry> entries;
	
	
	protected ProvRegisterEntryList ()
	{
		this ( new ArrayList<ProvenanceRegisterEntry> () );
	}

	
	public ProvRegisterEntryList ( List<ProvenanceRegisterEntry> entries )
	{
		super ();
		this.entries = entries;
	}

	@XmlElement ( name = "entry" )
	public List<ProvenanceRegisterEntry> getEntries ()
	{
		return entries;
	};
}
