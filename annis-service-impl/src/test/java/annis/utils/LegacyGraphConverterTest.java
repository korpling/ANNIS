/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.utils;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import annis.model.AnnotationGraph;
import annis.sqlgen.AOMAnnotateSqlGenerator;
import annis.sqlgen.SaltAnnotateSqlGenerator;
import annis.test.CSVResultSetProvider;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.sql.SQLException;
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
@ContextConfiguration(locations={"classpath:annis/sqlgen/SqlGenerator-context.xml"})
public class LegacyGraphConverterTest
{
  
  @Autowired AOMAnnotateSqlGenerator aomSqlGen;

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
  @Ignore
  public void testConvertToAOM() throws SQLException
  {
    
    SaltAnnotateSqlGenerator saltSqlGen = new SaltAnnotateSqlGenerator();
    
    
    SaltProject p = saltSqlGen.extractData(new CSVResultSetProvider(annis.sqlgen.SaltAnnotateSqlGeneratorTest.class.
      getResourceAsStream("SampleAnnotateResult.csv")).getResultSet());
    
    List<AnnotationGraph> expected = aomSqlGen.extractData(new CSVResultSetProvider(annis.sqlgen.SaltAnnotateSqlGeneratorTest.class.
      getResourceAsStream("SampleAnnotateResult.csv")).getResultSet());
    
    List<AnnotationGraph> result = LegacyGraphConverter.convertToAOM(p);
    
    assertEquals(expected, result);
  }
}
