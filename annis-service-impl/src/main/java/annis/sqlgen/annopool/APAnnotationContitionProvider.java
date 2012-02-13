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
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author thomas
 */
public class APAnnotationContitionProvider implements
  AnnotationConditionProvider
{

  @Override
  public void addAnnotationConditions(List<String> conditions, QueryNode node,
    int index, QueryAnnotation annotation, String table, QueryData queryData, TableAccessStrategy tas)
  {
    TextMatching tm = annotation.getTextMatching();

    StringBuilder sbFunc = new StringBuilder("get");

    sbFunc.append("AnnoBy");

    List<String> params = new LinkedList<String>();

    if (annotation.getNamespace() != null)
    {
      params.add("'" + annotation.getNamespace() + "'");
      sbFunc.append("Namespace");
    }
    if (annotation.getName() != null)
    {
      params.add("'" + annotation.getName() + "'");
      sbFunc.append("Name");
    }
    if (annotation.getValue() != null)
    {

      sbFunc.append("Val");

      if (tm == TextMatching.REGEXP_EQUAL
        || tm == TextMatching.REGEXP_NOT_EQUAL)
      {
        sbFunc.append("Regex");
        params.add("'^" + annotation.getValue() + "$'");
      }
      else
      {
        params.add("'" + annotation.getValue() + "'");
      }
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

    if (tm == TextMatching.EXACT_NOT_EQUAL || tm
      == TextMatching.REGEXP_NOT_EQUAL)
    {
      cond = "NOT (" + cond + ")";
    }
    conditions.add(cond);
  }
}
