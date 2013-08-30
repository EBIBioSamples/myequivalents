package uk.ac.ebi.fg.myequivalents.utils.jaxb;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;

/**
 * 
 * Manages the translation of clear password set in an incoming XML into its hashed version.
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
  	return User.hashPassword ( clearPwd );
  }
}
