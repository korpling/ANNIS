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
package annis.ql.parser;

import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.slf4j.LoggerFactory;

public class QueryData implements Cloneable
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    QueryData.class);

  private List<List<QueryNode>> alternatives;

  private List<Long> corpusList;

  private List<Long> documents;

  private List<QueryAnnotation> metaData;

  private int maxWidth;

  private Set<Object> extensions;

  private HashMap<Long, Properties> corpusConfiguration;

  public QueryData()
  {
    alternatives = new ArrayList<>();
    corpusList = new ArrayList<>();
    documents = new ArrayList<>();
    metaData = new ArrayList<>();
    extensions = new HashSet<>();
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    Iterator<List<QueryNode>> itOr = getAlternatives().iterator();

    sb.append("ALTERNATIVES\n");
    while (itOr.hasNext())
    {
      sb.append("\t");
      List<QueryNode> nextNodes = itOr.next();
      Iterator<QueryNode> itAnd = nextNodes.iterator();
      while (itAnd.hasNext())
      {
        sb.append("{").append(itAnd.next());
        sb.append("}");
        if (itAnd.hasNext())
        {
          sb.append(" AND ");
        }
      }

      if (itOr.hasNext())
      {
        sb.append("\n");
      }
    }
    Iterator<QueryAnnotation> itMeta = getMetaData().iterator();
    if (itMeta.hasNext())
    {
      sb.append("META");
      sb.append("\n");
    }
    while (itMeta.hasNext())
    {
      sb.append("\t").append(itMeta.next().toString());
      sb.append("\n");
    }
    if (!extensions.isEmpty())
    {
      sb.append("EXTENSIONS\n");
    }
    for (Object extension : extensions)
    {
      String toString = extension.toString();
      if (!"".equals(toString))
      {
        sb.append("\t" + toString + "\n");
      }
    }

    return sb.toString();
  }

  /**
   * Outputs this alternative as an equivalent AQL query.
   *
   * @param alternative
   * @return
   */
  public static String toAQL(List<QueryNode> alternative)
  {
    List<String> fragments = new LinkedList<>();
    
    for(QueryNode n : alternative)
    {
      String frag = n.toAQLNodeFragment();
      if(frag != null && !frag.isEmpty())
      {
        fragments.add(frag);
      }
    }
    
    for(QueryNode n : alternative)
    {
      String frag = n.toAQLEdgeFragment();
      if(frag != null && !frag.isEmpty())
      {
        fragments.add(frag);
      }
    }
    
    return Joiner.on(" & ").join(fragments);
  }

  /**
   * Outputs this normalized query data as an equivalent AQL query.
   *
   * @return
   */
  public String toAQL()
  {
    StringBuilder sb = new StringBuilder();
    Iterator<List<QueryNode>> itAlternative = alternatives.iterator();


    while (itAlternative.hasNext())
    {
      List<QueryNode> alt = itAlternative.next();

      if (alternatives.size() > 1)
      {
        sb.append("(");
      }

      sb.append(toAQL(alt));

      if (alternatives.size() > 1)
      {
        sb.append(")");
        if (itAlternative.hasNext())
        {
          sb.append("\n|\n");
        }
      }
    }
    // TODO: add metadata

    return sb.toString();
  }

  public List<List<QueryNode>> getAlternatives()
  {
    return alternatives;
  }

  public void setAlternatives(List<List<QueryNode>> alternatives)
  {
    this.alternatives = alternatives;
  }

  public List<Long> getCorpusList()
  {
    return corpusList;
  }

  public void setCorpusList(List<Long> corpusList)
  {
    this.corpusList = corpusList;
  }

  public List<QueryAnnotation> getMetaData()
  {
    return metaData;
  }

  public void setMetaData(List<QueryAnnotation> metaData)
  {
    this.metaData = metaData;
  }

  public int getMaxWidth()
  {
    return maxWidth;
  }

  public void setMaxWidth(int maxWidth)
  {
    this.maxWidth = maxWidth;
  }

  public boolean addAlternative(List<QueryNode> nodes)
  {
    return alternatives.add(nodes);
  }

  public boolean addMetaAnnotations(List<QueryAnnotation> annotations)
  {
    return metaData.addAll(annotations);
  }

  // FIXME: warum diese spezielle clone-Funktion?
  @Override
  public QueryData clone()
  {
    try
    {
      return (QueryData) super.clone();
    }
    catch (CloneNotSupportedException ex)
    {
      log.error(null, ex);
      throw new InternalError("could not clone QueryData");
    }
  }

  public List<Long> getDocuments()
  {
    return documents;
  }

  public void setDocuments(List<Long> documents)
  {
    this.documents = documents;
  }

  public Set<Object> getExtensions()
  {
    return extensions;
  }

  public <T> List<T> getExtensions(Class<T> clazz)
  {
    List<T> result = new LinkedList<>();

    for (Object o : extensions)
    {
      if (clazz.isInstance(o))
      {
        result.add((T) o);
      }
    }

    return result;
  }

  public boolean addExtension(Object extension)
  {
    return extensions.add(extension);
  }

  public HashMap<Long, Properties> getCorpusConfiguration()
  {
    return corpusConfiguration;
  }

  public void setCorpusConfiguration(
    HashMap<Long, Properties> corpusConfiguration)
  {
    this.corpusConfiguration = corpusConfiguration;
  }
}