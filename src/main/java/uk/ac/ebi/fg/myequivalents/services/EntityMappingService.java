package uk.ac.ebi.fg.myequivalents.services;

import uk.ac.ebi.fg.myequivalents.dao.EntityMappingDAO;


public class EntityMappingService
{
	EntityMappingDAO entityMappingDAO = new EntityMappingDAO ( null );
	
	public void storeMapping ( String serviceName1, String accession1, String serviceName2, String accession2 )
	{
	}

	public void storeMappings ( String... entities )
	{
	}

	public void storeMappingBundle ( String... entities )
	{
	}

}
