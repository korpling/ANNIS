/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.sqlgen;

import annis.test.CsvResultSetProvider;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNamedElement;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SaltAnnotateExtractorTest
{

  // class under test
  private CsvResultSetProvider resultSetProviderSingleText;
  private CsvResultSetProvider resultSetProviderMultiText;
  private SaltAnnotateExtractor instance;
  
  // dependencies
  private PostgreSqlArraySolutionKey<String> solutionKey = new PostgreSqlArraySolutionKey<>();
  

  @Before
  public void setUp() throws SQLException
  {
    initMocks(this);
    
    solutionKey.setKeyColumnName("key");

    resultSetProviderSingleText = new CsvResultSetProvider(getClass().getResourceAsStream(
      "SampleAnnotateResult.csv"));
    
    resultSetProviderMultiText = new CsvResultSetProvider(getClass().getResourceAsStream(
      "SampleAnnotateResult_MultiText.csv"));

    instance = new SaltAnnotateExtractor() {
      protected SolutionKey<?> createSolutionKey() {
        return solutionKey;
      }
    };
    CorpusPathExtractor corpusPathExtractor = new ArrayCorpusPathExtractor();
    instance.setCorpusPathExtractor(corpusPathExtractor);

    TestAnnotateSqlGenerator.setupOuterQueryFactsTableColumnAliases(instance);

  }

  @Test
  public void testCorpusGraph() throws Exception
  {
    SaltProject project = instance.extractData(resultSetProviderSingleText.getResultSet());
    assertNotNull(project);
    
    assertEquals(1, project.getCorpusGraphs().size());

    SCorpusGraph corpusGraph = project.getCorpusGraphs().get(0);

    assertEquals(1, corpusGraph.getCorpora().size());
    assertEquals("pcc2", corpusGraph.getCorpora().get(0).getName());

    assertEquals(1, corpusGraph.getDocuments().size());
    assertEquals("4282", corpusGraph.getDocuments().get(0).getName());

  }

  @Test
  public void testLayerNames() throws SQLException
  {
    SaltProject project = instance.extractData(resultSetProviderSingleText.getResultSet());
    assertNotNull(project);
    
    SDocumentGraph g = project.getCorpusGraphs().get(0).getDocuments().get(0).
      getDocumentGraph();

    List<SLayer> layers = new ArrayList<>(g.getLayers());

    Collections.sort(layers, new NameComparator());

    assertEquals(6, layers.size());
    assertEquals("default_ns", layers.get(0).getName());
    assertEquals("dep", layers.get(1).getName());
    assertEquals("exmaralda", layers.get(2).getName());
    assertEquals("mmax", layers.get(3).getName());
    assertEquals("rst", layers.get(4).getName());
    assertEquals("tiger", layers.get(5).getName());
  }

  @Test
  public void testLayerNodes() throws SQLException
  {
    SaltProject project = instance.extractData(resultSetProviderSingleText.getResultSet());
    assertNotNull(project);
    
    SDocumentGraph g = project.getCorpusGraphs().get(0).getDocuments().get(0).
      getDocumentGraph();

    List<SNode> n = new ArrayList<>(g.getLayerByName("exmaralda").get(0).getNodes());
    Collections.sort(n, new NameComparator());
    assertEquals(9, n.size());
    assertEquals("Focus_newInfSeg_10", n.get(0).getName());
    assertEquals("Focus_newInfSeg_9", n.get(1).getName());
    assertEquals("Inf-StatSeg_29", n.get(2).getName());
    assertEquals("Inf-StatSeg_30", n.get(3).getName());
    assertEquals("NPSeg_29", n.get(4).getName());
    assertEquals("NPSeg_30", n.get(5).getName());
    assertEquals("PPSeg_7", n.get(6).getName());
    assertEquals("SentSeg_10", n.get(7).getName());
    assertEquals("SentSeg_9", n.get(8).getName());

    n = new ArrayList<>(g.getLayerByName("mmax").get(0).getNodes());
    Collections.sort(n, new NameComparator());
    assertEquals(5, n.size());
    assertEquals("primmarkSeg_1000154", n.get(0).getName());
    assertEquals("primmarkSeg_60", n.get(1).getName());
    assertEquals("sentenceSeg_50010", n.get(2).getName());
    assertEquals("sentenceSeg_50011", n.get(3).getName());
    assertEquals("sentenceSeg_5009", n.get(4).getName());

    n = new ArrayList<>(g.getLayerByName("tiger").get(0).getNodes());
    Collections.sort(n, new NameComparator());
    assertEquals(10, n.size());
    assertEquals("const_50", n.get(0).getName());
    assertEquals("const_52", n.get(1).getName());
    assertEquals("const_54", n.get(2).getName());
    assertEquals("const_55", n.get(3).getName());
    assertEquals("const_56", n.get(4).getName());
    assertEquals("const_57", n.get(5).getName());
    assertEquals("const_58", n.get(6).getName());
    assertEquals("const_59", n.get(7).getName());
    assertEquals("const_60", n.get(8).getName());
    assertEquals("const_61", n.get(9).getName());

    n = new ArrayList<>(g.getLayerByName("default_ns").get(0).getNodes());
    Collections.sort(n, new NameComparator());
    assertEquals(12, n.size());
    assertEquals("tok_150", n.get(0).getName());
    assertEquals("tok_151", n.get(1).getName());
    assertEquals("tok_152", n.get(2).getName());
    assertEquals("tok_153", n.get(3).getName());
    assertEquals("tok_154", n.get(4).getName());
    assertEquals("tok_155", n.get(5).getName());
    assertEquals("tok_156", n.get(6).getName());
    assertEquals("tok_157", n.get(7).getName());
    assertEquals("tok_158", n.get(8).getName());
    assertEquals("tok_159", n.get(9).getName());
    assertEquals("tok_160", n.get(10).getName());
    assertEquals("tok_161", n.get(11).getName());

    n = new ArrayList<>(g.getLayerByName("rst").get(0).getNodes());
    Collections.sort(n, new NameComparator());
    assertEquals(9, n.size());
    assertEquals("u0", n.get(0).getName());
    assertEquals("u10", n.get(1).getName());
    assertEquals("u11", n.get(2).getName());
    assertEquals("u12", n.get(3).getName());
    assertEquals("u20", n.get(4).getName());
    assertEquals("u23", n.get(5).getName());
    assertEquals("u24", n.get(6).getName());
    assertEquals("u27", n.get(7).getName());
    assertEquals("u28", n.get(8).getName());
    
    assertEquals(0, g.getLayerByName("dep").get(0).getNodes().size());
  }

  @Test
  public void testLayerRelations() throws SQLException
  {
    SaltProject project = instance.extractData(resultSetProviderSingleText.getResultSet());
    assertNotNull(project);
    
    SDocumentGraph g = project.getCorpusGraphs().get(0).getDocuments().get(0).
      getDocumentGraph();


    // dep //
    List<SRelation<SNode, SNode>> e = new ArrayList<>(g.getLayerByName("dep").get(0).getRelations());
    Collections.sort(e, new EdgeComparator());

    assertEquals(9, e.size());

    assertEquals("tok_150", e.get(0).getSource().getName());
    assertEquals("tok_151", e.get(0).getTarget().getName());

    assertEquals("tok_152", e.get(1).getSource().getName());
    assertEquals("tok_153", e.get(1).getTarget().getName());

    assertEquals("tok_156", e.get(2).getSource().getName());
    assertEquals("tok_154", e.get(2).getTarget().getName());

    assertEquals("tok_156", e.get(3).getSource().getName());
    assertEquals("tok_155", e.get(3).getTarget().getName());

    assertEquals("tok_156", e.get(4).getSource().getName());
    assertEquals("tok_157", e.get(4).getTarget().getName());

    assertEquals("tok_157", e.get(5).getSource().getName());
    assertEquals("tok_158", e.get(5).getTarget().getName());

    assertEquals("tok_158", e.get(6).getSource().getName());
    assertEquals("tok_160", e.get(6).getTarget().getName());

    assertEquals("tok_160", e.get(7).getSource().getName());
    assertEquals("tok_159", e.get(7).getTarget().getName());

    assertEquals("tok_160", e.get(8).getSource().getName());
    assertEquals("tok_161", e.get(8).getTarget().getName());

    // exmaralda //
    e = new ArrayList<>(g.getLayerByName("exmaralda").get(0).getRelations());
    Collections.sort(e, new EdgeComparator());

    assertEquals(30, e.size());

    assertEquals("Focus_newInfSeg_10", e.get(0).getSource().getName());
    assertEquals("tok_154", e.get(0).getTarget().getName());
    assertEquals("Focus_newInfSeg_10", e.get(1).getSource().getName());
    assertEquals("tok_155", e.get(1).getTarget().getName());
    assertEquals("Focus_newInfSeg_10", e.get(2).getSource().getName());
    assertEquals("tok_156", e.get(2).getTarget().getName());
    assertEquals("Focus_newInfSeg_10", e.get(3).getSource().getName());
    assertEquals("tok_157", e.get(3).getTarget().getName());
    assertEquals("Focus_newInfSeg_10", e.get(4).getSource().getName());
    assertEquals("tok_158", e.get(4).getTarget().getName());
    assertEquals("Focus_newInfSeg_10", e.get(5).getSource().getName());
    assertEquals("tok_159", e.get(5).getTarget().getName());
    assertEquals("Focus_newInfSeg_10", e.get(6).getSource().getName());
    assertEquals("tok_160", e.get(6).getTarget().getName());

    assertEquals("Focus_newInfSeg_9", e.get(7).getSource().getName());
    assertEquals("tok_150", e.get(7).getTarget().getName());
    assertEquals("Focus_newInfSeg_9", e.get(8).getSource().getName());
    assertEquals("tok_151", e.get(8).getTarget().getName());
    assertEquals("Focus_newInfSeg_9", e.get(9).getSource().getName());
    assertEquals("tok_152", e.get(9).getTarget().getName());
    
    assertEquals("Inf-StatSeg_29", e.get(10).getSource().getName());
    assertEquals("tok_150", e.get(10).getTarget().getName());
    assertEquals("Inf-StatSeg_29", e.get(11).getSource().getName());
    assertEquals("tok_151", e.get(11).getTarget().getName());
    
    assertEquals("Inf-StatSeg_30", e.get(12).getSource().getName());
    assertEquals("tok_155", e.get(12).getTarget().getName());

    assertEquals("NPSeg_29", e.get(13).getSource().getName());
    assertEquals("tok_150", e.get(13).getTarget().getName());
    assertEquals("NPSeg_29", e.get(14).getSource().getName());
    assertEquals("tok_151", e.get(14).getTarget().getName());
    
    assertEquals("NPSeg_30", e.get(15).getSource().getName());
    assertEquals("tok_155", e.get(15).getTarget().getName());
    
    assertEquals("PPSeg_7", e.get(16).getSource().getName());
    assertEquals("tok_150", e.get(16).getTarget().getName());
    assertEquals("PPSeg_7", e.get(17).getSource().getName());
    assertEquals("tok_151", e.get(17).getTarget().getName());


    assertEquals("SentSeg_10", e.get(18).getSource().getName());
    assertEquals("tok_154", e.get(18).getTarget().getName());
    assertEquals("SentSeg_10", e.get(19).getSource().getName());
    assertEquals("tok_155", e.get(19).getTarget().getName());
    assertEquals("SentSeg_10", e.get(20).getSource().getName());
    assertEquals("tok_156", e.get(20).getTarget().getName());
    assertEquals("SentSeg_10", e.get(21).getSource().getName());
    assertEquals("tok_157", e.get(21).getTarget().getName());
    assertEquals("SentSeg_10", e.get(22).getSource().getName());
    assertEquals("tok_158", e.get(22).getTarget().getName());
    assertEquals("SentSeg_10", e.get(23).getSource().getName());
    assertEquals("tok_159", e.get(23).getTarget().getName());
    assertEquals("SentSeg_10", e.get(24).getSource().getName());
    assertEquals("tok_160", e.get(24).getTarget().getName());
    assertEquals("SentSeg_10", e.get(25).getSource().getName());
    assertEquals("tok_161", e.get(25).getTarget().getName());
    
    
    assertEquals("SentSeg_9", e.get(26).getSource().getName());
    assertEquals("tok_150", e.get(26).getTarget().getName());
    assertEquals("SentSeg_9", e.get(27).getSource().getName());
    assertEquals("tok_151", e.get(27).getTarget().getName());
    assertEquals("SentSeg_9", e.get(28).getSource().getName());
    assertEquals("tok_152", e.get(28).getTarget().getName());
    assertEquals("SentSeg_9", e.get(29).getSource().getName());
    assertEquals("tok_153", e.get(29).getTarget().getName());
    
    // mmax, only control samples //
    e = new ArrayList<>(g.getLayerByName("mmax").get(0).getRelations());
    Collections.sort(e, new EdgeComparator());

    assertEquals(14, e.size());

    assertEquals("primmarkSeg_60", e.get(1).getSource().getName());
    assertEquals("tok_150", e.get(1).getTarget().getName());
    assertEquals("sentenceSeg_50010", e.get(7).getSource().getName());
    assertEquals("tok_158", e.get(7).getTarget().getName());

    // tiger, only control samples //
    e = new ArrayList<>(g.getLayerByName("tiger").get(0).getRelations());
    Collections.sort(e, new EdgeComparator());

    assertEquals(17, e.size());

    assertEquals("const_59", e.get(9).getSource().getName());
    assertEquals("tok_160", e.get(9).getTarget().getName());
    assertEquals("const_61", e.get(16).getSource().getName());
    assertEquals("tok_156", e.get(16).getTarget().getName());

    // urml, only control samples //
    e = new ArrayList<>(g.getLayerByName("rst").get(0).getRelations());
    Collections.sort(e, new EdgeComparator());

    assertEquals(20, e.size());

    assertEquals("u0", e.get(0).getSource().getName());
    assertEquals("u28", e.get(0).getTarget().getName());
    assertEquals("u11", e.get(5).getSource().getName());
    assertEquals("tok_153", e.get(5).getTarget().getName());

  }

  @Test
  public void testRelationType() throws SQLException
  {
    SaltProject project = instance.extractData(resultSetProviderSingleText.getResultSet());
    assertNotNull(project);
    
    SDocumentGraph g = project.getCorpusGraphs().get(0).getDocuments().get(0).
      getDocumentGraph();

    for (SRelation<? extends SNode,? extends SNode> r : g.getRelations())
    {
      if(!(r instanceof STextualRelation))
      {
        assertEquals(1, r.getLayers().size());
        String layerName = r.getLayers().iterator().next().getName();

        if ("exmaralda".equals(layerName) || "urml".equals(layerName) || "mmax".
          equals(layerName))
        {
          assertTrue("instance of SSpanningRelation",
            r instanceof SSpanningRelation);
        }
        else if ("dep".equals(layerName))
        {
          assertTrue("instance of SPointingRelation",
            r instanceof SPointingRelation);
        }
        else if ("tiger".equals(layerName))
        {
          assertTrue("instance of SDominanceRelation",
            r instanceof SDominanceRelation);
        }
      }
    }
  }
  
  @Test
  public void testMultipleTextGeneration() throws SQLException
  {
    SaltProject project = instance.extractData(resultSetProviderMultiText.getResultSet());
    assertNotNull(project);
    
    SDocumentGraph g = project.getCorpusGraphs().get(0)
      .getDocuments().get(0).getDocumentGraph();
    
    assertEquals(3, g.getTextualDSs().size());
    
  }

  public static class NameComparator implements Comparator<SNamedElement>
  {

    @Override
    public int compare(SNamedElement arg0, SNamedElement arg1)
    {
      return arg0.getName().compareTo(arg1.getName());
    }
  }

  public static class EdgeComparator implements Comparator<SRelation<SNode,SNode>>
  {

    @Override
    public int compare(SRelation<SNode,SNode> arg0, SRelation<SNode,SNode> arg1)
    {
      int result = arg0.getSource().getName().compareTo(arg1.getSource().
        getName());
      if (result == 0)
      {
        result = arg0.getTarget().getName().compareTo(arg1.getTarget().
          getName());
      }

      if (result == 0)
      {
        String t0 = arg0.getType();
        String t1 = arg1.getType();

        if (t0 == null && t1 == null)
        {
          result = 0;
        }
        else if (t0 == null && t1 != null)
        {
          result = -1;
        }
        else if (t0 != null && t1 == null)
        {
          result = +1;
        }
        else
        {
          result = t0.compareTo(t1);
        }
      }
      return result;
    }
  }
}