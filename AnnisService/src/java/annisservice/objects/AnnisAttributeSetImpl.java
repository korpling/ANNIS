package annisservice.objects;

import java.util.HashSet;

import annisservice.ifaces.AnnisAttribute;
import annisservice.ifaces.AnnisAttributeSet;

public class AnnisAttributeSetImpl extends HashSet<AnnisAttribute> implements AnnisAttributeSet {
	private static final long serialVersionUID = 4786862953057862936L;

	public String getJSON() {
		StringBuffer sBuffer = new StringBuffer();
		for(AnnisAttribute a : this) {
			if(sBuffer.length() > 0)
				sBuffer.append(", ");
			sBuffer.append(a.getJSON());
		}
		return "[" + sBuffer + "]";
	}
}
