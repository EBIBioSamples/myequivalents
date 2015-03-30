package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO: comment me!
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
