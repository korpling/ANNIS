package annis.service.objects;

import java.util.Collection;
import java.util.HashSet;

import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisAttributeSet;

public class AnnisAttributeSetImpl extends HashSet<AnnisAttribute> implements AnnisAttributeSet {
	private static final long serialVersionUID = 4786862953057862936L;

	public AnnisAttributeSetImpl() {
		super();
	}

	public AnnisAttributeSetImpl(Collection<? extends AnnisAttribute> c) {
		super(c);
	}

	public String getJSON() {
		StringBuffer sBuffer = new StringBuffer();
		for(AnnisAttribute a : this) {
			if(sBuffer.length() > 0)
				sBuffer.append(", \n");
			sBuffer.append(a.getJSON());
		}
		return "[" + sBuffer + "]\n";
	}
}
