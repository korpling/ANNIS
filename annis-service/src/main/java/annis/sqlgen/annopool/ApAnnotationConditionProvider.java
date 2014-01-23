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

import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.model.QueryNode.TextMatching;
import annis.ql.parser.QueryData;
import annis.sqlgen.AnnotationConditionProvider;
import annis.sqlgen.TableAccessStrategy;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author thomas
 */
public class ApAnnotationConditionProvider implements
  AnnotationConditionProvider
{

  @Override
  public void addAnnotationConditions(List<String> conditions, QueryNode node,
    int index, QueryAnnotation annotation, String table, QueryData queryData,
    TableAccessStrategy tas)
  {
    TextMatching tm = annotation.getTextMatching();

    StringBuilder sbFunc = new StringBuilder("getAnno");

    if (tm == TextMatching.EXACT_NOT_EQUAL || tm
      == TextMatching.REGEXP_NOT_EQUAL)
    {
      sbFunc.append("Not");
    }


    List<String> params = new LinkedList<String>();

    if (annotation.getNamespace() != null)
    {
      params.add("'" + annotation.getNamespace() + "'");
    }
    else
    {
      params.add("NULL");
    }

    if (annotation.getName() != null)
    {
      params.add("'" + annotation.getName() + "'");
    }
    else
    {
      params.add("NULL");
    }

    if (annotation.getValue() != null)
    {
      if (tm == TextMatching.REGEXP_EQUAL
        || tm == TextMatching.REGEXP_NOT_EQUAL)
      {
        params.add("NULL");
        params.add("'^(" + annotation.getValue() + ")$'");
      }
      else
      {
        String escapeQuote = annotation.getValue().replaceAll("'", "''");
        params.add("'" + escapeQuote + "'");
        params.add("NULL");
      }
    }
    else
    {
      params.add("NULL");
      params.add("NULL");
    }

    params.add("ARRAY[" + StringUtils.join(queryData.getCorpusList(), ", ")
      + "]");

    params.add("'"
      + StringUtils.removeEnd(table, "_annotation").toLowerCase() + "'");

    sbFunc.append("(");
    sbFunc.append(StringUtils.join(params, ", "));
    sbFunc.append(")");


    String cond =
      tas.aliasedColumn(table, "anno_ref", index)
      + "= ANY(" + sbFunc.toString() + ")";


    conditions.add(cond);
  }
}
