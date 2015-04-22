package uk.ac.ebi.fg.myequivalents.utils.jaxb;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.StringUtils;

/**
 * A {@link XmlAdapter JAXB adapter}, which converts back/forth the string values true/false/null 
 *
 * <dl><dt>date</dt><dd>10 Jul 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class NullBooleanJaxbXmlAdapter extends XmlAdapter<String, Boolean> 
{
	/** This can be useful when you want to make conversions on your own */
	public static final NullBooleanJaxbXmlAdapter STR2BOOL = new NullBooleanJaxbXmlAdapter ();

  @Override
  public String marshal ( Boolean b )
  {
  	return b == null ? "null" : b.toString ();
  }

  @Override
  public Boolean unmarshal ( String bs ) 
  {
  	if ( ( bs = StringUtils.trimToNull ( bs ) ) == null ) return null;
  	
  	if ( "null".equalsIgnoreCase ( bs ) ) return null;
  	if ( "true".equalsIgnoreCase ( bs ) ) return true;
  	if ( "false".equalsIgnoreCase ( bs ) ) return false;
  	
  	throw new IllegalArgumentException ( "Syntax error on the boolean xml attribute value: " + bs );
  }
}