/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class SubQueryCorpusSelectionStrategy {

	public String buildSubQuery(List<Long> corpusList, List<QueryAnnotation> metaData) {
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
		
		List<String> conditions = new ArrayList<>();
		
		if ( ! corpusList.isEmpty() ) {
			conditions.add("c1.pre >= c2.pre");
			conditions.add("c1.post <= c2.post");
			conditions.add("c2.id IN ( :corpusList )".replace(":corpusList", StringUtils.join(corpusList, ", ")));
		}
		
		for (int i = 1; i <= metaData.size(); ++i) {
			QueryAnnotation annotation = metaData.get(i - 1);
			if (annotation.getNamespace() != null)
				conditions.add("corpus_annotation" + i + ".namespace = '" + annotation.getNamespace() + "'");
			conditions.add("corpus_annotation" + i + ".name = '" + annotation.getName() + "'");
			if (annotation.getValue() != null)
         {
           String value = annotation.getValue();
           if(annotation.getTextMatching() == QueryNode.TextMatching.REGEXP_EQUAL 
             || annotation.getTextMatching() == QueryNode.TextMatching.REGEXP_NOT_EQUAL)
           {
             value = "^" + value + "$";
           }
				conditions.add("corpus_annotation" + i + ".value " + annotation.getTextMatching().sqlOperator() + " '" + value + "'");
         }
			conditions.add("corpus_annotation" + i + ".corpus_ref = c1.id");
		}
		
		sb.append(StringUtils.join(conditions, " AND "));

		return sb.toString();
	}

	public boolean hasCorpusSelection(List<Long> corpusList, List<QueryAnnotation> metaData) {
		return ! (corpusList.isEmpty() && metaData.isEmpty() );
	}

}
