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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import annis.sqlgen.model.Join;
import annis.sqlgen.model.RankTableJoin;
import com.google.common.base.Joiner;
import java.util.LinkedList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class QueryNode implements Serializable
{
  public enum Type {NODE, AND, OR};

  // this class is send to the front end
  // node object in database
  private long id;
  private long corpus; // FIXME: Corpus object with annotations or move to
  // graph?
  private long textId;
  private long left;
  private long right;
  private String spannedText;
  private Long tokenIndex;
  private long leftToken;
  private long rightToken;
  private Set<QueryAnnotation> nodeAnnotations;
  // node position in annotation graph
  private String name;
  private String namespace;
  // node constraints
  private boolean partOfEdge;
  private boolean root;
  private boolean token;
  private TextMatching spanTextMatching;
  private List<Join> joins;
  private String variable;
  private Set<QueryAnnotation> edgeAnnotations;
  private Range arity;
  private Range tokenArity;
  private Long matchedNodeInQuery;
  private boolean artificial;
  
  public enum TextMatching
  {

    EXACT_EQUAL("=", "\"", "="), 
    REGEXP_EQUAL("~", "/", "="), 
    EXACT_NOT_EQUAL("<>", "\"", "!="), 
    REGEXP_NOT_EQUAL("!~", "/", "!=");
    
    private String sqlOperator;
    private String annisQuote;
    private String aqlOperator;

    private TextMatching(String sqlOperator, String annisQuote, 
      String aqlOperator)
    {
      this.sqlOperator = sqlOperator;
      this.annisQuote = annisQuote;
      this.aqlOperator = aqlOperator;
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
    nodeAnnotations = new TreeSet<QueryAnnotation>();
    edgeAnnotations = new TreeSet<QueryAnnotation>();
    joins = new ArrayList<Join>();
  }

  public QueryNode(long id)
  {
    this();
    this.id = id;
  }
  
  /**
   * Copy constructor
   * @param other 
   */
  public QueryNode(QueryNode other)
  {
    this.arity = other.arity;
    this.corpus = other.corpus;
    this.edgeAnnotations = new TreeSet<QueryAnnotation>(other.edgeAnnotations);
    this.id = other.id;
    this.joins = new ArrayList<Join>(other.joins);
    this.left = other.left;
    this.leftToken = other.leftToken;
    this.matchedNodeInQuery = other.matchedNodeInQuery;
    this.name = other.name;
    this.namespace = other.namespace;
    this.nodeAnnotations = new TreeSet<QueryAnnotation>(other.nodeAnnotations);
    this.partOfEdge = other.partOfEdge;
    this.right = other.right;
    this.rightToken = other.rightToken;
    this.root = other.root;
    this.spanTextMatching = other.spanTextMatching;
    this.spannedText = other.spannedText;
    this.textId = other.textId;
    this.token = other.token;
    this.tokenArity = other.tokenArity;
    this.tokenIndex = other.tokenIndex;
    this.variable = other.variable;
  }
  
  /**
   * Copy constructor that allows to change the ID.
   * @param newId
   * @param other 
   */
  public QueryNode(long newId, QueryNode other)
  {
    this(other);
    this.id = newId;
  }

  public QueryNode(long id, long corpusRef, long textRef, long left,
    long right, String namespace, String name, long tokenIndex,
    String span, long leftToken, long rightToken)
  {
    this(id);

    this.corpus = corpusRef;
    this.textId = textRef;
    this.left = left;
    this.right = right;
    this.leftToken = leftToken;
    this.rightToken = rightToken;

    setNamespace(namespace);
    setName(name);
    setTokenIndex(tokenIndex);

    setSpannedText(span, TextMatching.EXACT_EQUAL);
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

    if (name != null)
    {
      sb.append("; named '");
      sb.append(qName(namespace, name));
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

    if (isRoot())
    {
      sb.append("; root node");
    }

    if (!nodeAnnotations.isEmpty())
    {
      sb.append("; node labels: ");
      sb.append(nodeAnnotations);
    }

    if (!edgeAnnotations.isEmpty())
    {
      sb.append("; edge labes: ");
      sb.append(edgeAnnotations);
    }

    for (Join join : joins)
    {
      sb.append("; ");
      sb.append(join);
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

      sb.append(anno.getQualifiedName());
      
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
  
  public String toAQLEdgeFragment()
  {
    List<String> frags = new LinkedList<String>();
    for (Join join : joins)
    {
      frags.add(join.toAQLFragment(this));
    }
    return Joiner.on(" & ").join(frags);
  }

  public boolean addEdgeAnnotation(QueryAnnotation annotation)
  {
    return edgeAnnotations.add(annotation);
  }

  public boolean addNodeAnnotation(QueryAnnotation annotation)
  {
    return nodeAnnotations.add(annotation);
  }
  
  public boolean addJoin(Join join)
  {
    boolean result = joins.add(join);

    if (join instanceof RankTableJoin)
    {
      this.setPartOfEdge(true);

      QueryNode target = join.getTarget();
      target.setPartOfEdge(true);
    }

    return result;
  }
  
  public String getQualifiedName()
  {
    return qName(namespace, name);
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
    if (this.corpus != other.corpus)
    {
      return false;
    }
    if (this.textId != other.textId)
    {
      return false;
    }
    if (this.left != other.left)
    {
      return false;
    }
    if (this.right != other.right)
    {
      return false;
    }
    if ((this.spannedText == null) ? (other.spannedText != null)
      : !this.spannedText.equals(other.spannedText))
    {
      return false;
    }
    if (this.leftToken != other.leftToken)
    {
      return false;
    }
    if (this.nodeAnnotations != other.nodeAnnotations
      && (this.nodeAnnotations == null || !this.nodeAnnotations.equals(
      other.nodeAnnotations)))
    {
      return false;
    }
    if ((this.name == null) ? (other.name != null) : !this.name.equals(
      other.name))
    {
      return false;
    }
    if ((this.namespace == null) ? (other.namespace != null)
      : !this.namespace.equals(other.namespace))
    {
      return false;
    }
    if (this.partOfEdge != other.partOfEdge)
    {
      return false;
    }
    if (this.root != other.root)
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
    if (this.joins != other.joins
      && (this.joins == null || !this.joins.equals(other.joins)))
    {
      return false;
    }
    if ((this.variable == null) ? (other.variable != null) : !this.variable.
      equals(other.variable))
    {
      return false;
    }
    if (this.edgeAnnotations != other.edgeAnnotations
      && (this.edgeAnnotations == null || !this.edgeAnnotations.equals(
      other.edgeAnnotations)))
    {
      return false;
    }
    if ((this.arity == null) ? (other.arity != null) : !this.arity.equals(
      other.arity))
    {
      return false;
    }
    if ((this.tokenArity == null) ? (other.tokenArity != null)
      : !this.tokenArity.equals(other.tokenArity))
    {
      return false;
    }

    return true;
  }

  // @Override
  // public boolean equals(Object obj) {
  // if (obj == null || !(obj instanceof AnnisNode))
  // return false;
  //
  // AnnisNode other = (AnnisNode) obj;
  //
  // return new EqualsBuilder()
  // .append(this.id, other.id)
  // .append(this.corpus, other.corpus)
  // .append(this.textId, other.textId)
  // .append(this.left, other.left)
  // .append(this.right, other.right)
  // .append(this.spannedText, other.spannedText)
  // .append(this.leftToken, other.leftToken)
  // .append(this.nodeAnnotations, other.nodeAnnotations)
  // .append(this.name, other.name)
  // .append(this.namespace, other.namespace)
  // .append(this.partOfEdge, other.partOfEdge)
  // .append(this.root, other.root)
  // .append(this.token, other.token)
  // .append(this.spanTextMatching, other.spanTextMatching)
  // .append(this.joins, other.joins)
  // .append(this.variable, other.variable)
  // .append(this.edgeAnnotations, other.edgeAnnotations)
  // .append(this.marker, other.marker)
  // .isEquals();
  // }
  //
  @Override
  public int hashCode()
  {
    return (int) id;
  }

  // /// Getter / Setter
  public Set<QueryAnnotation> getEdgeAnnotations()
  {
    return edgeAnnotations;
  }

  public void setEdgeAnnotations(Set<QueryAnnotation> edgeAnnotations)
  {
    this.edgeAnnotations = edgeAnnotations;
  }

  public boolean isRoot()
  {
    return root;
  }

  public void setRoot(boolean root)
  {
    this.root = root;
  }
  
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getNamespace()
  {
    return namespace;
  }

  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }

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

  @XmlTransient // currently not supported, might be added later
  public List<Join> getJoins()
  {
    return joins;
  }
  public boolean isToken()
  {
    return token;
  }

  public void setToken(boolean token)
  {
    this.token = token;
  }

  public boolean isPartOfEdge()
  {
    return partOfEdge;
  }

  public void setPartOfEdge(boolean partOfEdge)
  {
    this.partOfEdge = partOfEdge;
  }

  public long getCorpus()
  {
    return corpus;
  }

  public void setCorpus(long corpus)
  {
    this.corpus = corpus;
  }

  public long getTextId()
  {
    return textId;
  }

  public void setTextId(long textIndex)
  {
    this.textId = textIndex;
  }

  public long getLeft()
  {
    return left;
  }

  public void setLeft(long left)
  {
    this.left = left;
  }

  public long getRight()
  {
    return right;
  }

  public void setRight(long right)
  {
    this.right = right;
  }

  public Long getTokenIndex()
  {
    return tokenIndex;
  }

  public void setTokenIndex(Long tokenIndex)
  {
    this.tokenIndex = tokenIndex;
    // FIXME: vermengung von node und constraint semantik
    setToken(tokenIndex != null);
  }

  public long getLeftToken()
  {
    return leftToken;
  }

  public void setLeftToken(long leftToken)
  {
    this.leftToken = leftToken;
  }

  public long getRightToken()
  {
    return rightToken;
  }

  public void setRightToken(long rightToken)
  {
    this.rightToken = rightToken;
  }

  public Range getArity()
  {
    return arity;
  }

  public void setArity(Range arity)
  {
    this.arity = arity;
  }

  public Range getTokenArity()
  {
    return tokenArity;
  }

  public void setTokenArity(Range tokenArity)
  {
    this.tokenArity = tokenArity;
  }

  public Long getMatchedNodeInQuery()
  {
    return matchedNodeInQuery;
  }

  public void setMatchedNodeInQuery(Long matchedNodeInQuery)
  {
    this.matchedNodeInQuery = matchedNodeInQuery;
  }

  /**
   * Returns if this query node was artificially created by some normalization process.
   * @return 
   */
  public boolean isArtificial()
  {
    return artificial;
  }

  public void setArtificial(boolean artificial)
  {
    this.artificial = artificial;
  }

  
}
