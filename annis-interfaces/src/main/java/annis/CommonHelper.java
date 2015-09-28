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
import annis.service.objects.Match;
import com.google.common.base.Splitter;
import de.hu_berlin.u.saltnpepper.graph.Label;
import de.hu_berlin.u.saltnpepper.graph.Relation;
import de.hu_berlin.u.saltnpepper.salt.common.SCorpus;
import de.hu_berlin.u.saltnpepper.salt.common.SCorpusGraph;
import de.hu_berlin.u.saltnpepper.salt.common.SDocument;
import de.hu_berlin.u.saltnpepper.salt.common.SDocumentGraph;
import de.hu_berlin.u.saltnpepper.salt.common.SOrderRelation;
import de.hu_berlin.u.saltnpepper.salt.common.STextualDS;
import de.hu_berlin.u.saltnpepper.salt.common.STextualRelation;
import de.hu_berlin.u.saltnpepper.salt.common.SToken;
import de.hu_berlin.u.saltnpepper.salt.common.SaltProject;
import de.hu_berlin.u.saltnpepper.salt.core.GraphTraverseHandler;
import de.hu_berlin.u.saltnpepper.salt.core.SAnnotation;
import de.hu_berlin.u.saltnpepper.salt.core.SFeature;
import de.hu_berlin.u.saltnpepper.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.u.saltnpepper.salt.core.SLayer;
import de.hu_berlin.u.saltnpepper.salt.core.SNode;
import de.hu_berlin.u.saltnpepper.salt.core.SRelation;
import de.hu_berlin.u.saltnpepper.salt.util.DataSourceSequence;
import de.hu_berlin.u.saltnpepper.salt.util.SALT_TYPE;
import de.hu_berlin.u.saltnpepper.salt.util.SaltUtil;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities class for non-gui operations.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class CommonHelper
{

  private final static Logger log = LoggerFactory.getLogger(CommonHelper.class);

  /**
   * Detects arabic characters in a string.
   *
   * <p>
   * Every character is checked, if its bit representation lies between:
   * <code>[1425, 1785] | [64286, 65019] | [65136, 65276]</code>
   *
   * </p>
   *
   * @param str The string to be checked.
   * @return returns true, if arabic characters are detected.
   */
  public static boolean containsRTLText(String str)
  {
    if (str != null)
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

  /**
   * Calculates a {@link SOrderRelation} node chain of a {@link SDocumentGraph}.
   *
   * <p>
   * If no segmentation name is set, a list of sorted {@link SToken} will be
   * returned.<p>
   *
   * @param segName The segmentation name, for which the chain is computed.
   * @param graph The salt document graph, which is traversed for the
   * segmentation.
   *
   * @return Returns a List of {@link SNode}, which is sorted by the
   * {@link SOrderRelation}.
   */
  public static List<SNode> getSortedSegmentationNodes(String segName,
    SDocumentGraph graph)
  {
    List<SNode> token = new ArrayList<SNode>();

    if (segName == null)
    {
      // if no segmentation is given just return the sorted token list
      token.addAll(graph.getSortedTokenByText());
    }
    else
    {
      // get the very first node of the order relation chain
      Set<SNode> startNodes = new LinkedHashSet<SNode>();

      for (SNode n : graph.getNodes())
      {
        SFeature feat
          = n.getFeature(SaltUtil.createQName(AnnisConstants.ANNIS_NS,
              AnnisConstants.FEAT_FIRST_NODE_SEGMENTATION_CHAIN));
        if (feat != null && segName.equalsIgnoreCase(feat.getValue_STEXT()))
        {
          startNodes.add(n);
        }
      }

      Set<String> alreadyAdded = new HashSet<String>();

      // add all nodes on the order relation chain beginning from the start node
      for (SNode s : startNodes)
      {
        SNode current = s;
        while (current != null)
        {
          token.add(current);
          List<SRelation<SNode, SNode>> out = graph.getOutRelations(current.
            getId());
          current = null;
          if (out != null)
          {
            for (Relation e : out)
            {
              if (e instanceof SOrderRelation)
              {
                current = ((SOrderRelation) e).getTarget();
                if (alreadyAdded.contains(current.getId()))
                {
                  // abort if cycle detected
                  current = null;
                }
                else
                {
                  alreadyAdded.add(current.getId());
                }
                break;
              }
            }
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
      for (SToken n : graph.getTokens())
      {
        for (SAnnotation anno : n.getAnnotations())
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

    for (SCorpusGraph corpusGraphs : p.getCorpusGraphs())
    {
      for (SDocument doc : corpusGraphs.getDocuments())
      {
        SDocumentGraph g = doc.getDocumentGraph();
        result.addAll(getTokenAnnotationLevelSet(g));
      }
    }

    return result;
  }

  /**
   * Gets the spannend/covered text for a token. This will get all
   * {@link STextualRelation} relations for a {@link SToken} from the
   * {@link SDocumentGraph} and calculates the appropiate substring from the
   * {@link STextualDS}.
   *
   * @param tok The {@link SToken} which is overlapping the text sequence.
   * @return An empty {@link String} object, if there is no
   * {@link STextualRelation}
   */
  public static String getSpannedText(SToken tok)
  {
    SDocumentGraph graph = tok.getGraph();

    List<SRelation<SNode, SNode>> relations = graph.getOutRelations(tok.getId());
    for (SRelation e : relations)
    {
      if (e instanceof STextualRelation)
      {
        STextualRelation textRel = (STextualRelation) e;
        return textRel.getTarget().getText().substring(textRel.getStart(),
          textRel.getEnd());
      }
    }
    return "";
  }

  /**
   * Checks a {@link SNode} if it is member of a specific {@link SLayer}.
   *
   * @param layerName Specifies the layername to check.
   * @param node Specifies the node to check.
   * @return true - it is true when the name of layername corresponds to the
   * name of any label of the SNode.
   */
  public static boolean checkSLayer(String layerName, SNode node)
  {
    //robustness
    if (layerName == null || node == null)
    {
      return false;
    }

    Set<SLayer> sLayers = node.getLayers();
    if (sLayers != null)
    {
      for (SLayer l : sLayers)
      {
        Collection<Label> labels = l.getLabels();
        if (labels != null)
        {
          for (Label label : labels)
          {
            if (layerName.equals(label.getValue()))
            {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  public static List<String> getCorpusPath(SCorpusGraph corpusGraph,
    SDocument doc)
  {
    final List<String> result = new LinkedList<>();

    result.add(doc.getName());
    SCorpus c = corpusGraph.getCorpus(doc);
    ArrayList<SNode> cAsList = new ArrayList<>();
    cAsList.add(c);
    corpusGraph.traverse(cAsList, GRAPH_TRAVERSE_TYPE.BOTTOM_UP_DEPTH_FIRST,
      "getRootCorpora",
      new GraphTraverseHandler()
      {
        @Override
        public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId, SNode currNode, SRelation relation, SNode fromNode,
          long order)
        {
          result.add(currNode.getName());
        }

        @Override
        public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId,
          SNode currNode, SRelation relation, SNode fromNode, long order)
        {
        }

        @Override
        public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType,
          String traversalId, SRelation relation, SNode currNode, long order)
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

  /**
   * Finds the {@link STextualDS} for a given node. The node must dominate a
   * token of this text.
   *
   * @param node
   * @return
   */
  public static STextualDS getTextualDSForNode(SNode node, SDocumentGraph graph)
  {
    if (node != null)
    {
      STextualDS tokenText = null;
      List<SALT_TYPE> types = new ArrayList<>();
      types.add(SALT_TYPE.STEXT_OVERLAPPING_RELATION);

      List<DataSourceSequence> dataSources = graph.
        getOverlappedDataSourceSequence(
          node,
          types);
      if (dataSources != null)
      {
        for (DataSourceSequence seq : dataSources)
        {
          if (seq.getDataSource() instanceof STextualDS)
          {
            return (STextualDS) seq.getDataSource();
          }
        }
      }
    }
    return null;
  }

  /**
   * Returns a file name that is safe to use and does not have any invalid
   * characters.
   *
   * @param orig
   * @return
   */
  public static String getSafeFileName(String orig)
  {
    if (orig != null)
    {
      return orig.replaceAll("[^0-9A-Za-z-]", "_");
    }
    else
    {
      return UUID.randomUUID().toString();
    }
  }

  /**
   * Gets all names of a corpus from a salt project.
   *
   * @param p
   * @return returns an empty list if project is empty or null.
   */
  public static Set<String> getToplevelCorpusNames(SaltProject p)
  {
    Set<String> names = new HashSet<String>();

    if (p != null && p.getCorpusGraphs() != null)
    {
      for (SCorpusGraph g : p.getCorpusGraphs())
      {
        if (g.getRoots() != null)
        {
          for (SNode c : g.getRoots())
          {
            names.add(c.getName());
          }
        }
      }
    }

    return names;
  }

  /**
   * Takes a map of salt node IDs to a value and return a new map that uses the
   * SNodes as keys instead of the IDs.
   *
   * @param <V>
   * @param map
   * @param graph
   * @return
   */
  public static <V> Map<SNode, V> createSNodeMapFromIDs(Map<String, V> map,
    SDocumentGraph graph)
  {
    HashMap<SNode, V> result = new LinkedHashMap<>();

    if (map != null && graph != null)
    {
      for (Map.Entry<String, V> e : map.entrySet())
      {
        SNode n = graph.getNode(e.getKey());
        if (n != null)
        {
          result.put(n, e.getValue());
        }
      }
    }

    return result;
  }

  public static Match extractMatch(SDocument doc) throws URISyntaxException
  {
    Splitter idSplit = Splitter.on(',').trimResults();
    Match m = null;

    // get the matched node IDs
    SFeature featIDs = doc.getFeature(SaltUtil.createQName(
      AnnisConstants.ANNIS_NS,
      AnnisConstants.FEAT_MATCHEDIDS));

    if (featIDs != null)
    {
      LinkedList<URI> idList = new LinkedList<>();
      for (String rawID : idSplit.split(featIDs.getValue_STEXT()))
      {
        idList.add(new URI(rawID));
      }
      SFeature featAnnos = doc.getFeature(SaltUtil.createQName(
        AnnisConstants.ANNIS_NS,
        AnnisConstants.FEAT_MATCHEDANNOS));
      if (featAnnos == null)
      {
        m = new Match(idList);
      }
      else
      {
        m = new Match(idList, idSplit.splitToList(featAnnos.getValue_STEXT()));
      }
    }

    return m;
  }

  // TODO: remove if really not needed
//  public static SNode[] getMatchedNodes(SDocument doc)
//  {
//    SNode[] result = new SNode[0];
//
//    // get the matched node IDs
//    SFeature feat = doc.getSFeature(AnnisConstants.ANNIS_NS,
//      AnnisConstants.FEAT_MATCHEDIDS);
//    if (feat != null)
//    {
//      Match m = Match.parseFromString(feat.getSValueSTEXT());
//      result = new SNode[m.getSaltIDs().size()];
//
//      int i = 0;
//      for(URI u : m.getSaltIDs())
//      {
//        // get the specific node
//        SNode node = doc.getDocumentGraph().getNode(u.toASCIIString());
//        if (node != null)
//        {
//          result[i] = node;
//        }
//        i++;
//      }
//    }
//
//    return result;
//  }
}
