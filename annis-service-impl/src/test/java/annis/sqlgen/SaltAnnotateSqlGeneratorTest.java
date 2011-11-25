/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.sqlgen;

import annis.test.CSVResultSetProvider;
import java.sql.SQLException;
import static org.mockito.MockitoAnnotations.initMocks;
import au.com.bytecode.opencsv.CSVReader;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.sql.ResultSet;
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
public class SaltAnnotateSqlGeneratorTest
{
  
  CSVResultSetProvider resultSetProvider;
  private CSVReader sample;
  private String[] currentLine;
  
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
    

  }
  
  @After
  public void tearDown()
  {
  }

  /**
   * Test of extractData method, of class SaltAnnotateSqlGenerator.
   */
  @Test
  public void testExtractData() throws Exception
  {
    System.out.println("extractData");
    ResultSet resultSet = resultSetProvider.getResultSet();
    
    SaltAnnotateSqlGenerator instance = new SaltAnnotateSqlGenerator();
    
    SaltProject result = instance.extractData(resultSet);
    
    assertNotNull(result);
    // TODO: test actual properties of the graph
  }

}
