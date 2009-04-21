package annis.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import annis.model.Annotation;
import annis.sqlgen.NodeSqlAdapter;

public class BaseCorpusSelectionStrategy implements CorpusSelectionStrategy {

	protected List<Long> corpusList;
	protected List<Annotation> annotations;

	public BaseCorpusSelectionStrategy() {
		corpusList = new ArrayList<Long>();
		annotations = new ArrayList<Annotation>();
	}
	
	public void registerNodeAdapter(NodeSqlAdapter adapter) {

	}

	public String createViewSql() {
		return null;
	}

	public String whereClauseForNode(String docRefColumn) {
		return null;
	}

	public List<Long> getCorpusList() {
		return corpusList;
	}

	public void setCorpusList(List<Long> corpusList) {
		this.corpusList = corpusList;
	}

	public boolean usesViews() {
		return createViewSql() != null;
	}

	public String viewName(String table) {
		return table;
	}

	public void addMetaAnnotations(List<Annotation> annotations) {
		this.annotations.addAll(annotations);
	}
	
	protected String corpusConstraint() {
		if (selectAll())
			return null;
		
		StringBuffer sb = new StringBuffer();

		sb.append("IN ( SELECT DISTINCT c1.id FROM corpus AS c1");
		
		if ( ! corpusList.isEmpty() ) {
			sb.append(", ");
			sb.append("corpus AS c2");
		}

		for (int i = 1; i <= annotations.size(); ++i) {
			sb.append(", ");
			sb.append("corpus_annotation AS corpus_annotation");
			sb.append(i);
		}
		
		sb.append(" WHERE ");
		
		List<String> conditions = new ArrayList<String>();
		
		if ( ! corpusList.isEmpty() ) {
			conditions.add("c1.pre >= c2.pre");
			conditions.add("c1.post <= c2.post");
			conditions.add("c2.id IN ( :corpusList )".replace(":corpusList", StringUtils.join(corpusList, ", ")));
		}
		
		for (int i = 1; i <= annotations.size(); ++i) {
			Annotation annotation = annotations.get(i - 1);
			if (annotation.getNamespace() != null)
				conditions.add("corpus_annotation" + i + ".namespace = '" + annotation.getNamespace() + "'");
			conditions.add("corpus_annotation" + i + ".name = '" + annotation.getName() + "'");
			if (annotation.getValue() != null)
				conditions.add("corpus_annotation" + i + ".value " + annotation.getTextMatching().sqlOperator() + " '" + annotation.getValue() + "'");
			conditions.add("corpus_annotation" + i + ".corpus_ref = c1.id");
		}
		
		sb.append(StringUtils.join(conditions, " AND "));
		

		sb.append(" )");
		return sb.toString();
	}

	protected boolean selectAll() {
		return corpusList.isEmpty() && annotations.isEmpty();
	}

}
