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
package annis.sqlgen;

import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.util.List;

/**
 * Implements method to allow different modes to have different behavior
 * how to add annotation constraints to the generated SQL.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface AnnotationConditionProvider
{

  /**
   * Adds annotation conditions for a single node.
   * @param conditions Condition list where the conditions should be added to
   * @param node Node which has annotations
   * @param index Index for a specific annotation
   * @param annotation The annotation to add
   * @param table Table to operate on
   * @param queryData {@link QueryData} of the query
   * @param tas {@link TableAccessStrategy} for the given node.
   */
  public void addAnnotationConditions(List<String> conditions,
    QueryNode node, int index, QueryAnnotation annotation, String table,
    QueryData queryData, TableAccessStrategy tas);
  
  public void addEqualValueConditions(List<String> conditions,
    QueryNode node, QueryNode target, TableAccessStrategy tasNode, TableAccessStrategy tasTarget, 
    boolean equal);
  
  public String getNodeAnnoNamespaceSQL(TableAccessStrategy tas);
  public String getNodeAnnoNameSQL(TableAccessStrategy tas);
  
}
