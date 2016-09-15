package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * The backup manager allows for dumping myEquivalents data onto a data document (e.g., XML), or to restore them from 
 * a document, a supported format. As usually, data are dumped from (restored to) the current backend (eg, relational 
 * database, or web services). This interface defines the dump destination in terms of generic Java streams, so that 
 * implementations and clients can define more specific destinations. See the documentation for details.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>20 Feb 2015</dd>
 *
 */
public interface BackupManager extends MyEquivalentsManager
{
	public int dump ( OutputStream out, Integer offset, Integer limit );
	public int upload ( InputStream in );
}
