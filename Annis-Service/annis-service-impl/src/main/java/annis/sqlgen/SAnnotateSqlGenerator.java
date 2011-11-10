/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.*;

import annis.dao.Match;
import annis.model.AnnisConstants;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructuredNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SAnnotateSqlGenerator extends BaseSqlGenerator<List<Match>, SaltProject>
{

  private TableAccessStrategy factsTas;

  public SAnnotateSqlGenerator()
  {

    // FIXME: totally ugly, but the query has fixed column names 
    // (and needs its own column aliasing)
    // TableAccessStrategyFactory wants a corpus selection 
    // strategy
    // solution: build AnnisNodes with API and refactor 
    // SqlGenerator to accept GROUP BY nodes
    Map<String, String> nodeColumns = new HashMap<String, String>();
    nodeColumns.put("namespace", "node_namespace");
    nodeColumns.put("name", "node_name");

    Map<String, String> nodeAnnotationColumns = new HashMap<String, String>();
    nodeAnnotationColumns.put("node_ref", "id");
    nodeAnnotationColumns.put("namespace", "node_annotation_namespace");
    nodeAnnotationColumns.put("name", "node_annotation_name");
    nodeAnnotationColumns.put("value", "node_annotation_value");

    Map<String, String> edgeAnnotationColumns = new HashMap<String, String>();
    nodeAnnotationColumns.put("rank_ref", "pre");
    edgeAnnotationColumns.put("namespace", "edge_annotation_namespace");
    edgeAnnotationColumns.put("name", "edge_annotation_name");
    edgeAnnotationColumns.put("value", "edge_annotation_value");

    Map<String, String> edgeColumns = new HashMap<String, String>();
    edgeColumns.put("node_ref", "id");

    Map<String, String> componentColumns = new HashMap<String, String>();
    componentColumns.put("id", "component_id");
    componentColumns.put("name", "edge_name");
    componentColumns.put("namespace", "edge_namespace");
    componentColumns.put("type", "edge_type");

    edgeColumns.put("name", "edge_name");
    edgeColumns.put("namespace", "edge_namespace");

    Map<String, Map<String, String>> columnAliases =
      new HashMap<String, Map<String, String>>();
    columnAliases.put(TableAccessStrategy.NODE_TABLE, nodeColumns);
    columnAliases.put(TableAccessStrategy.NODE_ANNOTATION_TABLE,
      nodeAnnotationColumns);
    columnAliases.put(TableAccessStrategy.EDGE_ANNOTATION_TABLE,
      edgeAnnotationColumns);
    columnAliases.put(TableAccessStrategy.RANK_TABLE, edgeColumns);
    columnAliases.put(COMPONENT_TABLE, componentColumns);

    factsTas = new TableAccessStrategy(null);
    factsTas.setColumnAliases(columnAliases);
  }

  @Override
  public String toSql(List<Match> queryData)
  {
    throw new NotImplementedException();
  }

  @Override
  public String toSql(List<Match> queryData, int indentBy)
  {
    throw new NotImplementedException();
  }

  @Override
  public SaltProject extractData(ResultSet resultSet)
    throws SQLException, DataAccessException
  {
    SaltProject project = SaltFactory.eINSTANCE.createSaltProject();

    SCorpusGraph corpusGraph = SaltFactory.eINSTANCE.createSCorpusGraph();
    SDocumentGraph graph = SaltFactory.eINSTANCE.createSDocumentGraph();

    // fn: edge pre order value -> edge
    Map<Long, SNode> nodeByPre = new HashMap<Long, SNode>();

    TreeMap<Long, String> tokenTexts = new TreeMap<Long, String>();
    TreeMap<Long, SToken> tokenByIndex = new TreeMap<Long, SToken>();

    // clear mapping functions for this graph
    // assumes that the result set is sorted by key, pre
    nodeByPre.clear();

    // set the matched keys
    SDocument document = SaltFactory.eINSTANCE.createSDocument();
    document.setSDocumentGraph(graph);

    int rowNum = 0;
    while (resultSet.next())
    {
      if (rowNum == 0)
      {

        Array sqlKey = resultSet.getArray("key");
        Validate.isTrue(!resultSet.wasNull(),
          "Match group identifier must not be null");
        Validate.isTrue(sqlKey.getBaseType() == Types.BIGINT,
          "Key in database must be from the type \"bigint\" but was \""
          + sqlKey.getBaseTypeName() + "\"");

        Long[] keyArray = (Long[]) sqlKey.getArray();
        List<Long> key = Arrays.asList(keyArray);

        ArrayList<String> path =
          new ArrayList<String>(Arrays.asList((String[]) resultSet.getArray(
          "path").getArray()));
        Collections.reverse(path);

        SCorpus rootCorpus = SaltFactory.eINSTANCE.createSCorpus();
        rootCorpus.setSName(path.get(0));
        corpusGraph.addSNode(rootCorpus);
        Validate.isTrue(path.size() >= 2,
          "Corpus path must be have at least two members (toplevel and document)");
        SCorpus corpus = rootCorpus;

        for (int i = 1; i < path.size() - 1; i++)
        {
          SCorpus subcorpus = SaltFactory.eINSTANCE.createSCorpus();
          subcorpus.setSName(path.get(i));
          corpusGraph.addSSubCorpus(corpus, subcorpus);
          corpus = subcorpus;
        }
        document.setSDocumentGraph(graph);
        document.setSName(path.get(path.size() - 1));
        corpusGraph.addSDocument(corpus, document);

        SFeature feature = SaltFactory.eINSTANCE.createSFeature();
        feature.setSName(AnnisConstants.MATCHED_IDS);
        feature.setSValue(key);
        graph.addSFeature(feature);
      }

      // get node data
      SNode node = createOrFindNewNode(resultSet, graph, tokenTexts,
        tokenByIndex);
      long pre = longValue(resultSet, RANK_TABLE, "pre");
      if (!resultSet.wasNull())
      {
        nodeByPre.put(pre, node);
        createRelation(resultSet, graph, nodeByPre, node);
      }

      rowNum++;
    }

    STextualDS textDataSource = SaltFactory.eINSTANCE.createSTextualDS();
    graph.addSNode(textDataSource);

    StringBuilder sbText = new StringBuilder();
    Iterator<Map.Entry<Long, String>> itToken = tokenTexts.entrySet().iterator();
    while (itToken.hasNext())
    {
      Map.Entry<Long, String> e = itToken.next();
      SToken tok = tokenByIndex.get(e.getKey());

      STextualRelation textRel = SaltFactory.eINSTANCE.createSTextualRelation();
      textRel.setSSource(tok);
      textRel.setSTarget(textDataSource);
      textRel.setSStart(sbText.length());
      textRel.setSEnd(sbText.length() + e.getValue().length());

      textRel.setSTextualDS(textDataSource);
      graph.addSRelation(textRel);

      sbText.append(e.getValue());
      if (itToken.hasNext())
      {
        sbText.append(" ");
      }
    }

    textDataSource.setSText(sbText.toString());

    return project;
  }

  private SNode createOrFindNewNode(ResultSet resultSet,
    SDocumentGraph graph, TreeMap<Long, String> tokenTexts,
    TreeMap<Long, SToken> tokenByIndex) throws SQLException
  {
    String id = stringValue(resultSet, NODE_TABLE, "node_name");

    long tokenIndex = longValue(resultSet, NODE_TABLE, "token_index");
    boolean isToken = !resultSet.wasNull();

    URI nodeURI = graph.getSElementPath();
    nodeURI = nodeURI.appendFragment(id);
    SStructuredNode node = (SStructuredNode) graph.getSNode(nodeURI.toString());
    if (node == null)
    {
      if (isToken)
      {
        SToken tok = SaltFactory.eINSTANCE.createSToken();
        node = tok;

        // get spanned text of token
        tokenTexts.put(tokenIndex, stringValue(resultSet, NODE_TABLE, "span"));
        tokenByIndex.put(tokenIndex, tok);
      }
      else
      {
        SStructure struct = SaltFactory.eINSTANCE.createSStructure();
        node = struct;
      }

      copyNodeProperties(null, node, graph, id);
    }

    String nodeAnnoValue =
      stringValue(resultSet, NODE_ANNOTATION_TABLE, "value");
    String nodeAnnoNameSpace = stringValue(resultSet, NODE_ANNOTATION_TABLE,
      "namespace");
    String nodeAnnoName = stringValue(resultSet, NODE_ANNOTATION_TABLE, "name");
    if (!resultSet.wasNull())
    {
      String fullName = nodeAnnoNameSpace == null ? "" : nodeAnnoNameSpace
        + "::" + nodeAnnoName;
      SAnnotation anno = node.getSAnnotation(fullName);
      if (anno == null)
      {
        anno = SaltFactory.eINSTANCE.createSAnnotation();
        anno.setSNS(nodeAnnoNameSpace == null ? "" : nodeAnnoNameSpace);
        anno.setSName(nodeAnnoName);
        anno.setSValue(nodeAnnoValue);
        node.addSAnnotation(anno);
      }
    }

    // TODO: what more do we have to do?
    return node;
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
      throw new NotImplementedException("no node creation possible for class: "
        + clazz.getName());
    }

    copyNodeProperties(oldNode, node, oldNode.getSGraph(), null);

    return node;
  }

  private void copyNodeProperties(SStructuredNode from, SStructuredNode to,
    SGraph graph, String fallbackName)
  {

    if (from == null)
    {
      to.setSName(fallbackName);
    }
    else
    {
      to.setSName(from.getSName());
    }

    if (from != null)
    {
      Boolean b = graph.removeNode(from);
      Validate.isTrue(b);
    }

    graph.addNode(to);

    if (from != null)
    {
      for (SAnnotation anno : from.getSAnnotations())
      {
        to.addSAnnotation(anno);
      }
      for (SFeature feat : from.getSFeatures())
      {
        to.addSFeature(feat);
      }
      for (SMetaAnnotation meta : from.getSMetaAnnotations())
      {
        to.addSMetaAnnotation(meta);
      }
    }
  }

  private SRelation createRelation(ResultSet resultSet, SDocumentGraph graph,
    Map<Long, SNode> nodeByPre, SNode targetNode) throws
    SQLException
  {
    long parent = longValue(resultSet, RANK_TABLE, "parent");
    if (resultSet.wasNull())
    {
      return null;
    }

//    long pre = longValue(resultSet, RANK_TABLE, "pre");
    String edgeNamespace = stringValue(resultSet, COMPONENT_TABLE, "namespace");
    String edgeName = stringValue(resultSet, COMPONENT_TABLE, "name");
    String edgeQName = (edgeNamespace == null ? "" : edgeNamespace)
      + ":" + (edgeName == null ? "" : edgeName);
    String type = stringValue(resultSet, COMPONENT_TABLE, "type");

    SStructuredNode sourceNode = (SStructuredNode) nodeByPre.get(parent);

    if (sourceNode == null)
    {
      // the edge is not fully included in the result
      return null;
    }

    SRelation rel = null;
    if (!resultSet.wasNull())
    {

      EList<Edge> existingEdges = graph.getEdges(sourceNode.getSId(),
        targetNode.getSId());
      if (existingEdges != null)
      {
        for (Edge e : existingEdges)
        {
          if (e instanceof SRelation)
          {
            SRelation existingRel = (SRelation) e;
            if (existingRel.getSTypes().contains(edgeQName))
            {
              rel = existingRel;
              break;
            }

          }
        }
      }


      if (rel == null)
      {
        if ("d".equals(type))
        {
          SDominanceRelation domrel = SaltFactory.eINSTANCE.
            createSDominanceRelation();
          rel = domrel;

          if (sourceNode != null && !(sourceNode instanceof SStructure))
          {
            sourceNode = recreateNode(SStructure.class, sourceNode);
            nodeByPre.put(parent, sourceNode);
          }
        }
        else if ("c".equals(type))
        {
          SSpanningRelation spanrel = SaltFactory.eINSTANCE.
            createSSpanningRelation();
          rel = spanrel;

          if (sourceNode != null && !(sourceNode instanceof SSpan))
          {
            sourceNode = recreateNode(SSpan.class, sourceNode);
            nodeByPre.put(parent, sourceNode);
          }
        }
        else if ("p".equals(type))
        {
          SPointingRelation pointingrel = SaltFactory.eINSTANCE.
            createSPointingRelation();
          rel = pointingrel;
        }

        rel.setSSource(nodeByPre.get(parent));
        rel.setSTarget(targetNode);
        rel.addSType(edgeQName);
        graph.addSRelation(rel);
      }

      // add edge annotations
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
      }
    }
    return rel;
  }

  protected long longValue(ResultSet resultSet, String table, String column)
    throws SQLException
  {
    return resultSet.getLong(factsTas.columnName(table, column));
  }

  protected String stringValue(ResultSet resultSet, String table, String column)
    throws SQLException
  {
    return resultSet.getString(factsTas.columnName(table, column));
  }
}
