package annis.service.objects;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import annis.service.ifaces.AnnisAttribute;

public class AnnisAttributeImpl implements AnnisAttribute, Serializable {
	
	private static final long serialVersionUID = 4786862953057862936L;

	private String name = "";
	private Set<String> distinctValues = new HashSet<String>();

	public Set<String> getValueSet() {
		return this.distinctValues;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getJSON() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("name: '" + this.getName() + "', values: [");
		int vCount = 0;
		for(String value : this.getValueSet()) {
			if(vCount++ > 0 )
				sBuffer.append(", ");
			sBuffer.append("'" + value + "'");
		}
		sBuffer.append("]");
		return "{" + sBuffer + "}";
	}

	public void addValue(String value) {
		this.distinctValues.add(value);
	}

	public boolean hasValue(String value) {
		return this.distinctValues.contains(value);
	}
	
	@Override
	public String toString() {
		return name + " " + distinctValues;
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj == null || ! (obj instanceof AnnisAttributeImpl) )
			return false;
		
		AnnisAttributeImpl other = (AnnisAttributeImpl) obj;
		
		return new EqualsBuilder().append(this.name, other.name).append(this.distinctValues, other.distinctValues).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(name).append(distinctValues).toHashCode();
	}
	
}
