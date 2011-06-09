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

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;


public class SqlGenerator
{

	private Logger log = Logger.getLogger(this.getClass());
	
	// dependencies
	private ClauseSqlGenerator clauseSqlGenerator;
	
	public String toSql(QueryData queryData, List<Long> corpusList, List<Long> documents) {
		
		// build SQL query
		List<String> subQueries = new ArrayList<String>();
		for (List<AnnisNode> alternative : queryData.getAlternatives()) {
			String clauseSql = clauseSqlGenerator.toSql(alternative, 
        queryData.getMaxWidth(), corpusList, documents);
			subQueries.add(clauseSql);
		}
		String sql = StringUtils.join(subQueries, "\n\nUNION ");
		log.debug("SQL:\n" + sql);

		return sql;
	}

	///// Getter / Setter
	
	public ClauseSqlGenerator getClauseSqlGenerator() {
		return clauseSqlGenerator;
	}

	public void setClauseSqlGenerator(ClauseSqlGenerator clauseSqlGenerator) {
		this.clauseSqlGenerator = clauseSqlGenerator;
	}

}
