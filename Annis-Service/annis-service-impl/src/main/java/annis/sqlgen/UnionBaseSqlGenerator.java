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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;


public abstract class UnionBaseSqlGenerator<T> extends AbstractSqlGenerator<T>
{

	// corpusList, documents
	public String toSql(QueryData queryData, int indentBy) {
		Assert.notEmpty(queryData.getAlternatives(), "BUG: no alternatives");
		
		String indent = computeIndent(indentBy);
		StringBuffer sb = new StringBuffer();
		
		indent(sb, indent);
		List<String> alternatives = new ArrayList<String>();
		for (List<AnnisNode> alternative : queryData.getAlternatives()) {
			alternatives.add(createSqlForAlternative(queryData, alternative, indent));
		}
		sb.append(StringUtils.join(alternatives, "\n" + indent + "UNION "));

		// ORDER BY and LIMIT/OFFSET clauses cannot depend on alternative?
		appendOrderByClause(sb, queryData, null, indent);
		appendLimitOffsetClause(sb, queryData, null, indent);
				
		return sb.toString();
	}

}
