/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.sqlgen;

import annis.model.RelannisNodeFeature;
import static annis.model.AnnisConstants.*;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.emf.common.util.URI;
import org.springframework.dao.DataAccessException;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.exceptions.SaltException;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.*;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SDATATYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SProcessingAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SaltAnnotateExtractor implements AnnotateExtractor<SaltProject>
{
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SaltAnnotateExtractor.class);
  private TableAccessStrategy outerQueryTableAccessStrategy;
  private CorpusPathExtractor corpusPathExtractor;
  
  
  public SaltAnnotateExtractor()
  {
  }

  @Override
  public SaltProject extractData(ResultSet resultSet)
    throws SQLException, DataAccessException
  {
    SaltProject project = SaltFactory.eINSTANCE.createSaltProject();

    try
    {
      
      SCorpusGraph corpusGraph = null;

      SDocumentGraph graph = null;

      // fn: parent information (pre and component) id to node
      FastInverseMap<RankID, SNode> nodeByPre = new FastInverseMap<RankID, SNode>();

      TreeSet<Long> allTextIDs = new TreeSet<Long>();
      TreeMap<Long, String> tokenTexts = new TreeMap<Long, String>();
      TreeMap<Long, SToken> tokenByIndex = new TreeMap<Long, SToken>();
      TreeMap<String, TreeMap<Long, String>> nodeBySegmentationPath =
        new TreeMap<String, TreeMap<Long, String>>();

      // clear mapping functions for this graph
      // assumes that the result set is sorted by key, pre
      nodeByPre.clear();

      SDocument document = null;

      String[] keyNameList = new String[0];
      
      AtomicInteger numberOfEdges = new AtomicInteger();
      int match_index = 0;
      
      SolutionKey<?> key = createSolutionKey();

      int counter = 0;
      while (resultSet.next())
      {
        if(counter % 1000 == 0)
        {
          log.debug("handling resultset row {}", counter);
        }
        counter++;
        //List<String> annotationGraphKey = 
        key.retrieveKey(resultSet);

        if (key.isNewKey())
        {

          // create the text for the last graph
          if (graph != null && document != null)
          {
            removeArtificialDominancesEdges(graph);
            createPrimaryTexts(graph, allTextIDs, tokenTexts, tokenByIndex);
            setMatchedIDs(document, keyNameList);
            addOrderingRelations(graph, nodeBySegmentationPath);
          }

          // new match, reset everything        
          nodeByPre.clear();
          tokenTexts.clear();
          tokenByIndex.clear();
          keyNameList = new String[key.getKeySize()];


          Integer matchstart = resultSet.getInt("matchstart");
          corpusGraph = SaltFactory.eINSTANCE.createSCorpusGraph();
          corpusGraph.setSName("match_" + (match_index + matchstart));

          project.getSCorpusGraphs().add(corpusGraph);

          graph = SaltFactory.eINSTANCE.createSDocumentGraph();
          document = SaltFactory.eINSTANCE.createSDocument();


          List<String> path = corpusPathExtractor.extractCorpusPath(resultSet,
            "path");

          SCorpus toplevelCorpus = SaltFactory.eINSTANCE.createSCorpus();
          toplevelCorpus.setSName(path.get(0));
          corpusGraph.addSNode(toplevelCorpus);

          Validate.isTrue(path.size() >= 2,
            "Corpus path must be have at least two members (toplevel and document)");
          SCorpus corpus = toplevelCorpus;

          for (int i = 1; i < path.size() - 1; i++)
          {
            SCorpus subcorpus = SaltFactory.eINSTANCE.createSCorpus();
            subcorpus.setSName(path.get(i));
            corpusGraph.addSSubCorpus(corpus, subcorpus);
            corpus = subcorpus;
          }
          document.setSName(path.get(path.size() - 1));
          corpusGraph.addSDocument(corpus, document);

          document.setSDocumentGraph(graph);
          match_index++;
        } // end if new key

        // get node data
        SNode node = createOrFindNewNode(resultSet, graph, allTextIDs, tokenTexts,
          tokenByIndex, nodeBySegmentationPath, key, keyNameList);
        long pre = longValue(resultSet, RANK_TABLE, "pre");
        long componentID = longValue(resultSet, RANK_TABLE, "component_id");
        if (!resultSet.wasNull())
        {
          nodeByPre.put(new RankID(componentID, pre), node);
          createRelation(resultSet, graph, nodeByPre, node, numberOfEdges);
        }
      } // end while new result row

      // the last match needs a primary text, too
      if (graph != null)
      {
        removeArtificialDominancesEdges(graph);
        createPrimaryTexts(graph, allTextIDs, tokenTexts, tokenByIndex);
        setMatchedIDs(document, keyNameList);
        addOrderingRelations(graph, nodeBySegmentationPath);
      }
    }
    catch(Exception ex)
    {
      log.error("could not map result set to SaltProject", ex);
    }

    return project;
  }
  
  /**
   * Removes all dominance edges with empty name, where an other edge with name,
   * the same namespace and the same source and target nodes exists.
   * @param graph 
   */
  private void removeArtificialDominancesEdges(SDocumentGraph graph)
  {
    Iterator<SDominanceRelation> itDomReal = graph.getSDominanceRelations().iterator();
    List<SDominanceRelation> edgesToRemove = new LinkedList<SDominanceRelation>();
    while(itDomReal.hasNext())
    {
      SDominanceRelation rel = itDomReal.next();
      
      SFeature featCompID = rel.getSFeature(ANNIS_NS, FEAT_COMPONENTID);
      SFeature featPre = rel.getSFeature(ANNIS_NS, FEAT_INTERNALID);
      
      boolean allNull = true;
      List<String> types = rel.getSTypes();
      if(types != null)
      {
        for(String s : types)
        {
          if(s != null)
          {
            allNull = false;
            break;
          }
        }
      } // end if types not null
      if (allNull)
      {
        List<Edge> mirrorEdges = graph.getEdges(rel.getSSource().getSId(), rel.
          getSTarget().getSId());
        if (mirrorEdges != null && mirrorEdges.size() > 1)
        {
          for (Edge mirror : mirrorEdges)
          {
            if (mirror != rel && featCompID != null && featPre != null)
            {
              SRelation mirrorRel = (SRelation) mirror;
              
              if(mirrorRel.getSFeature(ANNIS_NS, FEAT_ARTIFICIAL_DOMINANCE_COMPONENT) == null)
              {
                mirrorRel.createSFeature(
                  ANNIS_NS, FEAT_ARTIFICIAL_DOMINANCE_COMPONENT,
                  featCompID.getSValueSNUMERIC(), SDATATYPE.SNUMERIC);
                mirrorRel.createSFeature(
                  ANNIS_NS, FEAT_ARTIFICIAL_DOMINANCE_PRE,
                  featPre.getSValueSNUMERIC(), SDATATYPE.SNUMERIC);
              }
            }
          }
          // remove this edge
          edgesToRemove.add(rel);
        }
      }
    }
    
    // actually remove the edges
    for(SDominanceRelation rel : edgesToRemove)
    {
      Validate.isTrue( graph.removeEdge(rel), "Edge to remove must exist in graph." );;
    }
    
  }
  
  private void addOrderingRelations(SDocumentGraph graph,
    TreeMap<String, TreeMap<Long, String>> nodeBySegmentationPath)
  {
    AtomicInteger numberOfSOrderRels = new AtomicInteger();
    
    for(Map.Entry<String, TreeMap<Long, String>> e : nodeBySegmentationPath.entrySet())
    {
      TreeMap<Long, String> nodeBySegIndex = e.getValue();
      
      SNode lastNode = null;
      for(String nodeID : nodeBySegIndex.values())
      {
        SNode n = graph.getSNode(nodeID);
        
        if(lastNode != null && n != null)
        {
          SOrderRelation orderRel = SaltFactory.eINSTANCE.createSOrderRelation();
          orderRel.setSSource(lastNode);
          orderRel.setSTarget(n);
          orderRel.addSType(e.getKey());
          orderRel.setSName("sOrderRel" + numberOfSOrderRels.getAndIncrement());
          graph.addSRelation(orderRel);
        }
        lastNode = n;
      }
    }
  }

  private void setMatchedIDs(SDocument document, String[] keyNameList)
  {
    // set the matched keys
    SFeature feature = SaltFactory.eINSTANCE.createSFeature();
    feature.setSNS(ANNIS_NS);
    feature.setSName(FEAT_MATCHEDIDS);
    String val = StringUtils.join(keyNameList, ",");
    feature.setSValue(val);
    document.addSFeature(feature);

  }

  private void createSinglePrimaryText(SDocumentGraph graph, long textID,
    TreeMap<Long, String> tokenTexts,  TreeMap<Long, SToken> tokenByIndex)
  {
    STextualDS textDataSource = SaltFactory.eINSTANCE.createSTextualDS();
    textDataSource.setSName("sText" + textID);
    graph.addSNode(textDataSource);
    
    StringBuilder sbText = new StringBuilder();
    Iterator<Map.Entry<Long, String>> itToken = tokenTexts.entrySet().
      iterator();
    long index = 0;
    while (itToken.hasNext())
    {
      Map.Entry<Long, String> e = itToken.next();
      SToken tok = tokenByIndex.get(e.getKey());
      
      RelannisNodeFeature feat = (RelannisNodeFeature) tok.getSFeature(ANNIS_NS, FEAT_RELANNIS).getSValue();
      
      if(feat.getTextRef() == textID)
      {
        STextualRelation textRel = SaltFactory.eINSTANCE.createSTextualRelation();
        textRel.setSSource(tok);
        textRel.setSTarget(textDataSource);
        textRel.setSStart(sbText.length());
        textRel.setSEnd(sbText.length() + e.getValue().length());
        
        textRel.setSName("sTextRel" + textID + "_" + (index++));

        textRel.setSTextualDS(textDataSource);
        graph.addSRelation(textRel);

        sbText.append(e.getValue());
        if (itToken.hasNext())
        {
          sbText.append(" ");
        }
      }
    }

    textDataSource.setSText(sbText.toString());
  }
  
  private void createPrimaryTexts(SDocumentGraph graph,
    TreeSet<Long> allTextIDs, TreeMap<Long, String> tokenTexts, 
    TreeMap<Long, SToken> tokenByIndex)
  {
    for(long textID : allTextIDs)
    {
      createSinglePrimaryText(graph, textID, tokenTexts, tokenByIndex);
    }
    
  }

  private SNode createOrFindNewNode(ResultSet resultSet,
    SDocumentGraph graph, TreeSet<Long> allTextIDs, TreeMap<Long, String> tokenTexts,
    TreeMap<Long, SToken> tokenByIndex, 
    TreeMap<String, TreeMap<Long, String>> nodeBySegmentationPath,
    SolutionKey<?> key,
    String[] keyNameList) throws SQLException
  {
    String name = stringValue(resultSet, NODE_TABLE, "node_name");
    long internalID = longValue(resultSet, "node", "id");

    long tokenIndex = longValue(resultSet, NODE_TABLE, "token_index");
    boolean isToken = !resultSet.wasNull();

    URI nodeURI = graph.getSElementPath();
    nodeURI = nodeURI.appendFragment(name);
    SStructuredNode node = (SStructuredNode) graph.getSNode(nodeURI.toString());
    if (node == null)
    {
      // create new node
      if (isToken)
      {
        node = createSToken(tokenIndex, resultSet, tokenTexts, tokenByIndex);
      }
      else
      {
        node = createOtherSNode(resultSet);
      }

      node.setSName(name);
      
      setFeaturesForNode(node, internalID, resultSet);
      
      Object nodeId = key.getNodeId(resultSet,
        outerQueryTableAccessStrategy);
      Integer matchedNode = key.getMatchedNodeIndex(nodeId);
      if (matchedNode != null)
      {
        addLongSFeature(node, FEAT_MATCHEDNODE, matchedNode);
        keyNameList[matchedNode-1] = node.getSId();
      }

      graph.addNode(node);
      mapLayer(node, graph, resultSet);
      
      long textRef = longValue(resultSet, NODE_TABLE, "text_ref");
      allTextIDs.add(textRef);
      
    }

    String nodeAnnoValue =
      stringValue(resultSet, NODE_ANNOTATION_TABLE, "value");
    String nodeAnnoNameSpace = stringValue(resultSet, NODE_ANNOTATION_TABLE,
      "namespace");
    String nodeAnnoName = stringValue(resultSet, NODE_ANNOTATION_TABLE, "name");
    if (!resultSet.wasNull())
    {
      String fullName = (nodeAnnoNameSpace == null ? "" : (nodeAnnoNameSpace
        + "::")) + nodeAnnoName;
      SAnnotation anno = node.getSAnnotation(fullName);
      if (anno == null)
      {
        anno = SaltFactory.eINSTANCE.createSAnnotation();
        anno.setSNS(nodeAnnoNameSpace);
        anno.setSName(nodeAnnoName);
        anno.setSValue(nodeAnnoValue);
        node.addSAnnotation(anno);
      }
    }

    // prepare SOrderingRelation if the node is part of a segmentation path
    String segName = stringValue(resultSet, "node", "seg_name");
    if(segName != null)
    {
      long left = longValue(resultSet, "node", "seg_index");
      // only nodes that might be valid leafs
      // since we are sorting everything by preorder the real leafs will be the
      // last ones
      if(!nodeBySegmentationPath.containsKey(segName))
      {
        nodeBySegmentationPath.put(segName, new TreeMap<Long, String>());
      }
      nodeBySegmentationPath.get(segName).put(left, node.getSId());

    }
    
    return node;
  }
  
  private SToken createSToken(long tokenIndex, ResultSet resultSet,  
    TreeMap<Long, String> tokenTexts,
    TreeMap<Long, SToken> tokenByIndex) throws SQLException
  {
    SToken tok = SaltFactory.eINSTANCE.createSToken();
    
    // get spanned text of token
    tokenTexts.put(tokenIndex, stringValue(resultSet, NODE_TABLE, "span"));
    tokenByIndex.put(tokenIndex, tok);
    
    return tok;
  }
  
  private SStructuredNode createOtherSNode(ResultSet resultSet) throws SQLException
  {
    // check if we have span, early detection of spans will spare
    // us calls to recreateNode() which is quite costly since it
    // removes nodes/edges and this is something Salt does not handle
    // efficiently
    String edgeType = stringValue(resultSet, COMPONENT_TABLE, "type");
    if("c".equals(edgeType))
    {
      SSpan span = SaltFactory.eINSTANCE.createSSpan();
      return span;
    }
    else
    {
      // default fallback is a SStructure
      SStructure struct = SaltFactory.eINSTANCE.createSStructure();
      return struct;
    }
  }
  
  private void setFeaturesForNode(SStructuredNode node, long internalID, ResultSet resultSet) throws SQLException
  { 
    
    SFeature feat = SaltFactory.eINSTANCE.createSFeature();
    feat.setSNS(ANNIS_NS);
    feat.setSName(FEAT_RELANNIS);
    
    RelannisNodeFeature val = new RelannisNodeFeature();
    val.setCorpusRef(longValue(resultSet, "node", "corpus_ref"));
    val.setTextRef(longValue(resultSet, "node", "text_ref"));
    val.setLeft(longValue(resultSet, "node", "left"));
    val.setLeftToken(longValue(resultSet, "node", "left_token"));
    val.setRight(longValue(resultSet, "node", "right"));
    val.setRightToken(longValue(resultSet, "node", "right_token"));
    val.setTokenIndex(longValue(resultSet, "node", "token_index"));
    val.setSegIndex(longValue(resultSet, "node", "seg_index"));
    val.setSegName(stringValue(resultSet, "node", "seg_name"));
    feat.setSValue(val);
    
    node.addSFeature(feat);
  }
  
  private void mapLayer(SStructuredNode node, SDocumentGraph graph, ResultSet resultSet) 
    throws SQLException
  {
    String namespace = stringValue(resultSet, NODE_TABLE, "namespace");
    List<SLayer> layerList = graph.getSLayerByName(namespace);
    SLayer layer = (layerList != null && layerList.size() > 0)
      ? layerList.get(0) : null;
    if (layer == null)
    {
      layer = SaltFactory.eINSTANCE.createSLayer();
      layer.setSName(namespace);
      graph.addSLayer(layer);
    }
    node.getSLayers().add(layer);
  }
  

  private void addLongSFeature(SNode node, String name,
    long value) throws SQLException
  {
    SFeature feat = SaltFactory.eINSTANCE.createSFeature();
    feat.setSNS(ANNIS_NS);
    feat.setSName(name);
    feat.setSValue(value);
    node.addSFeature(feat);
  }
  
  private void addStringSFeature(SNode node, String name,
    String value) throws SQLException
  {
    SFeature feat = SaltFactory.eINSTANCE.createSFeature();
    feat.setSNS(ANNIS_NS);
    feat.setSName(name);
    feat.setSValue(value);
    node.addSFeature(feat);
  }

  private void addLongSFeature(SNode node, ResultSet resultSet, String name,
    String table, String tupleName) throws SQLException
  {
    addLongSFeature(node, name, longValue(resultSet, table, tupleName));
  }
  
  private void addStringSFeature(SNode node, ResultSet resultSet, String name,
    String table, String tupleName) throws SQLException
  {
    addStringSFeature(node, name, stringValue(resultSet, table, tupleName));
  }

  private SStructuredNode recreateNode(Class<? extends SStructuredNode> clazz,
    SStructuredNode oldNode)
  {
    if (oldNode.getClass() == clazz)
    {
      return oldNode;
    }
    
    SStructuredNode node = oldNode;

    if (clazz == SSpan.class)
    {
      node = SaltFactory.eINSTANCE.createSSpan();
    }
    else if (clazz == SStructure.class)
    {
      node = SaltFactory.eINSTANCE.createSStructure();
    }
    else
    {
      throw new UnsupportedOperationException("no node creation possible for class: "
        + clazz.getName());
    }
    moveNodeProperties(oldNode, node, oldNode.getSGraph());

    return node;
  }
  
  private void updateMapAfterRecreatingNode(SNode oldNode, SNode newNode, 
    FastInverseMap<RankID, SNode> nodeByPre)
  {
    // get *all* keys associated with this node
    List<RankID> keys = nodeByPre.getKeys(oldNode);
    for(RankID id : keys)
    {
      nodeByPre.put(id, newNode);
    }
  }

  private void moveNodeProperties(SStructuredNode from, SStructuredNode to,
    SGraph graph)
  {
    Validate.notNull(from);
    Validate.notNull(to);

    to.setSName(from.getSName());
    for (SLayer l : from.getSLayers())
    {
      to.getSLayers().add(l);
    }
    from.getSLayers().clear();
 
    List<Edge> inEdges =  graph.getInEdges(from.getSId());
    for(Edge e : inEdges)
    {
      if(e instanceof SRelation)
      {
        Validate.isTrue(graph.removeEdge(e));
      }
    }
    List<Edge> outEdges = graph.getOutEdges(from.getSId());
    for(Edge e : outEdges)
    {
      if(e instanceof SRelation)
      {
        Validate.isTrue(graph.removeEdge(e));
      }
    }
    
    Validate.isTrue(graph.removeNode(from));
    graph.addNode(to);
    
    // fix old edges
    for(Edge e : inEdges)
    {
      if(e instanceof SRelation)
      {
        ((SRelation) e).setSTarget(to);
        graph.addSRelation((SRelation) e);
      }
    }
    
    for(Edge e : outEdges)
    {
      if(e instanceof SRelation)
      {
        ((SRelation) e).setSSource(to);
        graph.addSRelation((SRelation) e);
      }
    }


    for (SAnnotation anno : from.getSAnnotations())
    {
      to.addSAnnotation(anno);
    }
    for (SFeature feat : from.getSFeatures())
    {
      // filter the features, do not include salt::SNAME 
      if (!(SaltFactory.SALT_CORE_NAMESPACE.equals(feat.getSNS())
        && SaltFactory.SALT_CORE_SFEATURES.SNAME.toString().equals(
        feat.getSName())))
      {
        to.addSFeature(feat);
      }
    }
    for (SProcessingAnnotation proc : from.getSProcessingAnnotations())
    {
      to.addSProcessingAnnotation(proc);
    }
    for (SMetaAnnotation meta : from.getSMetaAnnotations())
    {
      to.addSMetaAnnotation(meta);
    }

  }
  
  private SRelation findExistingRelation(SDocumentGraph graph, 
    SNode sourceNode, SNode targetNode, String edgeName, SLayer layer)
  {
    SRelation rel = null;
    
    List<Edge> existingEdges = graph.getEdges(sourceNode.getSId(),
      targetNode.getSId());
    if (existingEdges != null)
    {
      for (Edge e : existingEdges)
      {
        // only select the edge that has the same type ("edge_name" and
        // the same layer ("edge_namespace")
        if (e instanceof SRelation)
        {
          SRelation existingRel = (SRelation) e;

          boolean noType = existingRel.getSTypes() == null || existingRel.getSTypes().size() == 0;
          if (((noType && edgeName == null) || (!noType && existingRel.getSTypes().
            contains(edgeName)))
            && existingRel.getSLayers().contains(layer))
          {
            rel = existingRel;
            break;
          }

        }
      }
    }
    return rel;
  }  
  
  private SRelation createNewRelation(SDocumentGraph graph, SStructuredNode sourceNode, 
    SNode targetNode, String edgeName, String type, long componentID, 
    SLayer layer, long parent, long pre,
    FastInverseMap<RankID, SNode> nodeByPre, AtomicInteger numberOfEdges)
  {
    SRelation rel = null;
    // create new relation
    if ("d".equals(type))
    {
      SDominanceRelation domrel = SaltFactory.eINSTANCE.createSDominanceRelation();
      // always set a name by ourself since the SDocumentGraph#basicAddEdge() 
      // functions otherwise real slow
      domrel.setSName("sDomRel"+numberOfEdges.incrementAndGet());
      rel = domrel;

      if (sourceNode != null && !(sourceNode instanceof SStructure))
      {
        log.debug("Mismatched source type: should be SStructure");
        SNode oldNode = sourceNode;
        sourceNode = recreateNode(SStructure.class, sourceNode);
        updateMapAfterRecreatingNode(oldNode, sourceNode, nodeByPre);
      }
    }
    else if ("c".equals(type))
    {
      SSpanningRelation spanrel = SaltFactory.eINSTANCE.createSSpanningRelation();
      // always set a name by ourself since the SDocumentGraph#basicAddEdge() 
      // functions otherwise real slow
      spanrel.setSName("sSpanRel"+numberOfEdges.incrementAndGet());
      rel = spanrel;

      if (sourceNode != null && !(sourceNode instanceof SSpan))
      {
        log.debug("Mismatched source type: should be SSpan");
        SNode oldNode = sourceNode;
        sourceNode = recreateNode(SSpan.class, sourceNode);
        updateMapAfterRecreatingNode(oldNode,  sourceNode, nodeByPre);
      }
    }
    else if ("p".equals(type))
    {
      SPointingRelation pointingrel = SaltFactory.eINSTANCE.createSPointingRelation();
      pointingrel.setSName("sPointingRel"+numberOfEdges.incrementAndGet());
      rel = pointingrel;
    }
    else
    {
      throw new IllegalArgumentException("Invalid type " + type + " for new Edge"); 
    }

    try
    {
      rel.addSType(edgeName);


      SFeature featInternalID = SaltFactory.eINSTANCE.createSFeature();
      featInternalID.setSNS(ANNIS_NS);
      featInternalID.setSName(FEAT_INTERNALID);
      featInternalID.setSValue(Long.valueOf(pre));
      rel.addSFeature(featInternalID);

      SFeature featComponentID = SaltFactory.eINSTANCE.createSFeature();
      featComponentID.setSNS(ANNIS_NS);
      featComponentID.setSName(FEAT_COMPONENTID);
      featComponentID.setSValue(Long.valueOf(componentID));
      rel.addSFeature(featComponentID);

      rel.setSSource((SNode) nodeByPre.get(new RankID(componentID, parent)));
      if ("c".equals(type) && !(targetNode instanceof SToken))
      {
        log.warn("invalid edge detected: target node ({}) "
          + "of a coverage relation (from: {}, internal id {}) was not a token",
          new Object[]
          {
            targetNode.getSName(), sourceNode.getSName(), "" + pre
          });
      }
      else
      {
        rel.setSTarget(targetNode);
        graph.addSRelation(rel);
      }
      
      rel.getSLayers().add(layer);
      
    }
    catch (SaltException ex)
    {
      log.warn("invalid edge detected", ex);
    }
    
    return rel;
  }
  
  private void addEdgeAnnotations(ResultSet resultSet, SRelation rel) 
    throws SQLException
  {
    String edgeAnnoValue =
        stringValue(resultSet, EDGE_ANNOTATION_TABLE, "value");
      String edgeAnnoNameSpace = stringValue(resultSet, EDGE_ANNOTATION_TABLE,
        "namespace");
      String edgeAnnoName =
        stringValue(resultSet, EDGE_ANNOTATION_TABLE, "name");
      if (!resultSet.wasNull())
      {
        String fullName = edgeAnnoNameSpace == null ? "" : edgeAnnoNameSpace
          + "::" + edgeAnnoName;
        SAnnotation anno = rel.getSAnnotation(fullName);
        if (anno == null)
        {
          anno = SaltFactory.eINSTANCE.createSAnnotation();
          anno.setSNS(edgeAnnoNameSpace == null ? "" : edgeAnnoNameSpace);
          anno.setSName(edgeAnnoName);
          anno.setSValue(edgeAnnoValue);
          rel.addSAnnotation(anno);
        }
      } // end if edgeAnnoName exists
  }

  private SRelation createRelation(ResultSet resultSet, SDocumentGraph graph,
    FastInverseMap<RankID, SNode> nodeByPre, SNode targetNode, AtomicInteger numberOfEdges) throws
    SQLException
  {
    long parent = longValue(resultSet, RANK_TABLE, "parent");
    if (resultSet.wasNull())
    {
      return null;
    }

    long pre = longValue(resultSet, RANK_TABLE, "pre");
    long componentID = longValue(resultSet, RANK_TABLE, "component_id");
    String edgeNamespace = stringValue(resultSet, COMPONENT_TABLE, "namespace");
    String edgeName = stringValue(resultSet, COMPONENT_TABLE, "name");
    String type = stringValue(resultSet, COMPONENT_TABLE, "type");

    SStructuredNode sourceNode = 
      (SStructuredNode) nodeByPre.get(new RankID(componentID, parent));

    if (sourceNode == null)
    {
      // the edge is not fully included in the result
      return null;
    }

    List<SLayer> layerList = graph.getSLayerByName(edgeNamespace);
    SLayer layer = (layerList != null && layerList.size() > 0)
      ? layerList.get(0) : null;
    if (layer == null)
    {
      layer = SaltFactory.eINSTANCE.createSLayer();
      layer.setSName(edgeNamespace);
      graph.addSLayer(layer);
    }

    SRelation rel = null;
    if (!resultSet.wasNull())
    {

      rel = findExistingRelation(graph, sourceNode, targetNode, edgeName, layer);

      if (rel == null)
      {
        rel = createNewRelation(graph, sourceNode, targetNode, edgeName, type, 
          componentID, layer, parent, pre, nodeByPre, numberOfEdges);
      } // end if no existing relation

      // add edge annotations
      addEdgeAnnotations(resultSet, rel);
    }
    return rel;
  }
  
  protected SolutionKey<?> createSolutionKey()
  {
    throw new UnsupportedOperationException(
      "BUG: This method needs to be overwritten by ancestors or through Spring");
  }

  protected void newline(StringBuilder sb, int indentBy)
  {
    sb.append("\n");
    indent(sb, indentBy);
  }

  protected void indent(StringBuilder sb, int indentBy)
  {
    sb.append(StringUtils.repeat(AbstractSqlGenerator.TABSTOP, indentBy));
  }

  protected long longValue(ResultSet resultSet, String table, String column)
    throws SQLException
  {
    return resultSet.getLong(outerQueryTableAccessStrategy.columnName(table,
      column));
  }

  protected String stringValue(ResultSet resultSet, String table, String column)
    throws SQLException
  {
    return resultSet.getString(outerQueryTableAccessStrategy.columnName(
      table, column));
  }

  public CorpusPathExtractor getCorpusPathExtractor()
  {
    return corpusPathExtractor;
  }

  public void setCorpusPathExtractor(CorpusPathExtractor corpusPathExtractor)
  {
    this.corpusPathExtractor = corpusPathExtractor;
  }

  public TableAccessStrategy getOuterQueryTableAccessStrategy()
  {
    return outerQueryTableAccessStrategy;
  }

  @Override
  public void setOuterQueryTableAccessStrategy(TableAccessStrategy outerQueryTableAccessStrategy)
  {
    this.outerQueryTableAccessStrategy = outerQueryTableAccessStrategy;
  }
  
  public static class FastInverseMap<KeyType, ValueType>
  {
    private Map<KeyType, ValueType> key2value = new HashMap<KeyType, ValueType>();
    private Map<ValueType, List<KeyType>> values2keys = new HashMap<ValueType, List<KeyType>>(); 
    
    /**
     * Wrapper for {@link Map#put(java.lang.Object, java.lang.Object) }
     * @param key
     * @param value
     * @return 
     */
    public ValueType put(KeyType key, ValueType value)
    {
      List<KeyType> inverse = values2keys.get(value);
      if(inverse == null)
      {
        inverse = new LinkedList<KeyType>();
        values2keys.put(value, inverse);
      }
      
      inverse.add(key);
      
      return key2value.put(key, value);
    }
    
    /**
     * Wrapper for {@link Map#get(java.lang.Object) }
     * @param key
     * @return 
     */
    public ValueType get(KeyType key)
    {
      return key2value.get(key);
    }
    
    /**
     * Fast inverse lookup.
     * 
     * @param value
     * @return All keys belonging to this value.
     */
    public List<KeyType> getKeys(ValueType value)
    {
      List<KeyType> result = values2keys.get(value);
      if(result == null)
      {
        result = new LinkedList<KeyType>();
        values2keys.put(value, result);
      }
      
      // always return a copy
      return new LinkedList<KeyType>(result);
    }
    
    /**
     * Wrapper for {@link  Map#clear() }
     */
    public void clear()
    {
      key2value.clear();
      values2keys.clear();
    }
  }
  
  public static class RankID
  {
    private long componentID;
    private long pre;

    public RankID(long componentID, long pre)
    {
      this.componentID = componentID;
      this.pre = pre;
    }

    public long getComponentID()
    {
      return componentID;
    }

    public long getPre()
    {
      return pre;
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
      final RankID other = (RankID) obj;
      if (this.componentID != other.componentID)
      {
        return false;
      }
      if (this.pre != other.pre)
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 5;
      hash = 29 * hash + (int) (this.componentID ^ (this.componentID >>> 32));
      hash = 29 * hash + (int) (this.pre ^ (this.pre >>> 32));
      return hash;
    }
    
    
  }
  
}
