package annisservice.objects;
import java.util.HashSet;
import java.util.Set;

import annisservice.ifaces.AnnisAttribute;

public class AnnisAttributeImpl implements AnnisAttribute {
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

}
