package uk.ac.ebi.fg.myequivalents.managers.interfaces;


/**
 * The abstract factory for the MyEquivalents managers. This returns the managers needed to access the MyEquivalents
 * system. Several concrete implementations are available (e.g., DbManagerFactory, WsCliManagerFactory).  
 *
 * <dl><dt>date</dt><dd>Nov 26, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public interface ManagerFactory
{
	/** Works with the anonymous user */
	public EntityMappingManager newEntityMappingManager ();
	
	/** Gets an {@link EntityMappingManager} where the parameter used is logged-in and made active */ 
	public EntityMappingManager newEntityMappingManager ( String email, String apiPassword );

	
	/** Works with the anonymous user */
	public ServiceManager newServiceManager ();

	/** Gets an {@link ServiceManager} where the parameter used is logged-in and made active */ 
	public ServiceManager newServiceManager ( String email, String apiPassword );
	
	
	/** Gets an {@link AccessControlManager} where the parameter used is logged-in and made active */ 
	public AccessControlManager newAccessControlManager ( String email, String apiPassword );
	
	/** 
	 * This variant authenticate a user with its user password (not the API one), which is needed for operations like
	 * modifcation of user details.
	 */
	public AccessControlManager newAccessControlManagerFullAuth (  String email, String userPassword );
	
	/**
	 * Provides with a {@link BackupManager}. 
	 */
	public BackupManager newBackupManager ( String email, String apiPassword );

}
