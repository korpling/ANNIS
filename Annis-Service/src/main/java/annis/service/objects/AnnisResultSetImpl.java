package annis.service.objects;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;

public class AnnisResultSetImpl extends TreeSet<AnnisResult> implements AnnisResultSet {

	private static class AnnisResultSetComparator implements Comparator<AnnisResult>, Serializable {
		private static final long serialVersionUID = 9079334885343355346L;

		public int compare(AnnisResult o1, AnnisResult o2) {
			int order =  Long.signum(o1.getStartNodeId() - o2.getStartNodeId());
			if (order == 0) {
				order = Long.signum(o1.getEndNodeId() - o2.getEndNodeId());
				if (order == 0)
					return -1;
				else
					return order;
			} else 
				return order;
		}
	}

	private static final long serialVersionUID = -4537960128275640204L;

	public AnnisResultSetImpl() {
		super(new AnnisResultSetComparator());
	}
	
	public AnnisResultSetImpl(Collection<? extends AnnisResult> collection) {
		this();
		addAll(collection);
	}

	public String getJSON() {
		throw new RuntimeException("Not implemented yet");
	}

	public Set<String> getAnnotationLevelSet() {
		Set<String> levelSet = new HashSet<String>();
		for(AnnisResult result : this)
			levelSet.addAll(result.getAnnotationLevelSet());
		return levelSet;
	}

	public Set<String> getTokenAnnotationLevelSet() {
		Set<String> levelSet = new HashSet<String>();
		for(AnnisResult result : this)
			levelSet.addAll(result.getTokenAnnotationLevelSet());
		return levelSet;
	}

}
