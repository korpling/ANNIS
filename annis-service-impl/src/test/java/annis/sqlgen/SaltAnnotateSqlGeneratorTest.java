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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.junit.Before;
import org.junit.Test;

import annis.test.CSVResultSetProvider;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNamedElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SaltAnnotateSqlGeneratorTest
{

  // class under test
  private CSVResultSetProvider resultSetProvider;
  
  // dependencies
  private AnnisKey annisKey = new AnnisKey();
  
  // test data
  private SaltProject project;

  @Before
  public void setUp() throws SQLException
  {
    initMocks(this);

    resultSetProvider = new CSVResultSetProvider(getClass().getResourceAsStream(
      "SampleAnnotateResult.csv"));

    ResultSet resultSet = resultSetProvider.getResultSet();

    SaltAnnotateSqlGenerator instance = new SaltAnnotateSqlGenerator() {
      protected AnnisKey createAnnisKey() {
        return annisKey;
      }
    };

    project = instance.extractData(resultSet);
    assertNotNull(project);

  }

  @Test
  public void testCorpusGraph() throws Exception
  {
    assertEquals(1, project.getSCorpusGraphs().size());

    SCorpusGraph corpusGraph = project.getSCorpusGraphs().get(0);

    assertEquals(1, corpusGraph.getSCorpora().size());
    assertEquals("pcc2_plus", corpusGraph.getSCorpora().get(0).getSName());

    assertEquals(1, corpusGraph.getSDocuments().size());
    assertEquals("4282", corpusGraph.getSDocuments().get(0).getSName());

  }

  @Test
  public void testLayerNames()
  {
    SDocumentGraph g = project.getSCorpusGraphs().get(0).getSDocuments().get(0).
      getSDocumentGraph();

    EList<SLayer> layers = g.getSLayers();

    ECollections.sort(layers, new NameComparator());

    assertEquals(6, layers.size());
    assertEquals("dep", layers.get(0).getSName());
    assertEquals("exmaralda", layers.get(1).getSName());
    assertEquals("mmax", layers.get(2).getSName());
    assertEquals("tiger", layers.get(3).getSName());
    assertEquals("token_merged", layers.get(4).getSName());
    assertEquals("urml", layers.get(5).getSName());
  }

  @Test
  public void testLayerNodes()
  {
    SDocumentGraph g = project.getSCorpusGraphs().get(0).getSDocuments().get(0).
      getSDocumentGraph();

    EList<SNode> n = g.getSLayerByName("exmaralda").get(0).getSNodes();
    ECollections.sort(n, new NameComparator());
    assertEquals(9, n.size());
    assertEquals("sSpan10", n.get(0).getSName());
    assertEquals("sSpan40", n.get(1).getSName());
    assertEquals("sSpan41", n.get(2).getSName());
    assertEquals("sSpan74", n.get(3).getSName());
    assertEquals("sSpan75", n.get(4).getSName());
    assertEquals("sSpan86", n.get(5).getSName());
    assertEquals("sSpan9", n.get(6).getSName());
    assertEquals("sSpan97", n.get(7).getSName());
    assertEquals("sSpan98", n.get(8).getSName());

    n = g.getSLayerByName("mmax").get(0).getSNodes();
    ECollections.sort(n, new NameComparator());
    assertEquals(5, n.size());
    assertEquals("sSpan121", n.get(0).getSName());
    assertEquals("sSpan124", n.get(1).getSName());
    assertEquals("sSpan151", n.get(2).getSName());
    assertEquals("sSpan152", n.get(3).getSName());
    assertEquals("sSpan153", n.get(4).getSName());

    n = g.getSLayerByName("tiger").get(0).getSNodes();
    ECollections.sort(n, new NameComparator());
    assertEquals(10, n.size());
    assertEquals("const_50", n.get(0).getSName());
    assertEquals("const_52", n.get(1).getSName());
    assertEquals("const_54", n.get(2).getSName());
    assertEquals("const_55", n.get(3).getSName());
    assertEquals("const_56", n.get(4).getSName());
    assertEquals("const_57", n.get(5).getSName());
    assertEquals("const_58", n.get(6).getSName());
    assertEquals("const_59", n.get(7).getSName());
    assertEquals("const_60", n.get(8).getSName());
    assertEquals("const_61", n.get(9).getSName());

    n = g.getSLayerByName("token_merged").get(0).getSNodes();
    ECollections.sort(n, new NameComparator());
    assertEquals(12, n.size());
    assertEquals("tok_150", n.get(0).getSName());
    assertEquals("tok_151", n.get(1).getSName());
    assertEquals("tok_152", n.get(2).getSName());
    assertEquals("tok_153", n.get(3).getSName());
    assertEquals("tok_154", n.get(4).getSName());
    assertEquals("tok_155", n.get(5).getSName());
    assertEquals("tok_156", n.get(6).getSName());
    assertEquals("tok_157", n.get(7).getSName());
    assertEquals("tok_158", n.get(8).getSName());
    assertEquals("tok_159", n.get(9).getSName());
    assertEquals("tok_160", n.get(10).getSName());
    assertEquals("tok_161", n.get(11).getSName());

    n = g.getSLayerByName("urml").get(0).getSNodes();
    ECollections.sort(n, new NameComparator());
    assertEquals(2, n.size());
    assertEquals("sSpan166", n.get(0).getSName());
    assertEquals("sSpan167", n.get(1).getSName());

    assertEquals(0, g.getSLayerByName("dep").get(0).getSNodes().size());
  }

  @Test
  public void testLayerRelations()
  {
    SDocumentGraph g = project.getSCorpusGraphs().get(0).getSDocuments().get(0).
      getSDocumentGraph();


    // dep //
    EList<SRelation> e = g.getSLayerByName("dep").get(0).getSRelations();
    ECollections.sort(e, new EdgeComparator());

    assertEquals(9, e.size());

    assertEquals("tok_150", e.get(0).getSSource().getSName());
    assertEquals("tok_151", e.get(0).getSTarget().getSName());

    assertEquals("tok_152", e.get(1).getSSource().getSName());
    assertEquals("tok_153", e.get(1).getSTarget().getSName());

    assertEquals("tok_156", e.get(2).getSSource().getSName());
    assertEquals("tok_154", e.get(2).getSTarget().getSName());

    assertEquals("tok_156", e.get(3).getSSource().getSName());
    assertEquals("tok_155", e.get(3).getSTarget().getSName());

    assertEquals("tok_156", e.get(4).getSSource().getSName());
    assertEquals("tok_157", e.get(4).getSTarget().getSName());

    assertEquals("tok_157", e.get(5).getSSource().getSName());
    assertEquals("tok_158", e.get(5).getSTarget().getSName());

    assertEquals("tok_158", e.get(6).getSSource().getSName());
    assertEquals("tok_160", e.get(6).getSTarget().getSName());

    assertEquals("tok_160", e.get(7).getSSource().getSName());
    assertEquals("tok_159", e.get(7).getSTarget().getSName());

    assertEquals("tok_160", e.get(8).getSSource().getSName());
    assertEquals("tok_161", e.get(8).getSTarget().getSName());

    // exmaralda //
    e = g.getSLayerByName("exmaralda").get(0).getSRelations();
    ECollections.sort(e, new EdgeComparator());

    assertEquals(30, e.size());

    assertEquals("sSpan10", e.get(0).getSSource().getSName());
    assertEquals("tok_154", e.get(0).getSTarget().getSName());
    assertEquals("sSpan10", e.get(1).getSSource().getSName());
    assertEquals("tok_155", e.get(1).getSTarget().getSName());
    assertEquals("sSpan10", e.get(2).getSSource().getSName());
    assertEquals("tok_156", e.get(2).getSTarget().getSName());
    assertEquals("sSpan10", e.get(3).getSSource().getSName());
    assertEquals("tok_157", e.get(3).getSTarget().getSName());
    assertEquals("sSpan10", e.get(4).getSSource().getSName());
    assertEquals("tok_158", e.get(4).getSTarget().getSName());
    assertEquals("sSpan10", e.get(5).getSSource().getSName());
    assertEquals("tok_159", e.get(5).getSTarget().getSName());
    assertEquals("sSpan10", e.get(6).getSSource().getSName());
    assertEquals("tok_160", e.get(6).getSTarget().getSName());

    assertEquals("sSpan40", e.get(7).getSSource().getSName());
    assertEquals("tok_150", e.get(7).getSTarget().getSName());
    assertEquals("sSpan40", e.get(8).getSSource().getSName());
    assertEquals("tok_151", e.get(8).getSTarget().getSName());

    assertEquals("sSpan41", e.get(9).getSSource().getSName());
    assertEquals("tok_155", e.get(9).getSTarget().getSName());

    assertEquals("sSpan74", e.get(10).getSSource().getSName());
    assertEquals("tok_150", e.get(10).getSTarget().getSName());
    assertEquals("sSpan74", e.get(11).getSSource().getSName());
    assertEquals("tok_151", e.get(11).getSTarget().getSName());

    assertEquals("sSpan75", e.get(12).getSSource().getSName());
    assertEquals("tok_155", e.get(12).getSTarget().getSName());

    assertEquals("sSpan86", e.get(13).getSSource().getSName());
    assertEquals("tok_150", e.get(13).getSTarget().getSName());
    assertEquals("sSpan86", e.get(14).getSSource().getSName());
    assertEquals("tok_151", e.get(14).getSTarget().getSName());

    assertEquals("sSpan9", e.get(15).getSSource().getSName());
    assertEquals("tok_150", e.get(15).getSTarget().getSName());
    assertEquals("sSpan9", e.get(16).getSSource().getSName());
    assertEquals("tok_151", e.get(16).getSTarget().getSName());
    assertEquals("sSpan9", e.get(17).getSSource().getSName());
    assertEquals("tok_152", e.get(17).getSTarget().getSName());

    assertEquals("sSpan97", e.get(18).getSSource().getSName());
    assertEquals("tok_150", e.get(18).getSTarget().getSName());
    assertEquals("sSpan97", e.get(19).getSSource().getSName());
    assertEquals("tok_151", e.get(19).getSTarget().getSName());
    assertEquals("sSpan97", e.get(20).getSSource().getSName());
    assertEquals("tok_152", e.get(20).getSTarget().getSName());
    assertEquals("sSpan97", e.get(21).getSSource().getSName());
    assertEquals("tok_153", e.get(21).getSTarget().getSName());

    assertEquals("sSpan98", e.get(22).getSSource().getSName());
    assertEquals("tok_154", e.get(22).getSTarget().getSName());
    assertEquals("sSpan98", e.get(23).getSSource().getSName());
    assertEquals("tok_155", e.get(23).getSTarget().getSName());
    assertEquals("sSpan98", e.get(24).getSSource().getSName());
    assertEquals("tok_156", e.get(24).getSTarget().getSName());
    assertEquals("sSpan98", e.get(25).getSSource().getSName());
    assertEquals("tok_157", e.get(25).getSTarget().getSName());
    assertEquals("sSpan98", e.get(26).getSSource().getSName());
    assertEquals("tok_158", e.get(26).getSTarget().getSName());
    assertEquals("sSpan98", e.get(27).getSSource().getSName());
    assertEquals("tok_159", e.get(27).getSTarget().getSName());
    assertEquals("sSpan98", e.get(28).getSSource().getSName());
    assertEquals("tok_160", e.get(28).getSTarget().getSName());
    assertEquals("sSpan98", e.get(29).getSSource().getSName());
    assertEquals("tok_161", e.get(29).getSTarget().getSName());

    // mmax, only control samples //
    e = g.getSLayerByName("mmax").get(0).getSRelations();
    ECollections.sort(e, new EdgeComparator());

    assertEquals(14, e.size());

    assertEquals("sSpan151", e.get(2).getSSource().getSName());
    assertEquals("tok_150", e.get(2).getSTarget().getSName());
    assertEquals("sSpan151", e.get(3).getSSource().getSName());
    assertEquals("tok_151", e.get(3).getSTarget().getSName());

    // tiger, only control samples //
    e = g.getSLayerByName("tiger").get(0).getSRelations();
    ECollections.sort(e, new EdgeComparator());

    assertEquals(34, e.size());

    assertEquals("const_59", e.get(19).getSSource().getSName());
    assertEquals("tok_160", e.get(19).getSTarget().getSName());
    assertEquals("const_61", e.get(33).getSSource().getSName());
    assertEquals("tok_156", e.get(33).getSTarget().getSName());

    // urml, only control samples //
    e = g.getSLayerByName("urml").get(0).getSRelations();
    ECollections.sort(e, new EdgeComparator());

    assertEquals(12, e.size());

    assertEquals("sSpan166", e.get(0).getSSource().getSName());
    assertEquals("tok_150", e.get(0).getSTarget().getSName());
    assertEquals("sSpan167", e.get(6).getSSource().getSName());
    assertEquals("tok_156", e.get(6).getSTarget().getSName());

  }

  @Test
  public void testRelationType()
  {
    SDocumentGraph g = project.getSCorpusGraphs().get(0).getSDocuments().get(0).
      getSDocumentGraph();

    for (SRelation r : g.getSRelations())
    {
      if(!(r instanceof STextualRelation))
      {
        assertEquals(1, r.getSLayers().size());
        String layerName = r.getSLayers().get(0).getSName();

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

  public static class NameComparator implements Comparator<SNamedElement>
  {

    @Override
    public int compare(SNamedElement arg0, SNamedElement arg1)
    {
      return arg0.getSName().compareTo(arg1.getSName());
    }
  }

  public static class EdgeComparator implements Comparator<SRelation>
  {

    @Override
    public int compare(SRelation arg0, SRelation arg1)
    {
      int result = arg0.getSSource().getSName().compareTo(arg1.getSSource().
        getSName());
      if (result == 0)
      {
        result = arg0.getSTarget().getSName().compareTo(arg1.getSTarget().
          getSName());
      }

      if (result == 0)
      {
        String t0 = arg0.getSTypes() != null && arg0.getSTypes().size() > 0
          ? arg0.getSTypes().get(0) : null;
        String t1 = arg1.getSTypes() != null && arg1.getSTypes().size() > 0
          ? arg1.getSTypes().get(0) : null;

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