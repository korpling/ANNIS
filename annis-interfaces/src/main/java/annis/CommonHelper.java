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
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Label;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SOrderRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
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
      token.addAll(graph.getSortedSTokenByText());
    }
    else
    {
      // get the very first node of the order relation chain
      Set<SNode> startNodes = new LinkedHashSet<SNode>();
      
      for(SNode n : graph.getSNodes())
      {
        SFeature feat = 
          n.getSFeature(AnnisConstants.ANNIS_NS, 
            AnnisConstants.FEAT_FIRST_NODE_SEGMENTATION_CHAIN);
        if(feat != null && segName.equalsIgnoreCase(feat.getSValueSTEXT()))
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
          EList<Edge> out = graph.getOutEdges(current.getSId());
          current = null;
          if(out != null)
          {
            for(Edge e : out)
            {
              if(e instanceof SOrderRelation)
              {
                current = ((SOrderRelation) e).getSTarget();
                if(alreadyAdded.contains(current.getSId()))
                {
                  // abort if cycle detected
                  current = null;
                }
                else
                {
                  alreadyAdded.add(current.getSId());
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

  /**
   * Gets the spannend/covered text for a token. This will get all
   * {@link STextualRelation} edges for a {@link SToken} from the
   * {@link SDocumentGraph} and calculates the appropiate substring from the
   * {@link STextualDS}.
   *
   * @param tok The {@link SToken} which is overlapping the text sequence.
   * @return An empty {@link String} object, if there is no
   * {@link STextualRelation}
   */
  public static String getSpannedText(SToken tok)
  {
    SDocumentGraph graph = tok.getSDocumentGraph();

    EList<Edge> edges = graph.getOutEdges(tok.getSId());
    for (Edge e : edges)
    {
      if (e instanceof STextualRelation)
      {
        STextualRelation textRel = (STextualRelation) e;
        return textRel.getSTextualDS().getSText().substring(textRel.getSStart(),
          textRel.getSEnd());
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

    EList<SLayer> sLayers = node.getSLayers();
    if (sLayers != null)
    {
      for (SLayer l : sLayers)
      {
        EList<Label> labels = l.getLabels();
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

  /**
   * Finds the {@link STextualDS} for a given node. The node must
   * dominate a token of this text.
   *
   * @param node
   * @return
   */
  public static STextualDS getTextualDSForNode(SNode node, SDocumentGraph graph)
  {
    if (node != null)
    {
      STextualDS tokenText = null;
      EList<STYPE_NAME> types = new BasicEList<STYPE_NAME>();
      types.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);

      EList<SDataSourceSequence> dataSources = graph.getOverlappedDSSequences(
        node,
        types);
      if (dataSources != null)
      {
        for (SDataSourceSequence seq : dataSources)
        {
          if (seq.getSSequentialDS() instanceof STextualDS)
          {
            return (STextualDS) seq.getSSequentialDS();
          }
        }
      }
    }
    return null;
  }
  
  /**
   * Returns a file name that is safe to use and does not have any invalid characters.
   * @param orig
   * @return 
   */
  public static String getSafeFileName(String orig)
  {
    if(orig != null)
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
  public static Set<String> getCorpusNames(SaltProject p)
  {
    Set<String> names = new HashSet<String>();

    if (p != null && p.getSCorpusGraphs() != null)
    {
      for (SCorpusGraph g : p.getSCorpusGraphs())
      {
        for (SCorpus c : g.getSCorpora())
        {
          names.add(c.getSName());
        }
      }
    }


    return names;
  }
  
  public static void writeSDocument(SDocument doc, ObjectOutputStream out) 
    throws IOException
  {
    XMIResourceImpl res = new XMIResourceImpl();
    res.getContents().add(doc);
    
   // also add the SDocumentGraph of the document
//    res.getContents().add(doc.getSDocumentGraph());
    
    res.save(out, res.getDefaultSaveOptions());
  }
  
  public static SDocument readSDocument(ObjectInputStream in) 
    throws IOException
  {
    XMIResourceImpl res = new XMIResourceImpl();
    res.load(in, res.getDefaultLoadOptions());
    
    
    TreeIterator<EObject> itContents = res.getAllContents();
    while(itContents.hasNext())
    {
      EObject o = itContents.next();
      if(o instanceof SDocument)
      {
        return (SDocument) o;
      }
    }
    return SaltCommonFactory.eINSTANCE.createSDocument();
  }
  
  /**
   * Takes a map of salt node IDs to a value and return a new map that
   * uses the SNodes as keys instead of the IDs.
   * @param <V>
   * @param map
   * @param graph
   * @return 
   */
  public static <V> Map<SNode, V> createSNodeMapFromIDs(Map<String, V> map, SDocumentGraph graph)
  {
    HashMap<SNode, V> result = new LinkedHashMap<SNode, V>();
    
    if(map != null && graph != null)
    {
      for(Map.Entry<String, V> e : map.entrySet())
      {
        SNode n = graph.getSNode(e.getKey());
        if (n != null)
        {
          result.put(n, e.getValue());
        }
      }
    }
    
    return result;
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
//        SNode node = doc.getSDocumentGraph().getSNode(u.toASCIIString());
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
