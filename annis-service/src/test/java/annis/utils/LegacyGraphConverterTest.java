/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.utils;

import annis.AnnisXmlContextLoader;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.sqlgen.AomAnnotateExtractor;
import annis.sqlgen.ArrayCorpusPathExtractor;
import annis.sqlgen.CorpusPathExtractor;
import annis.sqlgen.PostgreSqlArraySolutionKey;
import annis.sqlgen.SaltAnnotateExtractor;
import annis.sqlgen.SolutionKey;
import annis.sqlgen.TestAnnotateSqlGenerator;
import annis.test.CsvResultSetProvider;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.corpus_tools.salt.common.SaltProject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author thomas
 */
@RunWith(SpringJUnit4ClassRunner.class)
// TODO: do not text context only for annopool
@ContextConfiguration(locations =
{
  "file:src/main/distribution/conf/spring/Common.xml",
  "file:src/main/distribution/conf/spring/SqlGenerator.xml",
  "file:src/main/distribution/conf/spring/Dao.xml"
}, loader=AnnisXmlContextLoader.class)
public class LegacyGraphConverterTest
{

  @Autowired
  AomAnnotateExtractor aomSqlGen;

  public LegacyGraphConverterTest()
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
  public void setUp()
  {
  }

  @After
  public void tearDown()
  {
  }

  /**
   * Test of convertToAOM method, of class LegacyGraphConverter.
   */
  @Test
  public void testConvertToAOM() throws SQLException
  {

    SaltAnnotateExtractor saltExtractor = new SaltAnnotateExtractor() {
      @Override
      protected SolutionKey<?> createSolutionKey()
      {
        PostgreSqlArraySolutionKey<Long> key = new PostgreSqlArraySolutionKey<>();
        key.setKeyColumnName("key");
        key.setIdColumnName("id");
        return key;
      }
    };
    CorpusPathExtractor corpusPathExtractor = new ArrayCorpusPathExtractor();
    saltExtractor.setCorpusPathExtractor(corpusPathExtractor);

    TestAnnotateSqlGenerator.setupOuterQueryFactsTableColumnAliases(saltExtractor);
    
    List<Match> matches = new ArrayList<>();
    matches.add(Match.parseFromString("salt:/pcc2/4282/#tok_155 tiger::pos::salt:/pcc2/4282#tok_156"));
    MatchGroup matchGroup = new MatchGroup(matches);
    
    SaltProject p =
      saltExtractor.extractData(new CsvResultSetProvider(annis.sqlgen.SaltAnnotateExtractorTest.class.
      getResourceAsStream("SampleAnnotateResult.csv")).getResultSet());
    SaltAnnotateExtractor.addMatchInformation(p, matchGroup);

    List<AnnotationGraph> expected =
      aomSqlGen.extractData(new CsvResultSetProvider(annis.sqlgen.SaltAnnotateExtractorTest.class.
      getResourceAsStream("SampleAnnotateResult.csv")).getResultSet());

    List<AnnotationGraph> result = LegacyGraphConverter.convertToAOM(p);

    assertEquals(expected.size(), result.size());
    Iterator<AnnotationGraph> itGraphExpected = expected.iterator();
    Iterator<AnnotationGraph> itGraphResult = result.iterator();

    while (itGraphExpected.hasNext() && itGraphResult.hasNext())
    {

      AnnotationGraph graphExpected = itGraphExpected.next();
      AnnotationGraph graphResult = itGraphResult.next();

      List<AnnisNode> nodeListExpected = graphExpected.getNodes();
      List<AnnisNode> nodeListResult = graphResult.getNodes();

      assertEquals(nodeListExpected.size(), nodeListResult.size());
      

      Collections.sort(nodeListExpected, new Comparator<AnnisNode>()
      {

        @Override
        public int compare(AnnisNode arg0, AnnisNode arg1)
        {
          return Long.valueOf(arg0.getId()).compareTo(Long.valueOf(arg1.getId()));
        }
      });
      Collections.sort(nodeListResult, new Comparator<AnnisNode>()
      {

        @Override
        public int compare(AnnisNode arg0, AnnisNode arg1)
        {
          return Long.valueOf(arg0.getId()).compareTo(Long.valueOf(arg1.getId()));
        }
      });

      Iterator<AnnisNode> itNodeExpected = nodeListExpected.iterator();
      Iterator<AnnisNode> itNodeResult = nodeListResult.iterator();

      while (itNodeExpected.hasNext() && itNodeResult.hasNext())
      {
        checkAnnisNodeEqual(itNodeExpected.next(), itNodeResult.next());
      }

    }

  }

  private void checkAnnisNodeEqual(AnnisNode n1, AnnisNode n2)
  {

    checkAnnotationSetEqual(n1.getNodeAnnotations(), n2.getNodeAnnotations());
    checkAnnotationSetEqual(n1.getEdgeAnnotations(), n2.getEdgeAnnotations());

    assertEquals("corpus number must be equal", n1.getCorpus(), n2.getCorpus());
    assertEquals("node ID must be equal", n1.getId(), n2.getId());
    assertEquals("left must be equal", n1.getLeft(), n2.getLeft());    
    assertEquals("left token must be equal", n1.getLeftToken(), n2.getLeftToken());
    assertEquals("matched node in query must be equal", n1.getMatchedNodeInQuery(), n2.getMatchedNodeInQuery());
    assertEquals("node name must be equal", n1.getName(), n2.getName());
    assertEquals("node namespace must be equal", n1.getNamespace(), n2.getNamespace());
    assertEquals("right must be equal", n1.getRight(), n2.getRight());
    assertEquals("right token must be equal", n1.getRightToken(), n2.getRightToken());
    assertEquals("spanned text of node must be equal", n1.getSpannedText(), n2.getSpannedText());
    assertEquals("text ID of node must be equal", n1.getTextId(), n2.getTextId());
    assertEquals("token index must be equal", n1.getTokenIndex(), n2.getTokenIndex());

    Set<Edge> out1 = n1.getOutgoingEdges();
    Set<Edge> out2 = n2.getOutgoingEdges();

    assertEquals("number of outgoing edges must be equal for [" + n1.toString() + "]", out1.size(), out2.size());
    for (Edge e1 : out1)
    {
      assertTrue("edge [" + e1 +"] must be contained in outgoing edges " + out2.toString(),out2.contains(e1));
      for (Edge e2 : out2)
      {
        if (e1.getPre() == e2.getPre())
        {
          checkAnnisEdgeEqual(e1, e2);
          break;
        }
      }
    }

    Set<Edge> in1 = n1.getIncomingEdges();
    Set<Edge> in2 = n2.getIncomingEdges();

    assertEquals("number of incoming edges must be the same for node " + n1.getName(), 
      in1.size(), in2.size());
    for (Edge e1 : in1)
    {
      assertTrue("edge " + e1.getPre() + " must be included", 
        in2.contains(e1));
      for (Edge e2 : in2)
      {
        if (e1.getPre() == e2.getPre() && e1.getComponentID() == e2.getComponentID())
        {
          checkAnnisEdgeEqual(e1, e2);
          break;
        }
      }
    }

  }

  private void checkAnnisEdgeEqual(Edge e1, Edge e2)
  {
    checkAnnotationSetEqual(e1.getAnnotations(), e1.getAnnotations());

    assertEquals("source IDs of edges must be equal", 
      e1.getSource().getId(), e2.getSource().getId());
    assertEquals("destiniation IDs of edges must be equal", 
      e1.getDestination().getId(), e2.getDestination().getId());
  }

  private void checkAnnotationSetEqual(Set<Annotation> annos1,
    Set<Annotation> annos2)
  {
    for (Annotation a : annos1)
    {
      assertTrue("annotation " + a.getQualifiedName() + "->" + a.getValue()
        + " from set 1 is not included in set 2", annos2.contains(a));
    }

    for (Annotation a : annos2)
    {
      assertTrue("annotation " + a.getQualifiedName() + "->" + a.getValue()
        + " from set 2 is not included in set 1", annos2.contains(a));
    }
  }
}
