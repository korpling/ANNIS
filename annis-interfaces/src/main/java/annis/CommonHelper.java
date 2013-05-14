/*
 * Copyright 2011 SFB 632.
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
package annis;

import annis.model.AnnisConstants;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SOrderRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities class for non-gui operations on Salt.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class CommonHelper
{

  private final static Logger log = LoggerFactory.getLogger(CommonHelper.class);

  public static boolean containsRTLText(String str)
  {
    if(str != null)
    {
      for (int i = 0; i < str.length(); i++)
      {
        char cc = str.charAt(i);
        // hebrew extended and basic, arabic basic and extendend
        if (cc >= 1425 && cc <= 1785)
        {
          return true;
        }
        // alphabetic presentations forms (hebrwew) to arabic presentation forms A
        else if (cc >= 64286 && cc <= 65019)
        {
          return true;
        }
        // arabic presentation forms B
        else if (cc >= 65136 && cc <= 65276)
        {
          return true;
        }
      }
    }
    return false;
  }

  public static List<SNode> getSortedSegmentationNodes(String segName, SDocumentGraph graph)
  {
    List<SNode> token = new ArrayList<SNode>();

    if (segName == null)
    {
      // if no segmentation is given just return the sorted token list
      token.addAll(graph.getSortedSTokenByText());
    }
    else
    {
      // get the very first node of the order relation chain
      Set<SNode> startNodes = new LinkedHashSet<SNode>();

      Map<SNode, SOrderRelation> outRelationForNode =
        new HashMap<SNode, SOrderRelation>();
      for (SOrderRelation rel : graph.getSOrderRelations())
      {
        if (rel.getSTypes() != null && rel.getSTypes().contains(segName))
        {
          SNode node = rel.getSSource();
          outRelationForNode.put(node, rel);

          EList<Edge> inEdgesForSource =
            graph.getInEdges(node.getSId());
          boolean hasInOrderEdge = false;
          for (Edge e : inEdgesForSource)
          {
            if (e instanceof SOrderRelation)
            {
              hasInOrderEdge = true;
              break;
            }
          } // for each ingoing edge

          if (!hasInOrderEdge)
          {
            startNodes.add(rel.getSSource());
          }
        } // end if type is segName
      } // end for all order relations of graph


      // add all nodes on the order relation chain beginning from the start node
      for (SNode s : startNodes)
      {
        SNode current = s;
        while (current != null)
        {
          token.add(current);
          if (outRelationForNode.containsKey(current))
          {
            current = outRelationForNode.get(current).getSTarget();
          }
          else
          {
            current = null;
          }
        }
      }
    }

    return token;
  }

  public static Set<String> getTokenAnnotationLevelSet(SDocumentGraph graph)
  {
    Set<String> result = new TreeSet<String>();

    if (graph != null)
    {
      for (SToken n : graph.getSTokens())
      {
        for (SAnnotation anno : n.getSAnnotations())
        {
          result.add(anno.getQName());
        }
      }
    }

    return result;
  }

  public static Set<String> getTokenAnnotationLevelSet(SaltProject p)
  {
    Set<String> result = new TreeSet<String>();

    for (SCorpusGraph corpusGraphs : p.getSCorpusGraphs())
    {
      for (SDocument doc : corpusGraphs.getSDocuments())
      {
        SDocumentGraph g = doc.getSDocumentGraph();
        result.addAll(getTokenAnnotationLevelSet(g));
      }
    }

    return result;
  }

  public static Set<String> getOrderingTypes(SaltProject p)
  {
    Set<String> result = new TreeSet<String>();

    for (SCorpusGraph corpusGraphs : p.getSCorpusGraphs())
    {
      for (SDocument doc : corpusGraphs.getSDocuments())
      {
        SDocumentGraph g = doc.getSDocumentGraph();
        if (g != null)
        {
          EList<SOrderRelation> orderRelations = g.getSOrderRelations();
          if (orderRelations != null)
          {
            for (SOrderRelation rel : orderRelations)
            {
              if(rel.getSTypes() != null)
              {
                result.addAll(rel.getSTypes());
              }
            }
          }
        }
      }
    }

    return result;
  }

  /**
   * Gets the spannend/covered text for a token. This will get all
   * {@link STextualRelation} edges for a {@link SToken} from the
   * {@link SDocumentGraph} and calculates the appropiate substring from the
   * {@link STextualDS}.
   *
   * @param tok
   * @return
   */
  public static String getSpannedText(SToken tok)
  {
    SDocumentGraph graph = tok.getSDocumentGraph();

    EList<Edge> edges = graph.getOutEdges(tok.getSId());
    for(Edge e : edges)
    {
      if(e instanceof STextualRelation)
      {
        STextualRelation textRel = (STextualRelation) e;
        return  textRel.getSTextualDS().getSText().substring(textRel.getSStart(), textRel.getSEnd());
      }
    }
    return "";
  }

  public static List<String> getCorpusPath(SCorpusGraph corpusGraph,
    SDocument doc)
  {
    final List<String> result = new LinkedList<String>();

    result.add(doc.getSName());
    SCorpus c = corpusGraph.getSCorpus(doc);
    BasicEList<SCorpus> cAsList = new BasicEList<SCorpus>();
    cAsList.add(c);
    corpusGraph.traverse(cAsList, GRAPH_TRAVERSE_TYPE.BOTTOM_UP_DEPTH_FIRST,
      "getRootCorpora",
      new SGraphTraverseHandler()
      {
        @Override
        public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId, SNode currNode, SRelation edge, SNode fromNode,
          long order)
        {
          result.add(currNode.getSName());
        }

        @Override
        public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId,
          SNode currNode, SRelation edge, SNode fromNode, long order)
        {
        }

        @Override
        public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId, SRelation edge, SNode currNode, long order)
        {
          return true;
        }
      });
    return result;
  }

  public static List<String> getCorpusPath(URI uri)
  {
    String rawPath = StringUtils.strip(uri.getRawPath(), "/ \t");

    // split on raw path (so "/" in corpus names are still encoded)
    String[] path = rawPath.split("/");


    // decode every single part by itself
    ArrayList<String> result = new ArrayList<String>(path.length);
    for (int i = 0; i < path.length; i++)
    {
      try
      {
        result.add(URLDecoder.decode(path[i], "UTF-8"));
      }
      catch (UnsupportedEncodingException ex)
      {
        log.error(null, ex);
        // fallback
        result.add(path[i]);
      }
    }

    return result;
  }
  
  public static SNode[] getMatchedNodes(SDocument doc)
  {
    SNode[] result = new SNode[0];
    
    // get the matched node IDs
    SFeature feat = doc.getSFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDIDS);
    if(feat != null)
    {
      String[] ids = feat.getSValueSTEXT().split(",");
      result = new SNode[ids.length];
      
      for(int i=0; i < ids.length; i++)
      {
        String id = ids[i].trim();
        if(!id.isEmpty())
        {
          // get the specific node
          SNode node = doc.getSDocumentGraph().getSNode(id);
          if(node != null)
          {
            result[i] = node;
          }
        }
      }
    }
    
    return result;
  }
}
