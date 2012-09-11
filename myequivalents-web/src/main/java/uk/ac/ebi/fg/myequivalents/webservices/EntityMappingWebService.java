/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.webservices;

import javax.jws.WebService;

import uk.ac.ebi.fg.myequivalents.managers.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.EntityMappingSearchResult;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Sep 11, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@WebService ( serviceName = "mapping-ws" )
public class EntityMappingWebService
{
	private EntityMappingManager emapMgr = new EntityMappingManager ();
	
	public EntityMappingSearchResult getMappings ( Boolean isRaw, String... entity ) {
		return emapMgr.getMappings ( isRaw, entity );
	}

}
