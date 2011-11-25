/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.utils;

import annis.model.AnnotationGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author thomas
 */
public class LegacyGraphConverterTest
{
  
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
  public void testConvertToAOM()
  {
    System.out.println("convertToAOM");
    SaltProject p = null;
    List expResult = null;
    List result = LegacyGraphConverter.convertToAOM(p);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of convertToAnnotationGraph method, of class LegacyGraphConverter.
   */
  @Test
  public void testConvertToAnnotationGraph()
  {
    System.out.println("convertToAnnotationGraph");
    SDocumentGraph docGraph = null;
    Long[] matchedIDs = null;
    AnnotationGraph expResult = null;
    AnnotationGraph result =
      LegacyGraphConverter.convertToAnnotationGraph(docGraph, matchedIDs);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of convertToPaulaInline method, of class LegacyGraphConverter.
   */
  @Test
  public void testConvertToPaulaInline()
  {
    System.out.println("convertToPaulaInline");
    SCorpusGraph g = null;
    String expResult = "";
    String result = LegacyGraphConverter.convertToPaulaInline(g);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
}
