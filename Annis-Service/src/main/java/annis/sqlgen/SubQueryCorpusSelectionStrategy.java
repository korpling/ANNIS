package annis.sqlgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import annis.model.Annotation;

public class SubQueryCorpusSelectionStrategy {

	public String buildSubQuery(List<Long> corpusList, List<Annotation> metaData) {
		StringBuffer sb = new StringBuffer();

		sb.append("SELECT DISTINCT c1.id FROM corpus AS c1");
		
		if ( ! corpusList.isEmpty() ) {
			sb.append(", ");
			sb.append("corpus AS c2");
		}

		for (int i = 1; i <= metaData.size(); ++i) {
			sb.append(", ");
			sb.append("corpus_annotation AS corpus_annotation");
			sb.append(i);
		}
		
		if (hasCorpusSelection(corpusList, metaData))
			sb.append(" WHERE ");
		
		List<String> conditions = new ArrayList<String>();
		
		if ( ! corpusList.isEmpty() ) {
			conditions.add("c1.pre >= c2.pre");
			conditions.add("c1.post <= c2.post");
			conditions.add("c2.id IN ( :corpusList )".replace(":corpusList", StringUtils.join(corpusList, ", ")));
		}
		
		for (int i = 1; i <= metaData.size(); ++i) {
			Annotation annotation = metaData.get(i - 1);
			if (annotation.getNamespace() != null)
				conditions.add("corpus_annotation" + i + ".namespace = '" + annotation.getNamespace() + "'");
			conditions.add("corpus_annotation" + i + ".name = '" + annotation.getName() + "'");
			if (annotation.getValue() != null)
				conditions.add("corpus_annotation" + i + ".value " + annotation.getTextMatching().sqlOperator() + " '" + annotation.getValue() + "'");
			conditions.add("corpus_annotation" + i + ".corpus_ref = c1.id");
		}
		
		sb.append(StringUtils.join(conditions, " AND "));

		return sb.toString();
	}

	public boolean hasCorpusSelection(List<Long> corpusList, List<Annotation> metaData) {
		return ! (corpusList.isEmpty() && metaData.isEmpty() );
	}

}
