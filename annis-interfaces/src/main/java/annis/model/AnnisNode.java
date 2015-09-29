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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


@SuppressWarnings("serial")
public class AnnisNode implements Serializable
{

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
  private Set<Annotation> nodeAnnotations;
  // annotation graph
  private AnnotationGraph graph;
  // node position in annotation graph
  private Set<Edge> incomingEdges;
  private Set<Edge> outgoingEdges;
  private String name;
  private String namespace;
  // node constraints
  private boolean partOfEdge;
  private boolean root;
  private boolean token;
  private Set<Annotation> edgeAnnotations;
  private Long matchedNodeInQuery;


  public static class Range implements Serializable
  {

    private int min;
    private int max;

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

  public AnnisNode()
  {
    nodeAnnotations = new TreeSet<>();
    edgeAnnotations = new TreeSet<>();
    incomingEdges = new HashSet<>();
    outgoingEdges = new HashSet<>();
  }

  public AnnisNode(long id)
  {
    this();
    this.id = id;
  }

  public AnnisNode(long id, long corpusRef, long textRef, long left,
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

    setSpannedText(span);
  }
  
  public static String qName(String namespace, String name)
  {
    return qName(namespace, name, ":");
  }

  public static String qName(String namespace, String name, String seperator)
  {
    return name == null ? null : (namespace == null ? name : namespace
      + seperator + name);
  }

  public void setSpannedText(String spannedText)
  {
    this.spannedText = spannedText;
  }

  public void clearSpannedText()
  {
    this.spannedText = null;
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();

    sb.append("node ");
    sb.append(id);


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
      sb.append("; spans=\"");
      sb.append(spannedText);
      sb.append("\"");
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
    
    return sb.toString();
  }

  public boolean addIncomingEdge(Edge edge)
  {
    return incomingEdges.add(edge);
  }

  public boolean addOutgoingEdge(Edge edge)
  {
    return outgoingEdges.add(edge);
  }

  public boolean addEdgeAnnotation(Annotation annotation)
  {
    return edgeAnnotations.add(annotation);
  }

  public boolean addNodeAnnotation(Annotation annotation)
  {
    return nodeAnnotations.add(annotation);
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
    final AnnisNode other = (AnnisNode) obj;
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
    
    if (this.edgeAnnotations != other.edgeAnnotations
      && (this.edgeAnnotations == null || !this.edgeAnnotations.equals(other.edgeAnnotations)))
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
  public Set<Annotation> getEdgeAnnotations()
  {
    return edgeAnnotations;
  }

  public void setEdgeAnnotations(Set<Annotation> edgeAnnotations)
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

  public Set<Annotation> getNodeAnnotations()
  {
    return nodeAnnotations;
  }

  public void setNodeAnnotations(Set<Annotation> nodeAnnotations)
  {
    this.nodeAnnotations = nodeAnnotations;
  }

  public long getId()
  {
    return id;
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

  public Set<Edge> getIncomingEdges()
  {
    return incomingEdges;
  }

  public void setIncomingEdges(Set<Edge> incomingEdges)
  {
    this.incomingEdges = incomingEdges;
  }

  public Set<Edge> getOutgoingEdges()
  {
    return outgoingEdges;
  }

  public void setOutgoingEdges(Set<Edge> outgoingEdges)
  {
    this.outgoingEdges = outgoingEdges;
  }

  public AnnotationGraph getGraph()
  {
    return graph;
  }

  public void setGraph(AnnotationGraph graph)
  {
    this.graph = graph;
  }

  public Long getMatchedNodeInQuery()
  {
    return matchedNodeInQuery;
  }

  public void setMatchedNodeInQuery(Long matchedNodeInQuery)
  {
    this.matchedNodeInQuery = matchedNodeInQuery;
  }
  
  

}
