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
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import java.util.List;
/**
 *
 * @author thomas
 */
public class AtAnnotationConditionProvider implements
  AnnotationConditionProvider
{
  
  private final Escaper likeEscaper = Escapers.builder()
    .addEscape('\'', "''")
    .addEscape('%', "\\%")
    .addEscape('_', "\\_")
    .addEscape('\\', "\\\\")
    .build();
  
  private final Escaper regexEscaper = Escapers.builder()
    .addEscape('\'', "''")
    .build();
  

  @Override
  public void addAnnotationConditions(List<String> conditions, QueryNode node,
    int index, QueryAnnotation annotation, String table, QueryData queryData,
    TableAccessStrategy tas)
  {
    TextMatching tm = annotation.getTextMatching();
    
    String column = annotation.getNamespace() == null
       ? "annotext" : "qannotext";
    
    Escaper escaper = tm != null && tm.isRegex() ? regexEscaper : likeEscaper;
    
    String val;
    if(tm == null)
    {
      val = "%";
    }
    else
    {
      val = escaper.escape(annotation.getValue());
    }
    
    String prefix;
    if(annotation.getNamespace() == null)
    {
      prefix = escaper.escape(annotation.getName()) + ":";
    }
    else
    {
      prefix = escaper.escape(annotation.getNamespace())
        + ":" + escaper.escape(annotation.getName()) + ":";
    }
    
    if(tm == null || tm == TextMatching.EXACT_EQUAL)
    {
      conditions.add(tas.aliasedColumn(table, column) 
        + " LIKE '" + prefix + val + "'");
    }
    else if(tm == TextMatching.EXACT_NOT_EQUAL)
    {
      conditions.add(tas.aliasedColumn(table, column) 
        + " NOT LIKE '" + prefix + val + "'");
    }
    else if(tm == TextMatching.REGEXP_EQUAL)
    {
      conditions.add(tas.aliasedColumn(table, column) 
        + " ~ '^(" + prefix + val + ")$'");
    }
    else if(tm == TextMatching.REGEXP_NOT_EQUAL)
    {
      conditions.add(tas.aliasedColumn(table, column) 
        + " !~ '^(" + prefix + val + ")$'");
    }
  }

  @Override
  public void addEqualValueConditions(List<String> conditions, QueryNode node,
    QueryNode target, TableAccessStrategy tasNode, TableAccessStrategy tasTarget,
    boolean equal)
  {
    
    // TODO: implement equal value condition for annotext
    
  }
  
  
}
