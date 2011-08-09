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

import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;

import annis.model.AnnisNode;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author thomas
 */
public class MetaDataAndCorpusWhereClause extends BaseNodeSqlGenerator
  implements WhereClauseSqlGenerator
{

  @Override
  public List<String> whereConditions(AnnisNode node, List<Long> corpusList, List<Long> documents)
  {
    if (documents == null && corpusList == null)
    {
      return null;
    }
    LinkedList<String> conditions = new LinkedList<String>();

    conditions.add("-- select documents by metadata and toplevel corpus");
    if (documents != null)
    {
      conditions.add(in(tables(node).aliasedColumn(NODE_TABLE, "corpus_ref"),
        documents));
    }

    if (corpusList != null)
    {
      conditions.add(in(tables(node).aliasedColumn(NODE_TABLE, "toplevel_corpus"),
        corpusList));


      if (tables(node).usesNodeAnnotationTable() && 
        !tables(node).isMaterialized(NODE_ANNOTATION_TABLE, FACTS_TABLE))
      {
        conditions.add(in(tables(node).aliasedColumn(NODE_ANNOTATION_TABLE, "toplevel_corpus"),
          corpusList));
      }


    }
    return conditions;
  }

  @Override
  public List<String> commonWhereConditions(List<AnnisNode> nodes, List<Long> corpusList, List<Long> documents)
  {
    return null;
  }
}
