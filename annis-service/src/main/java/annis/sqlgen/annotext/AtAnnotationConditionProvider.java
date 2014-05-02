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

import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.model.QueryNode.TextMatching;
import annis.ql.parser.QueryData;
import annis.sqlgen.AnnotationConditionProvider;
import annis.sqlgen.TableAccessStrategy;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import com.google.common.base.Objects;
import java.util.Set;
/**
 *
 * @author thomas
 */
public class AtAnnotationConditionProvider implements
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

  @Override
  public void addEqualValueConditions(List<String> conditions, QueryNode node,
    QueryNode target, TableAccessStrategy tasNode, TableAccessStrategy tasTarget,
    boolean equal)
  {
    
    String op = equal ? "=" : "<>";
    
    
    if(node.isToken() && target.isToken())
    {
      // join on span
      conditions.add(tasNode.aliasedColumn(NODE_TABLE, "span") 
        + " " + op + " " + tasTarget.aliasedColumn(NODE_TABLE, "span"));
    }
    else if(haveSameNodeAnnotationDefinitions(
      node.getNodeAnnotations(), target.getNodeAnnotations()))
    {
      // join on node_anno_ref
      conditions.add(tasNode.aliasedColumn(NODE_TABLE, "node_anno_ref") 
        + " " + op + " " + tasTarget.aliasedColumn(NODE_TABLE, "node_anno_ref"));
    }
    else
    {
      // most complex query, join on the actual value
      String left;
      if(node.isToken())
      {
        left = tasNode.aliasedColumn(NODE_TABLE, "span");
      }
      else
      {
        left = "getAnnoValue(" 
          + tasNode.aliasedColumn(NODE_TABLE, "node_anno_ref") + ", "
          + tasNode.aliasedColumn(NODE_TABLE, "toplevel_corpus") + ", "
          + "'node')";
      }
      String right;
      if(target.isToken())
      {
        right = tasTarget.aliasedColumn(NODE_TABLE, "span");
      }
      else
      {
        right = "getAnnoValue(" 
          + tasTarget.aliasedColumn(NODE_TABLE, "node_anno_ref") + ", "
          + tasTarget.aliasedColumn(NODE_TABLE, "toplevel_corpus") + ", "
          + "'node')";
      }
      conditions.add(left + " " + op + " " + right);
    }
  }
  
  private boolean haveSameNodeAnnotationDefinitions(Set<QueryAnnotation> sourceAnnos, 
    Set<QueryAnnotation> targetAnnos)
  {
    if(sourceAnnos != null && targetAnnos != null 
      && sourceAnnos.size() == 1 && targetAnnos.size() == 1)
    {
      QueryAnnotation anno1 = sourceAnnos.iterator().next();
      QueryAnnotation anno2 = targetAnnos.iterator().next();
      
      if(Objects.equal(anno1.getNamespace(), anno2.getNamespace()) 
        && Objects.equal(anno1.getName(), anno2.getName()))
      {
        return true;
      }
    }
    
    return false;
  }
}
