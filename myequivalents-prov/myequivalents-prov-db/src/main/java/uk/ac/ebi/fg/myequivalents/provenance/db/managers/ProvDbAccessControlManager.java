package uk.ac.ebi.fg.myequivalents.provenance.db.managers;


import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.p;
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.pent;

import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbAccessControlManager;
import uk.ac.ebi.fg.myequivalents.provenance.db.dao.ProvenanceRegisterEntryDAO;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterEntry;
import uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter;
import uk.ac.ebi.fg.myequivalents.utils.DbEntityIdResolver;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;

/**
 * A wrapper of {@link DbAccessControlManager} that uses the provenance register to keep track of access-control
 * changes in myEquivalents.
 *
 * <dl><dt>date</dt><dd>19 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProvDbAccessControlManager extends DbAccessControlManager
{
	private ProvenanceRegisterEntryDAO provRegDao;

	public ProvDbAccessControlManager ( EntityManager entityManager, String email, String apiPassword ) {
		this ( entityManager, email, apiPassword, false );
	}

	public ProvDbAccessControlManager ( EntityManager entityManager, String email, String password, boolean isUserPassword ) 
	{
		super ( entityManager, email, password, isUserPassword );
		provRegDao = new ProvenanceRegisterEntryDAO ( this.entityManager );
	}

	@Override
	/** Stores 'accessControl.storeUser' operation and a 'user' parameters into the provenance register */ 
	public void storeUser ( User user )
	{
		super.storeUser ( user );
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "accessControl.storeUser", Arrays.asList ( p ( "user", user.getEmail () ) )  
	  ));
	  ts.commit ();
	}

	@Override
	/** Stores 'accessControl.storeUserFromXml' operation and a 'user' parameter into the provenance register */ 
	public void storeUserFromXml ( Reader reader )
	{
		User user = super.storeUserFromXmlAndGetResult ( reader );
		if ( user == null ) return;

		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "accessControl.storeUserFromXml", Arrays.asList ( p ( "user", user.getEmail () ) )  
	  ));
	  ts.commit ();
	}

	/** Stores 'accessControl.setUserRole' operation, plus'user' and 'role' parameters into the provenance register */ 
	@Override
	public void setUserRole ( String email, Role role )
	{
		super.setUserRole ( email, role );

		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "accessControl.setUserRole", Arrays.asList ( p ( "user", email ), p ( "role", role.toString () ) )  
	  ));
	  ts.commit ();
	}

	/** Stores 'accessControl.deleteUser' operation and a 'user' parameter into the provenance register */ 
	@Override
	public boolean deleteUser ( String email )
	{
		if ( !super.deleteUser ( email ) ) return false;
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "accessControl.deleteUser", Arrays.asList ( p ( "user", email ) )   
	  ));
	  ts.commit ();
		
	  return true;
	}

	/** Stores 'accessControl.setServicesVisibility' operation and 'service' parameters into the provenance register */ 
	@Override
	public void setServicesVisibility ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceNames )
	{
		super.setServicesVisibility ( publicFlagStr, releaseDateStr, cascade, serviceNames );
		trackSetVisibility ( 
			"accessControl.setServicesVisibility", "service", publicFlagStr, releaseDateStr, cascade, serviceNames
		);
	}

	/** Stores 'accessControl.setRepositoriesVisibility' operation and a 'repository' parameters into the provenance register */ 
	@Override
	public void setRepositoriesVisibility ( 
		String publicFlagStr, String releaseDateStr, boolean cascade, String ... repositoryNames )
	{
		super.setRepositoriesVisibility ( publicFlagStr, releaseDateStr, cascade, repositoryNames );
		trackSetVisibility ( 
			"accessControl.setRepositoriesVisibility", "repository", 
			publicFlagStr, releaseDateStr, cascade, repositoryNames 
		);
	}

	/** Stores 'accessControl.setRepositoriesVisibility' operation and a 'repository' parameters into the provenance register */ 
	@Override
	public void setServiceCollectionsVisibility ( 
		String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceCollNames )
	{
		super.setServiceCollectionsVisibility ( publicFlagStr, releaseDateStr, cascade, serviceCollNames );
		trackSetVisibility ( 
			"accessControl.setServiceCollectionsVisibility", "serviceCollection", 
			publicFlagStr, releaseDateStr, cascade, serviceCollNames
		);
	}

	/** Stores 'accessControl.setEntitiesVisibility' operation and a 'entity' parameters into the provenance register */ 
	@Override
	public void setEntitiesVisibility ( String publicFlagStr, String releaseDateStr, String ... entityIds )
	{
		super.setEntitiesVisibility ( publicFlagStr, releaseDateStr, entityIds );
		trackSetVisibility ( 
			"accessControl.setEntitiesVisibility", "entity", 
			publicFlagStr, releaseDateStr, null, entityIds
		);
	}

	/**
	 * Used by operations related to visibility changes, to store corresponding records in the provenance register. 
	 * type is like 'service', 'repository', 'entity' etc, for the other parameters have a look at 
	 * {@link ProvenanceRegisterParameter}. 
	 */
	private void trackSetVisibility ( 
		String command, String type, String publicFlagStr, String releaseDateStr, Boolean cascade, String ... ids )
	{
		List<ProvenanceRegisterParameter> params = new LinkedList<> ();
		if ( publicFlagStr != null ) params.add ( new ProvenanceRegisterParameter ( "publicFlag", publicFlagStr ) );
		if ( releaseDateStr != null ) params.add ( new ProvenanceRegisterParameter ( "releaseDate", releaseDateStr ) );
		if ( cascade != null ) params.add ( new ProvenanceRegisterParameter ( "cascade", Boolean.toString ( cascade ) ) );
		
		if ( "entity".equals ( type ) )
			pent ( provRegDao.getEntityIdResolver (), params, Arrays.asList ( ids ) );
		else
			p ( params, type, Arrays.asList ( ids ) );
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( getUserEmail (), command, params ));  
	  ts.commit ();
	}

}
