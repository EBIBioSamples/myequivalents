package uk.ac.ebi.fg.myequivalents.provenance.model;

import static org.apache.commons.lang3.StringUtils.trimToNull;
import static uk.ac.ebi.fg.myequivalents.resources.Const.COL_LENGTH_S;
import static uk.ac.ebi.fg.myequivalents.resources.Const.COL_LENGTH_URIS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Index;

import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.EntityId;
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.utils.EntityIdResolver;

/**
 * The parameters passed to a given operation. These might be entity IDs, service names etc.
 *
 * <dl><dt>date</dt><dd>6 Jun 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
@Embeddable
@XmlRootElement ( name = "prov-operation-parameter" )
@XmlAccessorType ( XmlAccessType.NONE )
public class ProvenanceRegisterParameter
{
	private String valueType;
	private String value;
	private String extraValue;
	
	protected ProvenanceRegisterParameter () {
	}
	
	public ProvenanceRegisterParameter ( String valueType, String value, String extraValue )
	{
		super ();
		this.valueType = valueType;
		this.value = value;
		this.extraValue = extraValue;
	}

	public ProvenanceRegisterParameter ( String valueType, String value )
	{
		this ( valueType, value, null );
	}
	
	/**
	 * Might be strings like 'service', 'entity' etc.
	 */
	@Column ( length = COL_LENGTH_S )
	@Index ( name = "prov_param_vtype" )
	@XmlAttribute ( name = "value-type" )
	public String getValueType ()
	{
		return valueType;
	}
	
	protected void setValueType ( String valueType )
	{
		this.valueType = valueType;
	}

	/**
	 * Might be things like 'service1', 'repository10', etc
	 */
	@Column ( length = COL_LENGTH_URIS )
	@Index ( name = "prov_param_val" )
	@XmlAttribute ( name = "value" )
	public String getValue ()
	{
		return value;
	}
	
	protected void setValue ( String value )
	{
		this.value = value;
	}
	
	/**
	 * Might be things like 'acc1', for parameters like entityId = 'service1:acc1'
	 */
	@Column ( length = COL_LENGTH_URIS )
	@Index ( name = "prov_param_extra1" )
	@XmlAttribute ( name = "extra-value" )
	public String getExtraValue ()
	{
		return extraValue;
	}
	
	protected void setExtraValue ( String extraValue )
	{
		this.extraValue = extraValue;
	}
	
	
	@Override
  public boolean equals ( Object o ) 
  {
  	if ( o == null ) return false;
  	if ( this == o ) return true;
  	if ( this.getClass () != o.getClass () ) return false;
  	
    // The entity type
  	ProvenanceRegisterParameter that = (ProvenanceRegisterParameter) o;
    if ( this.getValueType () == null ? that.getValueType () != null : !this.valueType.equals ( that.valueType ) ) return false; 
    if ( this.getValue () == null ? that.getValue () != null : !this.value.equals ( that.value ) ) return false; 
    if ( this.getExtraValue () == null ? that.getExtraValue () != null : !this.extraValue.equals ( that.extraValue ) ) return false; 
    return true;
  }
	
	@Override
	public int hashCode ()
	{
		int result = 1;
		result = 31 * result + ( ( this.getValueType () == null ) ? 0 : valueType.hashCode () );
		result = 31 * result + ( ( this.getValue () == null ) ? 0 : value.hashCode () );
		result = 31 * result + ( ( this.getExtraValue () == null ) ? 0 : extraValue.hashCode () );
		return result;
	}
  
  @Override
  public String toString() 
  {
  	return String.format ( 
  		"%s { valueType: '%s', value: '%s', extraValue: '%s' }", 
  		this.getClass ().getSimpleName (), getValueType (), getValue (), getExtraValue ()
  	);
  }
  
  
  /**
   * Builds a parameter out of its string components
   */
  public static ProvenanceRegisterParameter p ( String valueType, String value, String extraValue ) {
  	return new ProvenanceRegisterParameter ( valueType, value, extraValue );
  }

  /**
   * Builds a parameter from its string components (null extraValue) 
   */
  public static ProvenanceRegisterParameter p ( String valueType, String value ) {
  	return new ProvenanceRegisterParameter ( valueType, value, null );
  }

  /**
   * Builds a parameter using a string in the form {@code "type:value[:extraValue]"}
   */
  public static ProvenanceRegisterParameter p ( String paramStr )
  {
		String[] ochunks = paramStr.split ( ":", -2 );
		String ptype = ochunks.length > 0 ? trimToNull ( ochunks [ 0 ] ) : null;
		String pval = ochunks.length > 1 ? trimToNull ( ochunks [ 1 ] ) : null;
		String pxval = ochunks.length > 2 ? trimToNull ( ochunks [ 2 ] ) : null;

		return p ( ptype, pval, pxval );
  }

  /**
   * Builds a parameter using a string in the form {@code "type:value[:extraValue]"}
   */
  public static List<ProvenanceRegisterParameter> p ( List<ProvenanceRegisterParameter> result, List<String> paramsStr )
  {
  	if ( paramsStr == null || paramsStr.isEmpty () ) return result;
		if ( result == null ) result = new ArrayList<ProvenanceRegisterParameter> ( paramsStr.size () );

		for ( String pstr: paramsStr )
			result.add ( p ( pstr ) );
		
		return result;
  }

  public static List<ProvenanceRegisterParameter> p ( List<String> paramsStr ) {
  	return p ( (List<ProvenanceRegisterParameter>) null, paramsStr );
  }

  
  /**
   * Builds p ( "entity", "service1", "acc1" )
   */
  public static ProvenanceRegisterParameter pent ( String serviceName, String acc ) {
  	return new ProvenanceRegisterParameter ( "entity", serviceName, acc );
  }

  /**
   * Builds p ( "entity", entityId ). The entity resolver is necessary because we always save IDs in the form
   * service:acc in the provenance records.
   */
  public static ProvenanceRegisterParameter pent ( EntityIdResolver entityIdResolver, String entityId ) 
  {
  	EntityId eid = entityIdResolver.doall ( entityId );
  	return pent ( eid.getServiceName (), eid.getAcc () );
  }
  
  
  
  /**
   * Builds p ( "service", service.getName () ).
   */
  public static ProvenanceRegisterParameter p ( Service service ) {
  	return new ProvenanceRegisterParameter ( "service", service.getName () );
  }

  /**
   * Builds p ( uncapitalize ( d.class.simpleName ), service.name ).
   */
  public static ProvenanceRegisterParameter p ( Describeable d ) 
  {
  	return new ProvenanceRegisterParameter ( 
  		StringUtils.uncapitalize ( d.getClass ().getSimpleName () ), 
  		d.getName () 
  	);
  }

  /**
   * Builds p ( "entity", e.serviceName, e.accession ).
   */
  public static ProvenanceRegisterParameter p ( Entity e ) {
  	return new ProvenanceRegisterParameter ( "entity",  e.getServiceName (), e.getAccession () );
  }

  /**
   * Calls previous methods based on the class object is instance of. 
   */
  public static <MM extends MyEquivalentsModelMember> ProvenanceRegisterParameter p ( MM object )
  {
  	if ( object instanceof Service ) return p ( (Service) object );
  	else if ( object instanceof Describeable ) return p ( (Describeable) object );
  	else if ( object instanceof Entity ) return p ( (Entity) object );
 
  	throw new RuntimeException ( 
			"Cannot track provenance for unknown type " + object.getClass ().getName ()
		);
  }

  /**
   * Uses {@link #p(MyEquivalentsModelMember)} for each object in the collection, adds the parameters built to result. 
   * result may be null, in which case it will be initialised with an empty list.
   */
	public static <MM extends MyEquivalentsModelMember> List<ProvenanceRegisterParameter> p 
		( List<ProvenanceRegisterParameter> result, Collection<MM> objects )
	{
		if ( objects == null || objects.isEmpty () ) return result;
		if ( result == null ) result = new ArrayList<ProvenanceRegisterParameter> ( objects.size () );

		for ( MM object: objects ) result.add ( p ( object ) ); 
		return result;
	}
	
	/**
	 * Wraps {@link #p(List, Collection)} with null result.
	 */
	public static <MM extends MyEquivalentsModelMember> List<ProvenanceRegisterParameter> p ( Collection<MM> objects )
	{
		return p ( null, objects );
	}

	/**
	 * Generate parameters having all the same valueType. Adds them to result, initialises result with an empty list, if 
	 * it's null. 
	 */
	public static List<ProvenanceRegisterParameter> p ( List<ProvenanceRegisterParameter> result, String valueType, List<String> values )
	{
		if ( values == null || values.isEmpty () ) return result;
		if ( result == null ) result = new ArrayList<ProvenanceRegisterParameter> ( values.size () );
		
		for ( String value: values ) result.add ( p ( valueType, value ) );
		return result;
	}

	/**
	 * Wraps {@link #p(List, String, List)} with result = null. 
	 */
	public static List<ProvenanceRegisterParameter> p ( String valueType, List<String> values ) {
		return p ( null, valueType, values );
	}

	
	/**
	 * Builds parameters for entity IDs. Adds up to result, which is initialised with an empty list, if it's null.
	 * @see #pent(EntityIdResolver, String) about the entityResolver parameter.
	 */
	public static List<ProvenanceRegisterParameter> pent ( 
		EntityIdResolver entityIdResolver, List<ProvenanceRegisterParameter> result, List<String> entityIds 
	)
	{
		if ( entityIds == null || entityIds.isEmpty () ) return result;
		if ( result == null ) result = new ArrayList<ProvenanceRegisterParameter> ( entityIds.size () );
		
		for ( String entityId: entityIds ) result.add ( pent ( entityIdResolver, entityId ) );
		return result;
	}

	/**
	 * Wraps {@link #pent(List, List)} with result = null.
	 * @see #pent(EntityIdResolver, String) about the entityResolver parameter.
   *
	 */
	public static List<ProvenanceRegisterParameter> pent ( EntityIdResolver entityIdResolver, List<String> entityIds ) 
	{
		return pent ( entityIdResolver, null, entityIds );
	}

}
