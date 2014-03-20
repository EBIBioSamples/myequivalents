package uk.ac.ebi.fg.myequivalents.managers.impl.db;

import java.io.Reader;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.myequivalents.access_control.model.User;
import uk.ac.ebi.fg.myequivalents.access_control.model.User.Role;
import uk.ac.ebi.fg.myequivalents.dao.DescribeableDAO;
import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;
import uk.ac.ebi.fg.myequivalents.exceptions.SecurityException;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.AccessControlManager;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
import uk.ac.ebi.fg.myequivalents.utils.JAXBUtils;
import static uk.ac.ebi.fg.myequivalents.utils.jaxb.DateJaxbXmlAdapter.STR2DATE;
import static uk.ac.ebi.fg.myequivalents.utils.jaxb.NullBooleanJaxbXmlAdapter.STR2BOOL;

/**
 * The relational version of {@link AccessControlManager}. 
 *
 * <dl><dt>date</dt><dd>Mar 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DbAccessControlManager extends DbMyEquivalentsManager implements AccessControlManager
{
	public DbAccessControlManager ( EntityManager entityManager, String email, String apiPassword ) {
		this ( entityManager, email, apiPassword, false);
	}

	public DbAccessControlManager ( EntityManager entityManager, String email, String password, boolean isUserPassword ) 
	{
		super ( entityManager );
		if ( isUserPassword ) this.setFullAuthenticationCredentials ( email, password );
		else this.setAuthenticationCredentials ( email, password );
	}
	
	@Override
	public User setFullAuthenticationCredentials ( String email, String userPassword ) throws SecurityException
	{
		return setAuthenticationCredentials ( email, userPassword, true );
	}


	@Override
	public void storeUser ( User user )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.store ( user );
		ts.commit ();
	}

	@Override
	public void storeUserFromXml ( Reader reader )
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance ( User.class );
			Unmarshaller u = context.createUnmarshaller ();
			User user = (User) u.unmarshal ( reader );
			this.storeUser ( user );
		} 
		catch ( JAXBException ex ) {
			throw new RuntimeException ( "Error while reading user description from XML: " + ex.getMessage (), ex );
		}
	}
	
	/**
	 * TODO: ExposedUser?
	 */
	@Override
	public User getUser ( String email ) {
		return userDao.findByEmail ( email );
	}

	private String getUserAsXml ( String email ) 
	{
		return JAXBUtils.marshal ( getUser ( email ), User.class );
	}
	
	@Override
	public String getUserAs ( String outputFormat, String email )
	{
		outputFormat = StringUtils.trimToNull ( outputFormat );
		if ( !"xml".equalsIgnoreCase ( outputFormat ) ) throw new IllegalArgumentException ( 
			"Unsopported output format '" + outputFormat + "'" 
		);
		return getUserAsXml ( email );
	}

	@Override
	public void setUserRole ( String email, Role role ) 
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			userDao.setRole ( email, role );
		ts.commit ();
	}

	@Override
	public boolean deleteUser ( String email )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
			boolean result = userDao.delete ( email );
		ts.commit ();
		return result;
	}


	@Override
	public void setServicesVisibility ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceNames )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		setServicesVisibilityUnCommitted ( publicFlagStr, releaseDateStr, cascade, serviceNames );
		ts.commit ();
	}
	
	
	private void setServicesVisibilityUnCommitted ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceNames ) 
	{
		setDescribVisibility ( Service.class, "Service", publicFlagStr, releaseDateStr, serviceNames );
		
		if ( !cascade )
			return;

		Boolean publicFlag = STR2BOOL.unmarshal ( publicFlagStr );
		Date releaseDate = STR2DATE.unmarshal ( releaseDateStr );
		
		for ( String serviceName: serviceNames )
		{
			String sql = "UPDATE entity_mapping\nSET ";
			
			String sep = "";
			if ( publicFlag != null ) { sql += "public_flag = :publicFlag"; sep = ", "; }
			if ( releaseDate != null ) { sql += sep + "release_date = :releaseDate"; }

			sql += "\nWHERE service_name = :serviceName";
			
			Query q = entityManager.createNativeQuery ( sql );
			
			q.setParameter ( "serviceName", serviceName );
			if ( publicFlag != null ) q.setParameter ( "publicFlag", publicFlag );
			if ( releaseDate != null ) q.setParameter ( "releaseDate", releaseDate );
			
			q.executeUpdate ();
		}
	}
	


	
	@Override
	public void setRepositoriesVisibility ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... repositoryNames )
	{
		EntityTransaction ts = entityManager.getTransaction ();
		
		ts.begin ();
		setDescribVisibility ( Service.class, "Repository", publicFlagStr, releaseDateStr, repositoryNames );
		
		if ( !cascade ) {
			ts.commit ();
			return;
		}
		
		for ( String repoName: repositoryNames )
		{
			@SuppressWarnings ( "unchecked" )
			List<String> servNames = entityManager.createQuery ( 
				"SELECT s.name FROM Service s JOIN s.repository r WHERE r.name = '" + repoName + "'" )
				.getResultList ();
			setServicesVisibilityUnCommitted ( publicFlagStr, releaseDateStr, true, servNames.toArray ( new String [ servNames.size () ] ) );
		}
		
		ts.commit ();
	}
	
	
	@Override
	public void setServiceCollectionsVisibility ( String publicFlagStr, String releaseDateStr, boolean cascade, String ... serviceCollNames ) 
	{
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		setDescribVisibility ( ServiceCollection.class, "Service collection", publicFlagStr, releaseDateStr, serviceCollNames );

		if ( !cascade ) {
			ts.commit ();
			return;
		}

		for ( String scName: serviceCollNames )
		{
			@SuppressWarnings ( "unchecked" )
			List<String> servNames = entityManager.createQuery ( 
				"SELECT s.name FROM Service s JOIN s.serviceCollection sc WHERE sc.name = '" + scName + "'" )
				.getResultList ();
			setServicesVisibilityUnCommitted ( publicFlagStr, releaseDateStr, true, servNames.toArray ( new String [ servNames.size () ] ) );			
		}
		
		ts.commit ();
	}

	private <D extends Describeable> void setDescribVisibility ( 
		Class<D> targetClass, String describeableLabel, String publicFlagStr, String releaseDateStr, String ... names )
	{
		publicFlagStr = StringUtils.trimToNull ( publicFlagStr );
		releaseDateStr = StringUtils.trimToNull ( releaseDateStr );
		
		if ( publicFlagStr == null && releaseDateStr == null ) throw new IllegalArgumentException (
			"At least one of the public-flag or release-date parameter must be specified"
		);
		
		Boolean publicFlag = STR2BOOL.unmarshal ( publicFlagStr );
		Date releaseDate = STR2DATE.unmarshal ( releaseDateStr );
				
		userDao.enforceRole ( User.Role.EDITOR );
		DescribeableDAO<D> descrDao = new DescribeableDAO<D> ( entityManager, targetClass );
		
		for ( String dname: names )
		{
			D descr = descrDao.findByName ( dname, false );
			if ( descr == null ) throw new RuntimeException ( String.format ( 
				"%s '%s' not found", describeableLabel, dname 
			));
			if ( publicFlag != null ) descr.setPublicFlag ( publicFlag );
			if ( releaseDate != null ) descr.setReleaseDate ( releaseDate );
		}		
	}

	
	@Override
	public void setEntitiesVisibility ( String publicFlagStr, String releaseDateStr, String ... entityIds )
	{
		publicFlagStr = StringUtils.trimToNull ( publicFlagStr );
		releaseDateStr = StringUtils.trimToNull ( releaseDateStr );
		
		if ( publicFlagStr == null && releaseDateStr == null ) throw new IllegalArgumentException (
			"At least one of the public-flag or release-date parameter must be specified"
		);

		Boolean publicFlag = STR2BOOL.unmarshal ( publicFlagStr );
		Date releaseDate = STR2DATE.unmarshal ( releaseDateStr );
				
		EntityTransaction ts = entityManager.getTransaction ();
		ts.begin ();
		
			userDao.enforceRole ( User.Role.EDITOR );
			EntityMappingDAO emDao = new EntityMappingDAO ( entityManager );
			
			for ( String entityId: entityIds )
			{
				EntityMapping emap = emDao.findEntityMapping ( entityId );
				if ( emap == null ) throw 
					new RuntimeException ( String.format ( "Entity mapping '%s' not found", entityId ));
				
				if ( publicFlag != null ) emap.setPublicFlag ( publicFlag );
				if ( releaseDate != null ) emap.setReleaseDate ( releaseDate );
			}
		ts.commit ();
	}
}
