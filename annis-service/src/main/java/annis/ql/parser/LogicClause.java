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
package annis.ql.parser;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.Token;
/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class LogicClause
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
  private List<LogicClause> children;
  private List<? extends Token> content;
  private LogicClause parent;

  /**
   * Default constructor. Will create a LogicClause which is a leaf and has
   * no content 
   */
  public LogicClause()
  {
    this.op = Operator.LEAF;
    this.children = new ArrayList<>();
    this.content = null;
    this.parent = null;
  }

  public LogicClause(Operator op)
  {
    this();
    this.op = op;
  }

  /**
   * Copy constructor
   * @param other 
   */
  public LogicClause(LogicClause other)
  {
    this();
    this.op = other.op;
    this.parent = other.parent;
    this.content = new ArrayList<>(other.content);
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

  public ImmutableList<LogicClause> getChildren()
  {
    return ImmutableList.copyOf(children);
  }
  
  public void addAllChildren(Collection<LogicClause> children)
  {
    if(children != null)
    {
      for(LogicClause c : children)
      {
        addChild(c);
      }
    }
  }
  
  public void addChild(LogicClause child)
  {
    Preconditions.checkArgument(child != this, "Cannot add itself as children");
    child.parent = this;
    children.add(child);
  }
  
  public void addChild(int idx, LogicClause child)
  {
    Preconditions.checkArgument(child != this, "Cannot add itself as children");
    child.parent = this;
    children.add(idx, child);
  }
  
  public LogicClause removeChild(int idx)
  {
    LogicClause result = children.remove(idx);
    if(result != null && result.parent == this)
    {
      result.parent = null;
    }
    return result;
  }
  
  public void clearChildren()
  {
    for(LogicClause c : children)
    {
      if(c.parent == this)
      {
        c.parent = null;
      }
    }
    children.clear();
  }
  

  public List<? extends Token> getContent()
  {
    return content;
  }

  public void setContent(List<? extends Token> content)
  {
    this.content = content;
  }
  
  public List<Token> getCoveredToken()
  {
    List<Token> result = new LinkedList<>();
    
    if(content != null && !content.isEmpty()
      && (op == Operator.AND || op == Operator.OR))
    {
      // add children and put our own operator between them
      
      Iterator<LogicClause> itChild = children.iterator();
      while(itChild.hasNext())
      {
        result.addAll(itChild.next().getCoveredToken());
        if(itChild.hasNext())
        {
          result.addAll(content);
        }
      }
    }
    else if(op == Operator.LEAF && content != null)
    {
      result.addAll(content);
    }
    else
    {
      // fallback: this node has no own token but it's children might have
      for(LogicClause child : children)
      {
        result.addAll(child.getCoveredToken());
      }
    }

    return result;
  }

  public LogicClause getParent()
  {
    return parent;
  }

  @Override
  public String toString()
  {
    if(op == Operator.AND)
    {
      return "(" + Joiner.on(" & ").join(children) + ")";
    }
    else if(op == Operator.OR)
    {
      return "(" + Joiner.on(" \n| \n").join(children) + ")";
    }
    
    LinkedList<String> texts = new LinkedList<>();
    if(content != null)
    {
      for(Token t : content)
      {
        texts.add(t.getText());
      }
    }
    return Joiner.on(" ").join(texts);
  }

}
