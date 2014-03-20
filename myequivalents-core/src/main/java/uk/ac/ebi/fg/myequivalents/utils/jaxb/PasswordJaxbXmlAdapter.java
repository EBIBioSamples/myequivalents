package uk.ac.ebi.fg.myequivalents.utils.jaxb;


import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * 
 * Avoids to expose any (hashed) password upon marshalling, i.e., the XML will never reports any password.
 *
 * <dl><dt>date</dt><dd>10 Jul 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class PasswordJaxbXmlAdapter extends XmlAdapter<String, String> 
{
  @Override
  public String marshal ( String hashedPwd )
  {
  	return null;
  }

  @Override
  public String unmarshal ( String clearPwd ) {
  	return clearPwd;
  }
}
