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
package annis.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class LogicClause
{
  public static enum Operator {AND, OR, LEAF}
  
  private Operator op;
  private List<LogicClause> children;
  private QueryNode content;
  private LogicClause parent;

  public LogicClause()
  {
    this.op = Operator.LEAF;
    this.children = new ArrayList<LogicClause>();
    this.content = null;
    this.parent = null;
  }

  public LogicClause(Operator op)
  {
    this();
    this.op = op;
  }

  

  public Operator getOp()
  {
    return op;
  }

  public void setOp(Operator op)
  {
    this.op = op;
  }

  public List<LogicClause> getChildren()
  {
    return children;
  }

  public void setChildren(
    List<LogicClause> children)
  {
    this.children = children;
  }

  public QueryNode getContent()
  {
    return content;
  }

  public void setContent(QueryNode content)
  {
    this.content = content;
  }

  public LogicClause getParent()
  {
    return parent;
  }

  public void setParent(LogicClause parent)
  {
    this.parent = parent;
  }

}
