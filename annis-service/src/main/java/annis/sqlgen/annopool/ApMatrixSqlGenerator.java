/*
 * Copyright 2012 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen.annopool;

import static annis.sqlgen.TableAccessStrategy.*;

import annis.ql.parser.QueryData;
import annis.sqlgen.MatrixSqlGenerator;
import annis.sqlgen.TableAccessStrategy;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ApMatrixSqlGenerator extends MatrixSqlGenerator
{

  @Override
  protected void addFromOuterJoins(StringBuilder sb, QueryData queryData,
    TableAccessStrategy tas,
    String indent)
  {
    // get all the original outer joins
    super.addFromOuterJoins(sb, queryData, tas, indent);


    List<Long> corpusList = queryData.getCorpusList();

    // add join to annotation pool  tables

    // node annopool
    sb.append(indent).append(TABSTOP);
    sb.append("LEFT OUTER JOIN annotation_pool AS node_anno ON  (").append(tas.
      aliasedColumn(NODE_TABLE, "node_anno_ref")).append(
      " = node_anno.id AND ").append(tas.aliasedColumn(NODE_TABLE,
      "toplevel_corpus")).append(
      " = node_anno.toplevel_corpus AND node_anno.toplevel_corpus IN (").append(StringUtils.
      join(corpusList, ", ")).append("))");

    sb.append("\n");

    // edge annopool
//    sb.append(indent).append(TABSTOP);
//    sb.append(
//      "LEFT OUTER JOIN annotation_pool AS edge_anno ON (").append(tas.
//      aliasedColumn(RANK_TABLE, "edge_anno_ref")).append(" = edge_anno.id AND ").
//      append(tas.aliasedColumn(RANK_TABLE, "toplevel_corpus")).append(" = edge_anno.toplevel_corpus AND "
//      + "edge_anno.toplevel_corpus IN (").append(StringUtils.join(corpusList,
//      ", ")).append("))");


  }

  @Override
  protected String selectAnnotationsString(TableAccessStrategy tas)
  {
    return "array_agg(DISTINCT coalesce(" 
      + "node_anno.\"namespace\"" + " || ':', '') || "
      + "node_anno.\"name\"" + " || ':' || encode("
      + "node_anno.\"val\"" + "::bytea, 'base64')) AS annotations";
  }

  
}
