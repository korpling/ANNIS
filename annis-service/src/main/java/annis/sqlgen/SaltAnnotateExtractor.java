/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.sqlgen;

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_FIRST_NODE_SEGMENTATION_CHAIN;
import static annis.model.AnnisConstants.FEAT_MATCHEDANNOS;
import static annis.model.AnnisConstants.FEAT_MATCHEDIDS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;
import static annis.model.AnnisConstants.FEAT_RELANNIS_EDGE;
import static annis.model.AnnisConstants.FEAT_RELANNIS_NODE;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SProcessingAnnotation;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.exceptions.SaltException;
import org.corpus_tools.salt.graph.Relation;
import org.corpus_tools.salt.util.SaltUtil;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

import annis.model.RelannisEdgeFeature;
import annis.model.RelannisNodeFeature;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SaltAnnotateExtractor implements AnnotateExtractor<SaltProject>
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    SaltAnnotateExtractor.class);
  private TableAccessStrategy outerQueryTableAccessStrategy;
  private CorpusPathExtractor corpusPathExtractor;

  public SaltAnnotateExtractor()
  {
  }

  @Override
  public SaltProject extractData(ResultSet resultSet)
    throws SQLException, DataAccessException
  {
    SaltProject project = SaltFactory.createSaltProject();

    try
    {

      SCorpusGraph corpusGraph = null;

      SDocumentGraph graph = null;

      // fn: parent information (pre and component) id to node
      FastInverseMap<Long, SNode> nodeByRankID = new FastInverseMap<>();

      TreeSet<Long> allTextIDs = new TreeSet<>();
      TreeMap<Long, String> tokenTexts = new TreeMap<>();
      TreeMap<Long, SToken> tokenByIndex = new TreeMap<>();
      TreeMap<String, TreeMap<Long, String>> nodeBySegmentationPath
        = new TreeMap<>();
      Map<String, ComponentEntry> componentForSpan = new HashMap<>();

      // clear mapping functions for this graph
      // assumes that the result set is sorted by key, pre
      nodeByRankID.clear();

      SDocument document = null;

      AtomicInteger numberOfRelations = new AtomicInteger();
      int match_index = 0;

      SolutionKey<?> key = createSolutionKey();

      int counter = 0;
      while (resultSet.next())
      {
        if (counter % 1000 == 0)
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
            createMissingSpanningRelations(graph, nodeByRankID, tokenByIndex,
              componentForSpan,
              numberOfRelations);
            createPrimaryTexts(graph, allTextIDs, tokenTexts, tokenByIndex);
            addOrderingRelations(graph, nodeBySegmentationPath);
          }

          // new match, reset everything        
          nodeByRankID.clear();
          tokenTexts.clear();
          tokenByIndex.clear();
          componentForSpan.clear();

          Integer matchstart = resultSet.getInt("matchstart");
          corpusGraph = SaltFactory.createSCorpusGraph();
          corpusGraph.setName("match_" + (match_index + matchstart));

          project.addCorpusGraph(corpusGraph);
          
          graph = SaltFactory.createSDocumentGraph();
          document = SaltFactory.createSDocument();
          
          document.setDocumentGraphLocation(org.eclipse.emf.common.util.URI.
            createFileURI(Files.createTempDir().getAbsolutePath()));
          
          List<String> path = corpusPathExtractor.extractCorpusPath(resultSet,
            "path");

          SCorpus toplevelCorpus = SaltFactory.createSCorpus();
          toplevelCorpus.setName(path.get(0));
          corpusGraph.addNode(toplevelCorpus);

          Validate.isTrue(path.size() >= 2,
            "Corpus path must be have at least two members (toplevel and document)");
          SCorpus corpus = toplevelCorpus;

          for (int i = 1; i < path.size() - 1; i++)
          {
            SCorpus subcorpus = SaltFactory.createSCorpus();
            subcorpus.setName(path.get(i));
            corpusGraph.addSubCorpus(corpus, subcorpus);
            corpus = subcorpus;
          }
          document.setName(path.get(path.size() - 1));
          document.setId("" + match_index);
          corpusGraph.addDocument(corpus, document);

          document.setDocumentGraph(graph);
          match_index++;
        } // end if new key

        // get node data
        SNode node = createOrFindNewNode(resultSet, graph, allTextIDs,
          tokenTexts,
          tokenByIndex, nodeBySegmentationPath,
          key, nodeByRankID);
        long rankID = longValue(resultSet, RANK_TABLE, "id");
        long componentID = longValue(resultSet, COMPONENT_TABLE, "id");
        if (!resultSet.wasNull())
        {
          nodeByRankID.put(rankID, node);
          createRelation(resultSet, graph, nodeByRankID, node, numberOfRelations);

          if (node instanceof SSpan)
          {
            componentForSpan.put(node.getId(), new ComponentEntry(componentID,
              'c',
              stringValue(resultSet, COMPONENT_TABLE, "namespace"),
              stringValue(resultSet, COMPONENT_TABLE, "name")
            ));
          }
        }
      } // end while new result row

      // the last match needs a primary text, too
      if (graph != null)
      {
        createMissingSpanningRelations(graph, nodeByRankID, tokenByIndex,
          componentForSpan,
          numberOfRelations);
        createPrimaryTexts(graph, allTextIDs, tokenTexts, tokenByIndex);
        addOrderingRelations(graph, nodeBySegmentationPath);
      }
    }
    catch (Exception ex)
    {
      log.error("could not map result set to SaltProject", ex);
    }

    return project;
  }

  private void addOrderingRelations(SDocumentGraph graph,
    TreeMap<String, TreeMap<Long, String>> nodeBySegmentationPath)
  {
    AtomicInteger numberOfSOrderRels = new AtomicInteger();

    for (Map.Entry<String, TreeMap<Long, String>> e : nodeBySegmentationPath.
      entrySet())
    {
      String segName = e.getKey();
      TreeMap<Long, String> nodeBySegIndex = e.getValue();

      // mark the first node in the chain
      if (!nodeBySegIndex.isEmpty())
      {
        String idOfFirstNode = nodeBySegIndex.firstEntry().getValue();
        SNode firstNodeInSegChain = graph.getNode(idOfFirstNode);
        if (firstNodeInSegChain != null)
        {
          SFeature featFistSegInChain = SaltFactory.createSFeature();
          featFistSegInChain.setNamespace(ANNIS_NS);
          featFistSegInChain.setName(FEAT_FIRST_NODE_SEGMENTATION_CHAIN);
          featFistSegInChain.setValue(segName);
          firstNodeInSegChain.addFeature(featFistSegInChain);
        }
      }

      SStructuredNode lastNode = null;
      for (String nodeID : nodeBySegIndex.values())
      {
        SNode nodeById = graph.getNode(nodeID);

        if (nodeById instanceof SStructuredNode)
        {
          SStructuredNode n = (SStructuredNode) nodeById;

          if (lastNode != null)
          {
            SOrderRelation orderRel = SaltFactory.createSOrderRelation();
            orderRel.setSource(lastNode);
            orderRel.setTarget(n);
            orderRel.setType(segName);
            orderRel.setName("sOrderRel" + numberOfSOrderRels.getAndIncrement());
            graph.addRelation(orderRel);
          }
          lastNode = n;
        }

      }
    }
  }

  /**
   * Use the left/right token index of the spans to create spanning relations
   * when this did not happen yet.
   *
   * @param graph
   * @param nodeByRankID
   * @param numberOfRelations
   */
  private void createMissingSpanningRelations(SDocumentGraph graph,
    FastInverseMap<Long, SNode> nodeByRankID,
    TreeMap<Long, SToken> tokenByIndex,
    Map<String, ComponentEntry> componentForSpan,
    AtomicInteger numberOfRelations)
  {

    // add the missing spanning relations for each continuous span of the graph
    for (SSpan span : graph.getSpans())
    {
      long pre=1;
      RelannisNodeFeature featSpan = RelannisNodeFeature.extract(span);      
      ComponentEntry spanComponent = componentForSpan.get(span.getId());
      if (spanComponent != null && featSpan != null 
        && featSpan.getLeftToken() >= 0 && featSpan.getRightToken() >= 0)
      {
        for (long i = featSpan.getLeftToken(); i <= featSpan.getRightToken();
          i++)
        {
          SToken tok = tokenByIndex.get(i);
          if (tok != null)
          {
            boolean missing = true;
            List<SRelation<SNode, SNode>> existingRelations = graph.getRelations(span.getId(),
              tok.getId());
            if (existingRelations != null)
            {
              for (Relation e : existingRelations)
              {
                if (e instanceof SSpanningRelation)
                {
                  missing = false;
                  break;
                }
              }
            } // end if relations exist

            if (missing)
            {
              String type = "c";

              SLayer layer
                = findOrAddSLayer(spanComponent.getNamespace(), graph);

              createNewRelation(graph, span, tok, null, type,
                spanComponent.getId(), layer,
                pre++, nodeByRankID, numberOfRelations);
            }
          } // end if token exists
        } // end for each covered token index
      }
    } // end for each span
  }

  private static void setMatchedIDs(SDocumentGraph docGraph, Match match)
  {
    List<String> allUrisAsString = new LinkedList<>();
    for (URI u : match.getSaltIDs())
    {
      allUrisAsString.add(u.toASCIIString());
    }
    // set the matched keys
    SFeature featIDs = SaltFactory.createSFeature();
    featIDs.setNamespace(ANNIS_NS);
    featIDs.setName(FEAT_MATCHEDIDS);
    featIDs.setValue(Joiner.on(",").join(allUrisAsString));
    docGraph.addFeature(featIDs);

    SFeature featAnnos = SaltFactory.createSFeature();
    featAnnos.setNamespace(ANNIS_NS);
    featAnnos.setName(FEAT_MATCHEDANNOS);
    featAnnos.setValue(Joiner.on(",").join(match.getAnnos()));
    docGraph.addFeature(featAnnos);

  }

  private void createSinglePrimaryText(SDocumentGraph graph, long textID,
    TreeMap<Long, String> tokenTexts, TreeMap<Long, SToken> tokenByIndex)
  {
    STextualDS textDataSource = SaltFactory.createSTextualDS();
    textDataSource.setName("sText" + textID);
    graph.addNode(textDataSource);

    StringBuilder sbText = new StringBuilder();
    Iterator<Map.Entry<Long, String>> itToken = tokenTexts.entrySet().
      iterator();
    long index = 0;
    while (itToken.hasNext())
    {
      Map.Entry<Long, String> e = itToken.next();
      SToken tok = tokenByIndex.get(e.getKey());

      SFeature rawFeature = tok.getFeature(SaltUtil.createQName(ANNIS_NS, FEAT_RELANNIS_NODE));
      if (rawFeature != null)
      {
        RelannisNodeFeature feat = (RelannisNodeFeature) rawFeature.getValue();

        if (feat.getTextRef() == textID)
        {
          STextualRelation textRel = SaltFactory.createSTextualRelation();
          textRel.setSource(tok);
          textRel.setTarget(textDataSource);
          textRel.setStart(sbText.length());
          textRel.setEnd(sbText.length() + e.getValue().length());

          textRel.setName("sTextRel" + textID + "_" + (index++));

          textRel.setTarget(textDataSource);
          graph.addRelation(textRel);

          sbText.append(e.getValue());
          if (itToken.hasNext())
          {
            sbText.append(" ");
          }
        }
      }
    }

    textDataSource.setText(sbText.toString());
  }

  private void createPrimaryTexts(SDocumentGraph graph,
    TreeSet<Long> allTextIDs, TreeMap<Long, String> tokenTexts,
    TreeMap<Long, SToken> tokenByIndex)
  {
    for (long textID : allTextIDs)
    {
      createSinglePrimaryText(graph, textID, tokenTexts, tokenByIndex);
    }

  }

  private SNode createOrFindNewNode(ResultSet resultSet,
    SDocumentGraph graph, TreeSet<Long> allTextIDs,
    TreeMap<Long, String> tokenTexts,
    TreeMap<Long, SToken> tokenByIndex,
    TreeMap<String, TreeMap<Long, String>> nodeBySegmentationPath,
    SolutionKey<?> key,
    FastInverseMap<Long, SNode> nodeByRankID) throws SQLException
  {
    String name = stringValue(resultSet, NODE_TABLE, "node_name");
    String saltID = stringValue(resultSet, NODE_TABLE, "salt_id");
    if (saltID == null)
    {
      // fallback to the name
      saltID = name;
    }
    long internalID = longValue(resultSet, "node", "id");

    String relationType = stringValue(resultSet, COMPONENT_TABLE, "type");

    long tokenIndex = longValue(resultSet, NODE_TABLE, "token_index");
    boolean isToken = !resultSet.wasNull();

    org.eclipse.emf.common.util.URI nodeURI =  graph.getDocument().getPath();

    nodeURI = nodeURI.appendFragment(saltID);
    SStructuredNode node = (SStructuredNode) graph.getNode(nodeURI.toString());
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

      node.setName(name);
      node.setId(nodeURI.toString());

      setFeaturesForNode(node, internalID, resultSet);

      Object nodeId = key.getNodeId(resultSet,
        outerQueryTableAccessStrategy);

      graph.addNode(node);
      Integer matchedNode = key.getMatchedNodeIndex(nodeId);
      if (matchedNode != null)
      {
        addLongSFeature(node, FEAT_MATCHEDNODE, matchedNode);
      }

      mapLayer(node, graph, resultSet);

      long textRef = longValue(resultSet, NODE_TABLE, "text_ref");
      allTextIDs.add(textRef);

    }
    else if ("c".equals(relationType) && isToken == false)
    {
      node = testAndFixNonSpan(node, nodeByRankID);
    }

    String nodeAnnoValue
      = stringValue(resultSet, NODE_ANNOTATION_TABLE, "value");
    String nodeAnnoNameSpace = stringValue(resultSet, NODE_ANNOTATION_TABLE,
      "namespace");
    String nodeAnnoName = stringValue(resultSet, NODE_ANNOTATION_TABLE, "name");
    if (!resultSet.wasNull())
    {
      String fullName = (nodeAnnoNameSpace == null || nodeAnnoNameSpace.
        isEmpty() ? "" : (nodeAnnoNameSpace
          + "::")) + nodeAnnoName;
      SAnnotation anno = node.getAnnotation(fullName);
      if (anno == null)
      {
        anno = SaltFactory.createSAnnotation();
        anno.setNamespace(nodeAnnoNameSpace);
        anno.setName(nodeAnnoName);
        anno.setValue(nodeAnnoValue);
        node.addAnnotation(anno);
      }
    }

    // prepare SOrderingRelation if the node is part of a segmentation path
    String segName = stringValue(resultSet, "node", "seg_name");
    if (segName != null)
    {
      long left = longValue(resultSet, "node", "seg_index");
      // only nodes that might be valid leafs
      // since we are sorting everything by preorder the real leafs will be the
      // last ones
      if (!nodeBySegmentationPath.containsKey(segName))
      {
        nodeBySegmentationPath.put(segName, new TreeMap<Long, String>());
      }
      nodeBySegmentationPath.get(segName).put(left, node.getId());

    }

    return node;
  }

  private SToken createSToken(long tokenIndex, ResultSet resultSet,
    TreeMap<Long, String> tokenTexts,
    TreeMap<Long, SToken> tokenByIndex) throws SQLException
  {
    SToken tok = SaltFactory.createSToken();

    // get spanned text of token
    tokenTexts.put(tokenIndex, stringValue(resultSet, NODE_TABLE, "span"));
    tokenByIndex.put(tokenIndex, tok);

    return tok;
  }

  private SStructuredNode createOtherSNode(ResultSet resultSet) throws
    SQLException
  {
    // check if we have span, early detection of spans will spare
    // us calls to recreateNode() which is quite costly since it
    // removes nodes/relations and this is something Salt does not handle
    // efficiently
    String relationType = stringValue(resultSet, COMPONENT_TABLE, "type");
    if ("c".equals(relationType))
    {
      SSpan span = SaltFactory.createSSpan();
      return span;
    }
    else
    {
      // default fallback is a SStructure
      SStructure struct = SaltFactory.createSStructure();
      return struct;
    }
  }

  private void setFeaturesForNode(SStructuredNode node, long internalID,
    ResultSet resultSet) throws SQLException
  {

    SFeature feat = SaltFactory.createSFeature();
    feat.setNamespace(ANNIS_NS);
    feat.setName(FEAT_RELANNIS_NODE);

    RelannisNodeFeature val = new RelannisNodeFeature();
    val.setInternalID(longValue(resultSet, "node", "id"));
    val.setCorpusRef(longValue(resultSet, "node", "corpus_ref"));
    val.setTextRef(longValue(resultSet, "node", "text_ref"));
    val.setLeft(longValue(resultSet, "node", "left"));
    val.setLeftToken(longValue(resultSet, "node", "left_token"));
    val.setRight(longValue(resultSet, "node", "right"));
    val.setRightToken(longValue(resultSet, "node", "right_token"));
    val.setTokenIndex(longValue(resultSet, "node", "token_index"));
    val.setSegIndex(longValue(resultSet, "node", "seg_index"));
    val.setSegName(stringValue(resultSet, "node", "seg_name"));
    feat.setValue(val);

    node.addFeature(feat);
  }

  private void mapLayer(SStructuredNode node, SDocumentGraph graph,
    ResultSet resultSet)
    throws SQLException
  {
    String namespace = stringValue(resultSet, NODE_TABLE, "namespace");
    List<SLayer> layerList = graph.getLayerByName(namespace);
    SLayer layer = (layerList != null && layerList.size() > 0)
      ? layerList.get(0) : null;
    if (layer == null)
    {
      layer = SaltFactory.createSLayer();
      layer.setName(namespace);
      graph.addLayer(layer);
    }
    node.addLayer(layer);
  }

  private void addLongSFeature(SNode node, String name,
    long value) throws SQLException
  {
    SFeature feat = SaltFactory.createSFeature();
    feat.setNamespace(ANNIS_NS);
    feat.setName(name);
    feat.setValue(value);
    node.addFeature(feat);
  }

//non used functions, commmented out in order to avoid some findbugs warnings 
//  private void addStringSFeature(SNode node, String name,
//    String value) throws SQLException
//  {
//    SFeature feat = SaltFactory.createSFeature();
//    feat.setNamespace(ANNIS_NS);
//    feat.setName(name);
//    feat.setValue(value);
//    node.addFeature(feat);
//  }
//  
//  private void addLongSFeature(SNode node, ResultSet resultSet, String name,
//    String table, String tupleName) throws SQLException
//  {
//    addLongSFeature(node, name, longValue(resultSet, table, tupleName));
//  }
//  
//  private void addStringSFeature(SNode node, ResultSet resultSet, String name,
//    String table, String tupleName) throws SQLException
//  {
//    addStringSFeature(node, name, stringValue(resultSet, table, tupleName));
//  }
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
      node = SaltFactory.createSSpan();
    }
    else if (clazz == SStructure.class)
    {
      node = SaltFactory.createSStructure();
    }
    else
    {
      throw new UnsupportedOperationException(
        "no node creation possible for class: "
        + clazz.getName());
    }
    moveNodeProperties(oldNode, node, oldNode.getGraph());

    return node;
  }

  private void updateMapAfterRecreatingNode(SNode oldNode, SNode newNode,
    FastInverseMap<Long, SNode> nodeByRankID)
  {
    // get *all* keys associated with this node
    List<Long> keys = nodeByRankID.getKeys(oldNode);
    for (Long id : keys)
    {
      nodeByRankID.put(id, newNode);
    }
  }

  private void moveNodeProperties(SStructuredNode oldNode, SStructuredNode newNode,
    SGraph graph)
  {
    Validate.notNull(oldNode);
    Validate.notNull(newNode);

    // step 1: collect every information that is need in a separate variable
    String id = oldNode.getId();
    String name = oldNode.getName();
    Set<SAnnotation> annotations = new LinkedHashSet<>(oldNode.getAnnotations());
    Set<SFeature> features = new LinkedHashSet<>(oldNode.getFeatures());
    Set<SProcessingAnnotation> processingAnnotations = new LinkedHashSet<>(oldNode.getProcessingAnnotations());
    Set<SMetaAnnotation> metaAnnotations = new LinkedHashSet<>(oldNode.getMetaAnnotations());
    Set<SLayer> nodeLayers = new LinkedHashSet<>(oldNode.getLayers());
    Multimap<SRelation, SLayer> layerOfRelation = ArrayListMultimap.create();
    List<SRelation<SNode,SNode>> inRelations = new LinkedList<>(graph.getInRelations(oldNode.
      getId()));
    List<SRelation<SNode,SNode>> outRelations = new LinkedList<>(graph.getOutRelations(oldNode.
      getId()));
    
    // step 2: remove the old node from everything it is connected to
    for (SRelation<SNode,SNode> rel : inRelations)
    {
      if (rel.getLayers() != null)
      {
        layerOfRelation.putAll(rel, rel.getLayers());
      }
      graph.removeRelation(rel);
    }
        
    for (SRelation<SNode,SNode> rel : outRelations)
    {
      if (rel.getLayers() != null)
      {
        layerOfRelation.putAll(rel, rel.getLayers());
      }
      graph.removeRelation(rel);
    }
    graph.removeNode(oldNode);

    
    // step 3: add the new node to everything it should be connected to
    newNode.setName(name);
    newNode.setId(id);
    graph.addNode(newNode);
    
    for (SAnnotation anno : annotations)
    {
      newNode.addAnnotation(anno);
    }
    for (SFeature feat : features)
    {
      // filter the features, do not include salt::SNAME 
      if (!(SaltUtil.SALT_NAMESPACE.equals(feat.getNamespace())
        && SaltUtil.FEAT_NAME.equals(
          feat.getName())))
      {
        newNode.addFeature(feat);
      }
    }
    for (SProcessingAnnotation proc : processingAnnotations)
    {
      newNode.addProcessingAnnotation(proc);
    }
    for (SMetaAnnotation meta : metaAnnotations)
    {
      newNode.addMetaAnnotation(meta);
    }
    
    for(SLayer l : nodeLayers)
    {
      l.addNode(newNode);
    }
    for (SRelation rel : inRelations)
    {
      rel.setTarget(newNode);
      graph.addRelation(rel);
      if (layerOfRelation.containsKey(rel))
      {
        for (SLayer l : layerOfRelation.get(rel))
        {
          l.addRelation(rel);
        }
      }      
    }
    
    for (SRelation rel : outRelations)
    {
      rel.setSource(newNode);
      graph.addRelation(rel);
      if (layerOfRelation.containsKey(rel))
      {
        for (SLayer l : layerOfRelation.get(rel))
        {
          l.addRelation(rel);
        }
      }
    }
  }

  private SRelation findExistingRelation(SDocumentGraph graph,
    SNode sourceNode, SNode targetNode, String relationName, SLayer layer)
  {
    SRelation rel = null;

    List<SRelation<SNode,SNode>> existingRelations = graph.getRelations(sourceNode.getId(),
      targetNode.getId());
    if (existingRelations != null)
    {
      for (Relation e : existingRelations)
      {
        // only select the relation that has the same type ("edge_name" and
        // the same layer "edge_namespace")
        if (e instanceof SRelation)
        {
          SRelation existingRel = (SRelation) e;

          boolean noType = existingRel.getType() == null ;
          if (((noType && relationName == null) || (!noType && existingRel.
            getType().equals(relationName)))
            && existingRel.getLayers().contains(layer))
          {
            rel = existingRel;
            break;
          }

        }
      }
    }
    return rel;
  }

  private SRelation createNewRelation(SDocumentGraph graph,
    SStructuredNode sourceNode,
    SNode targetNode, String relationName, String type, long componentID,
    SLayer layer, long pre,
    FastInverseMap<Long, SNode> nodeByRankID, AtomicInteger numberOfRelations)
  {

    SRelation rel = null;

    if (null != type)
    // create new relation
    {

      switch (type)
      {
        case "d":
          SDominanceRelation domrel = SaltFactory.
            createSDominanceRelation();
          // always set a name by ourself since the SDocumentGraph#basicAddRelation()
          // functions otherwise real slow
          domrel.setName("sDomRel" + numberOfRelations.incrementAndGet());
          rel = domrel;
          if (sourceNode != null && !(sourceNode instanceof SStructure))
          {
            log.debug("Mismatched source type: should be SStructure");
            SNode oldNode = sourceNode;
            sourceNode = recreateNode(SStructure.class, sourceNode);
            updateMapAfterRecreatingNode(oldNode, sourceNode, nodeByRankID);
          }

          if (relationName == null || relationName.isEmpty())
          {
            // check if there is an relation which connects the nodes in the same 
            // layer but has a non-empty relation name
            if (handleArtificialDominanceRelation(graph,
              sourceNode, targetNode,
              rel, layer, componentID,
              pre))
            {
              // don't include this relation
              rel = null;
            }
          }

          break;
        case "c":
          SSpanningRelation spanrel = SaltFactory.
            createSSpanningRelation();
          // always set a name by ourself since the SDocumentGraph#basicAddRelation()
          // functions is real slow otherwise
          spanrel.setName("sSpanRel" + numberOfRelations.incrementAndGet());
          rel = spanrel;
          sourceNode = testAndFixNonSpan(sourceNode, nodeByRankID);
          break;
        case "p":
          SPointingRelation pointingrel = SaltFactory.
            createSPointingRelation();
          pointingrel.setName("sPointingRel" + numberOfRelations.
            incrementAndGet());
          rel = pointingrel;
          break;
        default:
          throw new IllegalArgumentException("Invalid type " + type
            + " for new Relation");
      }

      try
      {
        if (rel != null)
        {
          rel.setType(relationName);

          RelannisEdgeFeature featRelation = new RelannisEdgeFeature();
          featRelation.setPre(pre);
          featRelation.setComponentID(componentID);

          SFeature sfeatRelation = SaltFactory.createSFeature();
          sfeatRelation.setNamespace(ANNIS_NS);
          sfeatRelation.setName(FEAT_RELANNIS_EDGE);
          sfeatRelation.setValue(featRelation);
          rel.addFeature(sfeatRelation);

          rel.setSource(sourceNode);

          if ("c".equals(type) && !(targetNode instanceof SToken))
          {
            log.warn("invalid relation detected: target node ({}) "
              + "of a coverage relation (from: {}, internal id {}) was not a token",
              new Object[]
              {
                targetNode.getName(), sourceNode == null ? "null" : sourceNode.
                  getName(), "" + pre
              });
          }
          else
          {
            rel.setTarget(targetNode);
            graph.addRelation(rel);
            layer.addRelation(rel);
          }

        }
      }
      catch (SaltException ex)
      {
        log.warn("invalid relation detected", ex);
      }
    }

    return rel;
  }

  /**
   * In ANNIS there is a special combined dominance component which has an empty
   * name, but which should not directly be included in the Salt graph.
   *
   * This functions checks if a dominance relation with empty name has a
   * "mirror" relation which is inside the same layer and between the same nodes
   * but has an relation name. If yes the original dominance relation is an
   * artificial one. The function will return true in this case and update the
   * mirror relation to include information about the artificial dominance
   * relation.
   *
   * @param graph
   * @param rel
   * @parem layer
   * @param componentID
   * @param pre
   * @return True if the dominance relation was an artificial one
   */
  private boolean handleArtificialDominanceRelation(SDocumentGraph graph,
    SNode source, SNode target,
    SRelation rel, SLayer layer,
    long componentID, long pre)
  {
    List<SRelation<SNode,SNode>> mirrorRelations = graph.getRelations(source.getId(),
      target.getId());
    if (mirrorRelations != null && mirrorRelations.size() > 0)
    {
      for (Relation mirror : mirrorRelations)

      {
        if (mirror != rel && mirror instanceof SRelation)
        {
          // check layer
          SRelation mirrorRel = (SRelation) mirror;

          Set<SLayer> mirrorLayers = mirrorRel.getLayers();
          if (mirrorLayers != null)
          {
            for (SLayer mirrorLayer : mirrorLayers)
            {
              if (mirrorLayer == layer)
              {
                // adjust the feature of the mirror relation to include
                // information about the artificial dominance relation
                RelannisEdgeFeature mirrorFeat = RelannisEdgeFeature.
                  extract(mirrorRel);
                mirrorFeat.setArtificialDominanceComponent(componentID);
                mirrorFeat.setArtificialDominancePre(pre);
                mirrorRel.removeLabel(ANNIS_NS, FEAT_RELANNIS_EDGE);
                mirrorRel.createFeature(ANNIS_NS, FEAT_RELANNIS_EDGE,
                  mirrorFeat);

                return true;
              }
            }
          }
        }
      }
    }

    return false;
  }

  private void addRelationAnnotations(ResultSet resultSet, SRelation rel)
    throws SQLException
  {
    String relationAnnoValue = stringValue(resultSet, EDGE_ANNOTATION_TABLE,
      "value");
    String relationAnnoNameSpace = stringValue(resultSet, EDGE_ANNOTATION_TABLE,
      "namespace");
    String relationAnnoName = stringValue(resultSet, EDGE_ANNOTATION_TABLE,
      "name");
    if (!resultSet.wasNull())
    {
      String fullName = relationAnnoNameSpace == null ? ""
        : relationAnnoNameSpace
        + "::" + relationAnnoName;
      SAnnotation anno = rel.getAnnotation(fullName);
      if (anno == null)
      {
        anno = SaltFactory.createSAnnotation();
        anno.setNamespace(relationAnnoNameSpace == null ? ""
          : relationAnnoNameSpace);
        anno.setName(relationAnnoName);
        anno.setValue(relationAnnoValue);
        rel.addAnnotation(anno);
      }
    } // end if relationAnnoName exists
  }

  /**
   * Tests if the source node is not a span and fixes this if necessary
   *
   * @param sourceNode The source node to check.
   * @param nodeByRankID
   * @return Either the original span or a new created one
   */
  private SSpan testAndFixNonSpan(SStructuredNode sourceNode,
    FastInverseMap<Long, SNode> nodeByRankID)
  {
    if (sourceNode != null && !(sourceNode instanceof SSpan))
    {
      log.debug("Mismatched source type: should be SSpan");
      SNode oldNode = sourceNode;
      sourceNode = recreateNode(SSpan.class, sourceNode);
      updateMapAfterRecreatingNode(oldNode, sourceNode, nodeByRankID);
    }
    return (SSpan) sourceNode;
  }

  private void createRelation(ResultSet resultSet, SDocumentGraph graph,
    FastInverseMap<Long, SNode> nodeByRankID, SNode targetNode,
    AtomicInteger numberOfRelations) throws
    SQLException
  {
    long parent = longValue(resultSet, RANK_TABLE, "parent");
    if (resultSet.wasNull())
    {
      return;
    }

    long pre = longValue(resultSet, RANK_TABLE, "pre");
    long componentID = longValue(resultSet, RANK_TABLE, "component_id");
    String relationNamespace = stringValue(resultSet, COMPONENT_TABLE,
      "namespace");
    if (relationNamespace == null)
    {
      relationNamespace = "default_ns";
    }
    String relationName = stringValue(resultSet, COMPONENT_TABLE, "name");
    String type = stringValue(resultSet, COMPONENT_TABLE, "type");

    SStructuredNode sourceNode = (SStructuredNode) nodeByRankID.get(parent);

    if (sourceNode == null)
    {
      // the relation is not fully included in the result
      return;
    }

    SLayer layer = findOrAddSLayer(relationNamespace, graph);

    SRelation rel;
    if (!resultSet.wasNull())
    {

      rel = findExistingRelation(graph, sourceNode, targetNode, relationName,
        layer);

      if (rel == null)
      {
        rel = createNewRelation(graph, sourceNode, targetNode, relationName,
          type,
          componentID, layer, pre, nodeByRankID, numberOfRelations);
      } // end if no existing relation

      // add relation annotations if relation was successfully created
      if (rel != null)
      {
        addRelationAnnotations(resultSet, rel);
      }
    }
  }

  /**
   * Retrieves an existing layer by it's name or creates and adds a new one if
   * not existing yet
   *
   * @param name
   * @param graph
   * @return Either the old or the newly created layer
   */
  private SLayer findOrAddSLayer(String name, SDocumentGraph graph)
  {
    List<SLayer> layerList = graph.getLayerByName(name);
    SLayer layer = (layerList != null && layerList.size() > 0)
      ? layerList.get(0) : null;
    if (layer == null)
    {
      layer = SaltFactory.createSLayer();
      layer.setName(name);
      graph.addLayer(layer);
    }
    return layer;
  }

  /**
   * Sets additional match (global) information about the matched nodes and
   * annotations.
   *
   * This will add the {@link AnnisConstants#FEAT_MATCHEDIDS) to all {@link SDocument} elements of the
   * salt project.
   *
   * @param p The salt project to add the features to.
   * @param matchGroup A list of matches in the same order as the corpus graphs
   * of the salt project.
   */
  public static void addMatchInformation(SaltProject p, MatchGroup matchGroup)
  {
    int matchIndex = 0;
    for (Match m : matchGroup.getMatches())
    {
      // get the corresponding SDocument of the salt project    
      SCorpusGraph corpusGraph = p.getCorpusGraphs().get(matchIndex);
      SDocument doc = corpusGraph.getDocuments().get(0);

      setMatchedIDs(doc.getDocumentGraph(), m);

      matchIndex++;
    }
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

  protected boolean booleanValue(ResultSet resultSet, String table,
    String column)
    throws SQLException
  {
    return resultSet.getBoolean(outerQueryTableAccessStrategy.columnName(table,
      column));
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
  public void setOuterQueryTableAccessStrategy(
    TableAccessStrategy outerQueryTableAccessStrategy)
  {
    this.outerQueryTableAccessStrategy = outerQueryTableAccessStrategy;
  }

  public static class FastInverseMap<KeyType, ValueType>
  {

    private Map<KeyType, ValueType> key2value = new HashMap<>();
    private Map<ValueType, List<KeyType>> values2keys = new HashMap<>();

    /**
     * Wrapper for {@link Map#put(java.lang.Object, java.lang.Object) }
     *
     * @param key
     * @param value
     * @return
     */
    public ValueType put(KeyType key, ValueType value)
    {
      List<KeyType> inverse = values2keys.get(value);
      if (inverse == null)
      {
        inverse = new LinkedList<>();
        values2keys.put(value, inverse);
      }

      inverse.add(key);

      return key2value.put(key, value);
    }

    /**
     * Wrapper for {@link Map#get(java.lang.Object) }
     *
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
      if (result == null)
      {
        result = new LinkedList<>();
        values2keys.put(value, result);
      }

      // always return a copy
      return new LinkedList<>(result);
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

    private final long componentID;
    private final long pre;

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

  public static class ComponentEntry
  {

    private final long id;
    private final char type;
    private final String namespace;
    private final String name;

    public ComponentEntry(long id, char type, String namespace, String name)
    {
      this.id = id;
      this.type = type;
      this.namespace = namespace;
      this.name = name;
    }

    public long getId()
    {
      return id;
    }

    public char getType()
    {
      return type;
    }

    public String getNamespace()
    {
      return namespace;
    }

    public String getName()
    {
      return name;
    }

    @Override
    public String toString()
    {
      return "ComponentEntry{" + "id=" + id + ", type=" + type + ", namespace="
        + namespace + ", name=" + name + '}';
    }

  }

}
