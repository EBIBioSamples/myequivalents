package uk.ac.ebi.fg.myequivalents.provenance.model;

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
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;
import uk.ac.ebi.fg.myequivalents.model.Service;
import uk.ac.ebi.fg.myequivalents.utils.EntityMappingUtils;

/**
 * TODO: Comment me!
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
	

	@Column ( length = 50 )
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

	
	@Column ( length = 2000 )
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
	
	@Column ( length = 2000 )
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
    if ( this.getValueType () == null && that.getValueType () != null || !this.valueType.equals ( that.valueType ) ) return false; 
    if ( this.getValue () == null && that.getValue () != null || !this.value.equals ( that.value ) ) return false; 
    if ( this.getExtraValue () == null && that.getExtraValue () != null || !this.extraValue.equals ( that.extraValue ) ) return false; 
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
  
  
  
  public static ProvenanceRegisterParameter p ( String valueType, String value, String extraValue ) {
  	return new ProvenanceRegisterParameter ( valueType, value, extraValue );
  }

  public static ProvenanceRegisterParameter p ( String valueType, String value ) {
  	return new ProvenanceRegisterParameter ( valueType, value, null );
  }

  public static ProvenanceRegisterParameter pent ( String entityId ) 
  {
  	String chunks[] = EntityMappingUtils.parseEntityId ( entityId );
  	return new ProvenanceRegisterParameter ( "entity", chunks [ 0 ], chunks [ 1 ] );
  }

  
  public static ProvenanceRegisterParameter p ( Service service ) {
  	return new ProvenanceRegisterParameter ( "service", service.getName () );
  }

  public static ProvenanceRegisterParameter p ( Describeable d ) 
  {
  	return new ProvenanceRegisterParameter ( 
  		StringUtils.uncapitalize ( d.getClass ().getSimpleName () ), 
  		d.getName () 
  	);
  }

  public static ProvenanceRegisterParameter p ( Entity e ) {
  	return new ProvenanceRegisterParameter ( "entity",  e.getServiceName (), e.getAccession () );
  }

  public static <MM extends MyEquivalentsModelMember> ProvenanceRegisterParameter p ( MM object )
  {
  	if ( object instanceof Service ) return p ( (Service) object );
  	else if ( object instanceof Describeable ) return p ( (Describeable) object );
  	else if ( object instanceof Entity ) return p ( (Entity) object );
 
  	throw new RuntimeException ( 
			"Cannot track provenance for unknown type " + object.getClass ().getName ()
		);
  }

  
	public static <MM extends MyEquivalentsModelMember> List<ProvenanceRegisterParameter> p 
		( List<ProvenanceRegisterParameter> result, Collection<MM> objects )
	{
		if ( objects == null || objects.isEmpty () ) return result;
		if ( result == null ) result = new ArrayList<ProvenanceRegisterParameter> ( objects.size () );

		for ( MM object: objects ) result.add ( p ( object ) ); 
		return result;
	}
	
	public static <MM extends MyEquivalentsModelMember> List<ProvenanceRegisterParameter> p ( Collection<MM> objects )
	{
		return p ( null, objects );
	}

	
	public static List<ProvenanceRegisterParameter> p ( List<ProvenanceRegisterParameter> result, String valueType, List<String> values )
	{
		if ( values == null || values.isEmpty () ) return result;
		if ( result == null ) result = new ArrayList<ProvenanceRegisterParameter> ( values.size () );
		
		for ( String value: values ) result.add ( p ( valueType, value ) );
		return result;
	}

	public static List<ProvenanceRegisterParameter> p ( String valueType, List<String> values ) {
		return p ( null, valueType, values );
	}

	
	
	public static List<ProvenanceRegisterParameter> pent ( List<ProvenanceRegisterParameter> result, List<String> entityIds )
	{
		if ( entityIds == null || entityIds.isEmpty () ) return result;
		if ( result == null ) result = new ArrayList<ProvenanceRegisterParameter> ( entityIds.size () );
		
		for ( String entityId: entityIds ) result.add ( pent ( entityId ) );
		return result;
	}

	public static List<ProvenanceRegisterParameter> pent ( List<String> entityIds ) {
		return pent ( null, entityIds );
	}

}
