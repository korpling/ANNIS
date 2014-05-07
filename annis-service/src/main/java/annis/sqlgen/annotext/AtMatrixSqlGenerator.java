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
package annis.sqlgen.annotext;


import annis.ql.parser.QueryData;
import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import annis.sqlgen.MatrixSqlGenerator;
import annis.sqlgen.TableAccessStrategy;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AtMatrixSqlGenerator extends MatrixSqlGenerator
{

  @Override
  protected void addFromOuterJoins(StringBuilder sb, QueryData queryData,
    TableAccessStrategy tas,
    String indent)
  {
    // get all the original outer joins
    super.addFromOuterJoins(sb, queryData, tas, indent);

    List<Long> corpusList = queryData.getCorpusList();
    
    String factsName = tas.partitionTableName(NODE_TABLE, corpusList);
    
    sb.append(indent).append(TABSTOP);
    sb.append("LEFT OUTER JOIN ").append(factsName).append(" AS node_anno")
      .append(" ON  (")
      .append(tas.aliasedColumn(NODE_TABLE, "id")).append(
        " = node_anno.id AND ")
      .append("node_anno.n_na_sample IS TRUE AND ")
      .append(tas.aliasedColumn(NODE_TABLE,
          "toplevel_corpus")).append(
        " = node_anno.toplevel_corpus AND ")
      .append("node_anno.toplevel_corpus IN (").append(StringUtils.
        join(corpusList, ", "))
      .append("))");

    sb.append("\n");

  }

  @Override
  protected String selectAnnotationsString(TableAccessStrategy tas)
  {
    return "array_agg(DISTINCT node_anno.node_qannotext) AS annotations";
  }

  
}
