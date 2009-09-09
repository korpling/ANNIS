/**
 * 
 */
package annis.ql.parser;

import java.util.ArrayList;
import java.util.List;

import annis.model.AnnisNode;
import annis.model.Annotation;

public class QueryData {
	private List<List<AnnisNode>> alternatives;
	private List<Long> corpusList;
	private List<Annotation> metaData;
	private int maxWidth;

	public QueryData() {
		alternatives = new ArrayList<List<AnnisNode>>();
		corpusList = new ArrayList<Long>();
		metaData = new ArrayList<Annotation>();
	}
	
	public List<List<AnnisNode>> getAlternatives() {
		return alternatives;
	}
	public void setAlternatives(List<List<AnnisNode>> alternatives) {
		this.alternatives = alternatives;
	}
	public List<Long> getCorpusList() {
		return corpusList;
	}
	public void setCorpusList(List<Long> corpusList) {
		this.corpusList = corpusList;
	}
	public List<Annotation> getMetaData() {
		return metaData;
	}
	public void setMetaData(List<Annotation> metaData) {
		this.metaData = metaData;
	}
	public int getMaxWidth() {
		return maxWidth;
	}
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public boolean addAlternative(List<AnnisNode> nodes) {
		return alternatives.add(nodes);
	}

	public boolean addMetaAnnotations(List<Annotation> annotations) {
		return metaData.addAll(annotations);
	}
}