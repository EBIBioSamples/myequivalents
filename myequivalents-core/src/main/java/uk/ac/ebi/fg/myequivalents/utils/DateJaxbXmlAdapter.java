package uk.ac.ebi.fg.myequivalents.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.StringUtils;

/**
 * Adapt JAXB to our desired date formats. Many thanks to 
 * <a href = 'http://stackoverflow.com/questions/13568543/how-do-you-specify-the-date-format-used-when-jaxb-marshals-xsddatetime'>this post</a>.
 *
 * <dl><dt>date</dt><dd>May 24, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DateJaxbXmlAdapter extends XmlAdapter<String, Date> 
{

	/**
	 * All the elements are used for parsing, the first element is used for output.
	 */
	public static final DateFormat DATE_FMTS[] =  new DateFormat[] { 
		new SimpleDateFormat ( "yyyyMMdd'-'HHmmss" ), 
		new SimpleDateFormat ( "yyyyMMdd" )
	};

  @Override
  public String marshal ( Date d )
  {
  	return d == null ? null : DATE_FMTS [ 0 ].format ( d );
  }

  @Override
  public Date unmarshal ( String ds )
  {
  	if ( ( ds = StringUtils.trimToNull ( ds ) ) == null ) return null;
  	if ( "null".equalsIgnoreCase ( ds ) ) return null;
  	
  	for ( DateFormat dfmt: DATE_FMTS )
  		try {
  			return dfmt.parse ( ds );
  		}
  		catch ( ParseException ex ) {}
  	
  	throw new IllegalArgumentException ( "Syntax error on the date xml attribute value: " + ds );
  }
}