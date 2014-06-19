package uk.ac.ebi.fg.myequivalents.provenance.db.managers;

import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.buildFromValues;

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
import static uk.ac.ebi.fg.myequivalents.provenance.model.ProvenanceRegisterParameter.*;

/**
 * TODO: Comment me!
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
	public void storeUser ( User user )
	{
		super.storeUser ( user );
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "accessControl.storeUser", buildFromValues ( "user", Arrays.asList ( user.getEmail () ) )  
	  ));
	  ts.commit ();
	}

	@Override
	public void storeUserFromXml ( Reader reader )
	{
		User user = super.storeUserFromXmlAndGetResult ( reader );
		if ( user == null ) return;

		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "accessControl.storeUserFromXml", buildFromValues ( "user", Arrays.asList ( user.getEmail () ) )  
	  ));
	  ts.commit ();
	}

	@Override
	public void setUserRole ( String email, Role role )
	{
		super.setUserRole ( email, role );

		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "accessControl.setUserRole", buildFromPairs ( Arrays.asList ( "user", email, "role", role.toString () ) )  
	  ));
	  ts.commit ();
	}

	@Override
	public boolean deleteUser ( String email )
	{
		if ( !super.deleteUser ( email ) ) return false;
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( 
			getUserEmail (), "accessControl.deleteUser", buildFromValues ( "user", Arrays.asList ( email ) )  
	  ));
	  ts.commit ();
		
	  return true;
	}

	@Override
	public void setServicesVisibility ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceNames )
	{
		super.setServicesVisibility ( publicFlagStr, releaseDateStr, cascade, serviceNames );
		trackSetVisibility ( 
			"accessControl.setServicesVisibility", "service", publicFlagStr, releaseDateStr, cascade, serviceNames
		);
	}

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

	@Override
	public void setEntitiesVisibility ( String publicFlagStr, String releaseDateStr, String ... entityIds )
	{
		super.setEntitiesVisibility ( publicFlagStr, releaseDateStr, entityIds );
		trackSetVisibility ( 
			"accessControl.setEntitiesVisibility", "entity", 
			publicFlagStr, releaseDateStr, null, entityIds
		);
	}

	private void trackSetVisibility ( 
		String command, String type, String publicFlagStr, String releaseDateStr, Boolean cascade, String ... ids )
	{
		List<ProvenanceRegisterParameter> result = new LinkedList<> ();
		if ( publicFlagStr != null ) result.add ( new ProvenanceRegisterParameter ( "publicFlag", publicFlagStr ) );
		if ( releaseDateStr != null ) result.add ( new ProvenanceRegisterParameter ( "releaseDate", releaseDateStr ) );
		if ( cascade != null ) result.add ( new ProvenanceRegisterParameter ( "cascade", Boolean.toString ( cascade ) ) );
		buildFromValues ( result, type, Arrays.asList ( ids ) );
		
		EntityTransaction ts = this.entityManager.getTransaction ();
	  ts.begin ();
	  provRegDao.create ( new ProvenanceRegisterEntry ( getUserEmail (), command, result ));  
	  ts.commit ();
	}

}
