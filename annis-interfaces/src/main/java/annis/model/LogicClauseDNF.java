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

import annis.sqlgen.model.Join;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class LogicClauseDNF
{
  public enum Operator {
    AND, OR, LEAF;

    @Override
    public String toString()
    {
      return super.toString();
    }
  }
  
  private Operator op;
  private List<LogicClauseDNF> children;
  private QueryNode content;
  private Join join;
  private LogicClauseDNF parent;

  /**
   * Default constructor. Will create a LogicClause which is a leaf and has
   * no content 
   */
  public LogicClauseDNF()
  {
    this.op = Operator.LEAF;
    this.children = new ArrayList<LogicClauseDNF>();
    this.content = null;
    this.parent = null;
    this.join = null;
  }

  public LogicClauseDNF(Operator op)
  {
    this();
    this.op = op;
  }

  /**
   * Copy constructor
   * @param other 
   */
  public LogicClauseDNF(LogicClauseDNF other)
  {
    this();
    this.op = other.op;
    this.parent = other.parent;
    this.content = other.content;
    this.join = other.join;
    this.children.addAll(other.children);
  }

  public Operator getOp()
  {
    return op;
  }

  public void setOp(Operator op)
  {
    this.op = op;
  }

  public ImmutableList<LogicClauseDNF> getChildren()
  {
    return ImmutableList.copyOf(children);
  }
  
  public void addChild(LogicClauseDNF child)
  {
    Preconditions.checkArgument(child != this, "Cannot add itself as children");
    child.parent = this;
    children.add(child);
  }
  
  public void addChild(int idx, LogicClauseDNF child)
  {
    Preconditions.checkArgument(child != this, "Cannot add itself as children");
    child.parent = this;
    children.add(idx, child);
  }
  
  public LogicClauseDNF removeChild(int idx)
  {
    LogicClauseDNF result = children.remove(idx);
    if(result != null && result.parent == this)
    {
      result.parent = null;
    }
    return result;
  }
  
  public void clearChildren()
  {
    for(LogicClauseDNF c : children)
    {
      if(c.parent == this)
      {
        c.parent = null;
      }
    }
    children.clear();
  }
  

  public QueryNode getContent()
  {
    return content;
  }

  public void setContent(QueryNode content)
  {
    this.content = content;
  }

  public Join getJoin()
  {
    return join;
  }

  public void setJoin(Join join)
  {
    this.join = join;
  }
  
  public LogicClauseDNF getParent()
  {
    return parent;
  }

  @Override
  public String toString()
  {
    if(children.isEmpty())
    {
      return "{ op: " + op + "; content: " + content + "} ";
    }
    else
    {
      return "{ op: " + op + "; content: " + content 
        + "; children:" + children.toString() + " }";
    }
    
  }

  
  

}
