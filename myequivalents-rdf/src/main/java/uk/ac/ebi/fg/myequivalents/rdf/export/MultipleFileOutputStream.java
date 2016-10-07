package uk.ac.ebi.fg.myequivalents.rdf.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Sep 2016</dd></dl>
 *
 */
public class MultipleFileOutputStream extends OutputStream
{
	private final File file;
	private OutputStream out;
	private int count = -1;
	
	public MultipleFileOutputStream ( File file, boolean append ) throws FileNotFoundException
	{
		this.out = new BufferedOutputStream ( new FileOutputStream ( file, append ) );
		this.file = file;
	}

	public MultipleFileOutputStream ( File file ) throws FileNotFoundException
	{
		this ( file, false );
	}

	public MultipleFileOutputStream ( String name, boolean append ) throws FileNotFoundException
	{
		this ( new File ( name ), append );
	}

	public MultipleFileOutputStream ( String name ) throws FileNotFoundException
	{
		this ( name, false );
	}
	
	public int nextFile () throws IOException
	{
		if ( ++count == 0 ) return 0; // first time, we keep this 
		
		String fpath = this.file.getCanonicalPath ();
		int idot = FilenameUtils.indexOfExtension ( fpath );
		fpath = idot == -1 
			? fpath + "_" + count 
			: fpath.substring ( 0, idot ) + "_" + count + fpath.substring ( idot );
		
		this.out = new BufferedOutputStream ( new FileOutputStream ( fpath ) );
		return count;
	}

	
	public void write ( int b ) throws IOException {
		out.write ( b );
	}

	public void write ( byte[] b ) throws IOException {
		out.write ( b );
	}

	public void write ( byte[] b, int off, int len ) throws IOException {
		out.write ( b, off, len );
	}

	public void flush () throws IOException {
		out.flush ();
	}

	public void close () throws IOException {
		out.close ();
	}
	
}
