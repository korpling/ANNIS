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

import static annis.sqlgen.SqlConstraints.in;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annis.querymodel.AnnisNode;
import annis.ql.parser.QueryData;

/**
 *
 * @author thomas
 */
public class MetaDataAndCorpusWhereClauseGenerator 
  extends TableAccessStrategyFactory
  implements WhereClauseSqlGenerator<QueryData>
{

	@Override
	public Set<String> whereConditions(QueryData queryData,
			List<AnnisNode> alternative, String indent) {
		Set<String> conditions = new HashSet<String>();
		List<Long> corpusList = queryData.getCorpusList();
		List<Long> documents = queryData.getDocuments();
		
	    if (documents == null && corpusList == null)
	    {
	      return new HashSet<String>();
	    }

	    for (AnnisNode node : alternative) {
			// FIXME: where condition comment should be optional 
			// conditions.add("-- select documents by metadata and toplevel corpus");
			if (documents != null && ! documents.isEmpty() )
			{
				conditions.add(in(tables(node).aliasedColumn(NODE_TABLE, "corpus_ref"),
			    documents));
			}
			
			if (corpusList != null && ! corpusList.isEmpty())
			{
				conditions.add(in(tables(node).aliasedColumn(NODE_TABLE, "toplevel_corpus"),
			    corpusList));
			
			}
		}
		
		return conditions;
	}

}
