/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Utility functions about the management of equivalent entity mappings and related objects.
 *
 * <dl><dt>date</dt><dd>Oct 8, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class EntityMappingUtils
{
	private EntityMappingUtils () {
	}

	/** 
	 * Parses the syntax for an entityID string and returns the pair [ service-name, accession ] that identify the entity.
	 * For the moment, the only valid syntax is 'service-name:accession'. In future, we plan to support formats like
	 * uri( "entity-uri" ) and uri( "service-uri", "entity-accession" ).
	 * 
	 * @return a pair of [ service-name, accession ]. It expects a non-null valid parameter, an exception is thrown 
	 * otherwise.
	 * 
	 */
	public static String[] parseEntityId ( String entityId )
	{
		entityId = StringUtils.trimToNull ( entityId );
		if ( entityId == null ) throw new IllegalArgumentException (
			"Null entity specification"
		);
		
		int twoColIdx = entityId.lastIndexOf ( ':' );
		if ( twoColIdx == -1 ) throw new IllegalArgumentException ( 
			"Invalid entity mapping ID '" + entityId + "', must be serviceName:accession, URIs are not supported yet" 
		);
		return new String[] { entityId.substring ( 0, twoColIdx ), entityId.substring ( twoColIdx + 1 ) };
	}

}
