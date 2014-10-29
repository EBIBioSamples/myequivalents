package uk.ac.ebi.fg.myequivalents.managers.interfaces;

import java.io.Reader;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter;

/**
 * Provides administrative functions to set visibility and user rights in a MyEquivalents store. 
 *
 * <dl><dt>date</dt><dd>Mar 4, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface AccessControlManager extends MyEquivalentsManager
{
	/** 
	 * Works like {@link #setAuthenticationCredentials(String, String)}, but uses the user password to authenticate the user, 
	 * rather than the API password. This is needed for user administration operations (e.g., {@link #storeUser(User)}). 
	 */
	public User setFullAuthenticationCredentials ( String email, String userPassword ) throws SecurityException;

	/**
	 * Changes user data. You need {@link #setFullAuthenticationCredentials(String, String)} for this and you need 
	 * to be administrator to change a user different than the one is logged in or to change the role of any user.
	 * 
	 * WARNING: When storing a user {@link User#getPassword() user.passwordHash} 
	 * or {@link User#getApiPassword() user.apiPasswordHash}. The parameter will send to  

	 */
	public void storeUser ( User user );
	
	/**
	 * Does the same as {@link #storeUser(User)}, but taking data from XML. TODO: document the format. This is 
	 * auto-generated via JAXB from {@link User} and reflects that class, for the moment examples are available 
	 * in JUnit tests (see uk.ac.ebi.fg.myequivalents.managers.AccessControlManagerTest in the -db package).
	 */
	public void storeUserFromXml ( Reader reader );

	/** 
	 * Get a user by email. It gives a result only if the user has proper rights, i.e., if the current user has a 
	 * {@link Role#VIEWER} and is the same as the parameter here, or if it has a {@link Role#ADMIN} role. 
	 */
	public User getUser ( String email );
	
	/**
	 * Like {@link #getUser(String)}, but returns the output as a string in the specified format. This is useful for 
	 * things like web services. Only 'xml' is supported at the moment. We plan formats like RDF or JSON for the future.
	 * 
	 * <b>WARNING</b>: due to the sake of performance, the output <b>is not</b> guaranteed to be pretty-printed, i.e. having
	 * indentation and alike. Use proper tools for achieving that (e.g., <a href = 'http://tinyurl.com/nuue8ql'>xmllint</a>).
	 */
	public String getUserAs ( String outputFormat, String email );
	
	/**
	 * Change the role of a user. It allows to do that only if the current user is an {@link Role#ADMIN}. It requires
	 * {@link #setFullAuthenticationCredentials(String, String)}.
	 */
	public void setUserRole ( String email, User.Role role );
	
	/**
	 * Removes a user. It allows to do that only if the current user is an {@link Role#ADMIN} and if you did 
	 * {@link #setFullAuthenticationCredentials(String, String)}.
	 */
	public boolean deleteUser ( String email );
	
	/**
	 * Changes the visibility parameters for {@link Service}. This cascades to all {@link EntityMapping} if these don't
	 * define more specific details.
	 * 
	 * You should use this format for the date: {@link DateJaxbXmlAdapter#DATE_FMT_REPRESENTATION}
	 */
	public void setServicesVisibility ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceNames );
	public void setRepositoriesVisibility ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... repositoryNames );
	public void setServiceCollectionsVisibility ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceCollNames );
	public void setEntitiesVisibility ( String publicFlagStr, String releaseDateStr, String ... entityIds );
}
