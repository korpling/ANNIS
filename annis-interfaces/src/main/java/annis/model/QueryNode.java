/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
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
package annis.model;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class QueryNode implements Serializable
{
  // this class is send to the front end
  // node object in database
  private long id;
  private String spannedText;
  private Set<QueryAnnotation> nodeAnnotations;
  // node constraints
  private boolean token;
  private TextMatching spanTextMatching;
  private String variable;
  private boolean artificial;
  private Integer alternativeNumber;
  private ParsedEntityLocation parseLocation;
  
  public enum TextMatching
  {

    EXACT_EQUAL("=", "\"", "=", false, false), 
    REGEXP_EQUAL("~", "/", "=", true, false), 
    EXACT_NOT_EQUAL("<>", "\"", "!=", false, true), 
    REGEXP_NOT_EQUAL("!~", "/", "!=", true, true);
    
    private String sqlOperator;
    private String annisQuote;
    private String aqlOperator;
    private boolean regex;
    private boolean negated;

    private TextMatching(String sqlOperator, String annisQuote, 
      String aqlOperator, boolean regex, boolean negated)
    {
      this.sqlOperator = sqlOperator;
      this.annisQuote = annisQuote;
      this.aqlOperator = aqlOperator;
      this.regex = regex;
      this.negated = negated;
    }

    @Override
    public String toString()
    {
      return sqlOperator;
    }

    public String sqlOperator()
    {
      return sqlOperator;
    }

    public String quote()
    {
      return annisQuote;
    }
    
    public String aqlOperator()
    {
      return aqlOperator;
    }

    public boolean isRegex()
    {
      return regex;
    }

    public boolean isNegated()
    {
      return negated;
    }
    
    
  };

  public static class Range implements Serializable
  {

    private int min;
    private int max;

    public Range()
    {
      this(0,0);
    }
    
    public Range(int _min, int _max)
    {
      min = _min;
      max = _max;
    }

    public int getMin()
    {
      return min;
    }

    public int getMax()
    {
      return max;
    }

    @Override
    public String toString()
    {
      return min + "," + max;
    }
    
    

    @Override
    public int hashCode()
    {
      return new HashCodeBuilder().append(min).append(max).toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj instanceof Range)
      {
        Range other = (Range) obj;

        return new EqualsBuilder().append(min, other.min).append(max, other.max).
          isEquals();
      }
      return false;
    }
  };

  public QueryNode()
  {
    nodeAnnotations = new TreeSet<>();
  }

  public QueryNode(long id)
  {
    this();
    this.id = id;
  }
  
  /**
   * Copy constructor
   * @param other other node to copy from
   */
  public QueryNode(QueryNode other)
  {
    this.id = other.id;
    this.nodeAnnotations = new TreeSet<>(other.nodeAnnotations);
    this.spanTextMatching = other.spanTextMatching;
    this.spannedText = other.spannedText;
    this.token = other.token;
    this.variable = other.variable;
  }
  
  /**
   * Copy constructor that allows to change the ID.
   * @param newId new ID
   * @param other other node to copy from 
   */
  public QueryNode(long newId, QueryNode other)
  {
    this(other);
    this.id = newId;
  }


  public static String qName(String namespace, String name)
  {
    return name == null ? null : (namespace == null ? name : namespace
      + ":" + name);
  }

  public void setSpannedText(String span)
  {
    setSpannedText(span, TextMatching.EXACT_EQUAL);
  }

  public void setSpannedText(String spannedText, TextMatching textMatching)
  {
    if (spannedText != null)
    {
      Validate.notNull(textMatching);
    }
    this.spannedText = spannedText;
    this.spanTextMatching = textMatching;
  }

  public void clearSpannedText()
  {
    this.spannedText = null;
    this.spanTextMatching = null;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("node ");
    sb.append(id);

    if (variable != null)
    {
      sb.append("; bound to '");
      sb.append(variable);
      sb.append("'");
    }

    if (token)
    {
      sb.append("; is a token");
    }

    if (spannedText != null)
    {
      sb.append("; spans");
      String op = spanTextMatching != null ? spanTextMatching.sqlOperator()
        : " ";
      String quote = spanTextMatching != null ? spanTextMatching.quote()
        : "?";
      sb.append(op);
      sb.append(quote);
      sb.append(spannedText);
      sb.append(quote);
    }

    if (!nodeAnnotations.isEmpty())
    {
      sb.append("; node labels: ");
      sb.append(nodeAnnotations);
    }

    return sb.toString();
  }
  
  public String toAQLNodeFragment()
  {
    StringBuilder sb = new StringBuilder();

    boolean foundConstraint = false;
    
    // check if we were given a custom name
    String idAsString = "" + id;
    if(variable != null && !idAsString.equals(variable))
    {
      sb.append(variable).append("#");
    }
    
    if (token)
    {
      sb.append("tok");
      foundConstraint = true;
    }

    if (spannedText != null && spanTextMatching != null)
    {
      if(token)
      {
        sb.append(spanTextMatching.aqlOperator());
      }

      sb.append(spanTextMatching.quote());
      sb.append(spannedText);
      sb.append(spanTextMatching.quote());
      foundConstraint = true;
    }


    if (nodeAnnotations.isEmpty())
    {
      if(!foundConstraint)
      {
        sb.append("node");
      }
    }
    else
    {
      QueryAnnotation anno=nodeAnnotations
        .toArray(new QueryAnnotation[nodeAnnotations.size()])[0];

      if(anno.getNamespace() == null || "".equals(anno.getNamespace()))
      {
        sb.append(anno.getName());
      }
      else
      {
        sb.append(anno.getQualifiedName());
      }
      
      if(anno.getTextMatching() != null && anno.getValue() != null)
      {
        sb.append(anno.getTextMatching().aqlOperator);
        sb.append(anno.getTextMatching().quote());
        sb.append(anno.getValue());
        sb.append(anno.getTextMatching().quote());
      }
    }

    
    return sb.toString();
  }

  public boolean addNodeAnnotation(QueryAnnotation annotation)
  {
    return nodeAnnotations.add(annotation);
  }
  
  
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final QueryNode other = (QueryNode) obj;
    if (this.id != other.id)
    {
      return false;
    }
    
    if ((this.spannedText == null) ? (other.spannedText != null)
      : !this.spannedText.equals(other.spannedText))
    {
      return false;
    }
    
    if (this.nodeAnnotations != other.nodeAnnotations
      && (this.nodeAnnotations == null || !this.nodeAnnotations.equals(
      other.nodeAnnotations)))
    {
      return false;
    }
    
    if (this.token != other.token)
    {
      return false;
    }
    if (this.spanTextMatching != other.spanTextMatching)
    {
      return false;
    }
    if ((this.variable == null) ? (other.variable != null) : !this.variable.
      equals(other.variable))
    {
      return false;
    }
    return true;
  }

  
  @Override
  public int hashCode()
  {
    return (int) id;
  }
  
 
  // /// Getter / Setter  
  public String getSpannedText()
  {
    return spannedText;
  }

  public TextMatching getSpanTextMatching()
  {
    return spanTextMatching;
  }

  public Set<QueryAnnotation> getNodeAnnotations()
  {
    return nodeAnnotations;
  }

  public void setNodeAnnotations(Set<QueryAnnotation> nodeAnnotations)
  {
    this.nodeAnnotations = nodeAnnotations;
  }

  public String getVariable()
  {
    return variable;
  }

  public void setVariable(String variable)
  {
    this.variable = variable;
  }

  public long getId()
  {
    return id;
  }
  
  public void setId(long id)
  {
    this.id = id;
  }
  
  
  public boolean isToken()
  {
    return token;
  }

  public void setToken(boolean token)
  {
    this.token = token;
  }


  /**
   * Returns if this query node was artificially created by some normalization process.
   * @return True if artificially created.
   */
  public boolean isArtificial()
  {
    return artificial;
  }

  public void setArtificial(boolean artificial)
  {
    this.artificial = artificial;
  }

  /**
   * If set return the number of the (normalized) alternative this node belongs to.
   * @return the number of the alternative
   */
  public Integer getAlternativeNumber()
  {
    return alternativeNumber;
  }

  public void setAlternativeNumber(Integer alternativeNumber)
  {
    this.alternativeNumber = alternativeNumber;
  }

  public ParsedEntityLocation getParseLocation()
  {
    return parseLocation;
  }

  public void setParseLocation(ParsedEntityLocation parseLocation)
  {
    this.parseLocation = parseLocation;
  }
  
  
}
