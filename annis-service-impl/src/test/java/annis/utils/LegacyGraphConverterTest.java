/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.utils;

import annis.model.Annotation;
import java.util.Set;
import annis.model.AnnisNode;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.sqlgen.AOMAnnotateSqlGenerator;
import annis.sqlgen.SaltAnnotateSqlGenerator;
import annis.test.CSVResultSetProvider;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.Assert.*;

/**
 *
 * @author thomas
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
{
  "classpath:annis/sqlgen/SqlGenerator-context.xml"
})
public class LegacyGraphConverterTest
{

  @Autowired
  AOMAnnotateSqlGenerator aomSqlGen;

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

    SaltAnnotateSqlGenerator saltSqlGen = new SaltAnnotateSqlGenerator();


    SaltProject p =
      saltSqlGen.extractData(new CSVResultSetProvider(annis.sqlgen.SaltAnnotateSqlGeneratorTest.class.
      getResourceAsStream("SampleAnnotateResult.csv")).getResultSet());

    List<AnnotationGraph> expected =
      aomSqlGen.extractData(new CSVResultSetProvider(annis.sqlgen.SaltAnnotateSqlGeneratorTest.class.
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
          return Long.compare(arg0.getId(), arg1.getId());
        }
      });
      Collections.sort(nodeListResult, new Comparator<AnnisNode>()
      {

        @Override
        public int compare(AnnisNode arg0, AnnisNode arg1)
        {
          return Long.compare(arg0.getId(), arg1.getId());
        }
      });
      
      Iterator<AnnisNode> itNodeExpected = nodeListExpected.iterator();
      Iterator<AnnisNode> itNodeResult = nodeListResult.iterator();
      
      while(itNodeExpected.hasNext() && itNodeResult.hasNext())
      {
        checkAnnisNodeEqual(itNodeExpected.next(), itNodeResult.next());
      }

    }

  }

  private void checkAnnisNodeEqual(AnnisNode n1, AnnisNode n2)
  {

    checkAnnotationSetEqual(n1.getNodeAnnotations(), n2.getNodeAnnotations());
    checkAnnotationSetEqual(n1.getEdgeAnnotations(), n2.getEdgeAnnotations());

  }

  private void checkAnnisEdgeEqual(Edge n1, Edge n2)
  {
    checkAnnotationSetEqual(n1.getAnnotations(), n2.getAnnotations());

  }

  private void checkAnnotationSetEqual(Set<Annotation> annos1,
    Set<Annotation> annos2)
  {
    for (Annotation a : annos1)
    {
      assertTrue("annotation " + a.getQualifiedName() + "->" + a.getName()
        + " from set 1 is not included in set 2", annos2.contains(a));
    }

    for (Annotation a : annos2)
    {
      assertTrue("annotation " + a.getQualifiedName() + "->" + a.getName()
        + " from set 2 is not included in set 1", annos2.contains(a));
    }
  }
}
