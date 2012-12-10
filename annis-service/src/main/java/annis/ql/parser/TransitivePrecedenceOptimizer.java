/*
 * Copyright 2009-2012 Collaborative Research Centre SFB 632 
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
package annis.ql.parser;

/**
 * Extends precedence relations to other nodes that are only transitivly connected.
 * 
 * The algorithm calculates the reachability graph for each node of the query
 * (as defined by the precedence operator) and inherits and extends the precedence
 * property to the nodes connected with this node. The Goal is to preserve as
 * much restrictive information as possible. 
 * 
 * Breadth-first search is used in order to find the shortest precedence 
 * relation between nodes . This is just an approximation since beeing near in 
 * the reachability graph does not necessary mean the relation is more 
 * restrictive than a relation with more edges. Still it is assumed that
 * "normal" AQL queries will satisfiy this condition. And in the end, even
 * a "is after this token somewhere in the text" condition is a huge improvement.
 * 
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class TransitivePrecedenceOptimizer implements QueryDataTransformer
{
  
  public TransitivePrecedenceOptimizer()
  {

  }
  @Override
  public QueryData transform(QueryData data)
  {
    
    QueryData result = data.clone();
    
    return result;
  
  }
  
  
}
