/*
 * Copyright 2013 SFB 632.
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
package annis.examplequeries;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wraps example queries.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@XmlRootElement
public class ExampleQuery implements Serializable
{

  private String type;

  private String description;

  private String exampleQuery;

  private String used_operators;

  private String corpusName;

  // the amount of nodes of the example query
  private int nodes;

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getExampleQuery()
  {
    return exampleQuery;
  }

  public void setExampleQuery(String exampleQuery)
  {
    this.exampleQuery = exampleQuery;
  }

  public String getUsedOperators()
  {
    return used_operators;
  }

  public void setUsedOperators(String used_operators)
  {
    this.used_operators = used_operators;
  }

  /**
   * @return the corpusName
   */
  public String getCorpusName()
  {
    return corpusName;
  }

  /**
   * @param corpusName the corpusName to set
   */
  public void setCorpusName(String corpusName)
  {
    this.corpusName = corpusName;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("example query: ").append(exampleQuery).append("\n");
    sb.append("example corpusName: ").append(corpusName).append("\n");
    sb.append("description: ").append(description).append("\n");
    sb.append("used operators: ").append(used_operators).append("\n");
    sb.append("nodes: ").append(nodes).append("\n");
    return sb.toString();
  }

  /**
   * @return the nodes
   */
  public int getNodes()
  {
    return nodes;
  }

  /**
   * @param nodes the nodes to set
   */
  public void setNodes(int nodes)
  {
    this.nodes = nodes;
  }
}
