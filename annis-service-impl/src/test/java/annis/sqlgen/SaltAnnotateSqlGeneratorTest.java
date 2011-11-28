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

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import annis.test.CSVResultSetProvider;
import java.sql.SQLException;
import static org.mockito.MockitoAnnotations.initMocks;
import au.com.bytecode.opencsv.CSVReader;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNamedElement;
import java.sql.ResultSet;
import java.util.Comparator;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SaltAnnotateSqlGeneratorTest
{

  CSVResultSetProvider resultSetProvider;
  private CSVReader sample;
  private String[] currentLine;
  private SaltProject project;

  public SaltAnnotateSqlGeneratorTest()
  {
  }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  @AfterClass
  public static void tearDownClass() throws Exception
  {
  }

  @Before
  public void setUp() throws SQLException
  {
    initMocks(this);

    resultSetProvider = new CSVResultSetProvider(getClass().getResourceAsStream(
      "SampleAnnotateResult.csv"));

    ResultSet resultSet = resultSetProvider.getResultSet();

    SaltAnnotateSqlGenerator instance = new SaltAnnotateSqlGenerator();

    project = instance.extractData(resultSet);
    assertNotNull(project);

  }

  @After
  public void tearDown()
  {
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
  }
  
  public static class NameComparator implements Comparator<SNamedElement>
  {

    @Override
    public int compare(SNamedElement arg0, SNamedElement arg1)
    {
      return arg0.getSName().compareTo(arg1.getSName());
    }    
  }
}
