package uk.ac.ebi.fg.myequivalents.provenance.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Index;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.ExposedService;
import uk.ac.ebi.fg.myequivalents.model.Describeable;
import uk.ac.ebi.fg.myequivalents.model.Entity;
import uk.ac.ebi.fg.myequivalents.model.MyEquivalentsModelMember;

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
	
	protected ProvenanceRegisterParameter () {
	}
	
	public ProvenanceRegisterParameter ( String valueType, String value )
	{
		super ();
		this.valueType = valueType;
		this.value = value;
	}

	public ProvenanceRegisterParameter ( String value )
	{
		this ( null, value );
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

	
	public static List<ProvenanceRegisterParameter> buildFromPairs ( Collection<String> typeValuePairs ) {
		return buildFromPairs ( null, typeValuePairs ); 
	}
	
	public static List<ProvenanceRegisterParameter> buildFromPairs ( 
		List<ProvenanceRegisterParameter> result, Collection<String> typeValuePairs 
	)
	{
		if ( typeValuePairs == null || typeValuePairs.isEmpty () ) return null;
		if ( typeValuePairs.size () % 2 != 0 ) throw new RuntimeException ( 
			"Internal error: cannot build a list of ProvenanceRegisterParameter from an uneven number of string pairs" );

		if ( result == null ) { 
			int sz = typeValuePairs.size () - 1;
			result = new ArrayList<ProvenanceRegisterParameter> ( (int) Math.ceil ( sz / 2 ) );
		}
		
		for ( Iterator<String> itr = typeValuePairs.iterator (); itr.hasNext ();  )
			result.add ( new ProvenanceRegisterParameter ( itr.next (), itr.next () ) );
		
		return result;
	}
	

	public static List<ProvenanceRegisterParameter> buildFromValues ( String valueType, Collection<String> values ) {
		return buildFromValues ( null, valueType, values );
	}
		
	
	public static List<ProvenanceRegisterParameter> buildFromValues ( 
		List<ProvenanceRegisterParameter> result, String valueType, Collection<String> values )
	{
		if ( values == null || values.isEmpty () ) return null;
		if ( result == null ) result = new ArrayList<ProvenanceRegisterParameter> ( values.size () );
		
		for ( String value: values )
			result.add ( new ProvenanceRegisterParameter ( valueType, value ) );
		return result;
	}
	

	public static <MM extends MyEquivalentsModelMember> List<ProvenanceRegisterParameter> buildFromObjects 
		( List<ProvenanceRegisterParameter> result, Collection<MM> objects )
	{
		if ( objects == null || objects.isEmpty () ) return result;
		
		if ( result == null ) result = new ArrayList<ProvenanceRegisterParameter> ( objects.size () );
		for ( MM mye: objects ) 
		{
			if ( mye == null ) continue;
			
			String type, value;
			
			if ( mye instanceof ExposedService ) {
				type = "service"; value = ((Describeable) mye).getName ();
			}
			else 
			{
				type = StringUtils.uncapitalize ( mye.getClass ().getSimpleName () );
				if ( mye instanceof Describeable )
					value = ((Describeable) mye).getName ();
				else if ( mye instanceof Entity ) 
				{
					Entity e = (Entity) mye;
					value = e.getServiceName () + ":" + e.getAccession ();
				}
				else throw new RuntimeException ( 
					"Internal error: cannot track provenance for unknown type " + mye.getClass ().getName ()
				);
			}

			result.add ( new ProvenanceRegisterParameter ( type, value ) );
		}
		return result;
	}
	
	public static <MM extends MyEquivalentsModelMember> List<ProvenanceRegisterParameter> buildFromObjects ( Collection<MM> objects )
	{
		return buildFromObjects ( null, objects );
	}
	
	
	
	@Override
  public boolean equals ( Object o ) 
  {
  	if ( o == null ) return false;
  	if ( this == o ) return true;
  	if ( this.getClass () != o.getClass () ) return false;
  	
    // The entity type
  	ProvenanceRegisterParameter that = (ProvenanceRegisterParameter) o;
    if ( this.getValueType () == null || that.getValueType () == null || !this.valueType.equals ( that.valueType ) ) return false; 
    if ( this.getValue () == null || that.getValue () == null || !this.value.equals ( that.value ) ) return false; 
    return true;
  }
	
	@Override
	public int hashCode ()
	{
		int result = 1;
		result = 31 * result + ( ( this.getValueType () == null ) ? 0 : valueType.hashCode () );
		result = 31 * result + ( ( this.getValue () == null ) ? 0 : value.hashCode () );
		return result;
	}
  
  @Override
  public String toString() {
  	return String.format ( 
  		"%s { valueType: '%s', value: '%s' }", this.getClass ().getSimpleName (), getValueType (), getValue ()
  	);
  }

}
