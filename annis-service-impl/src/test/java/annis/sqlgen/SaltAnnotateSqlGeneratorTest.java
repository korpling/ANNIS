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

import annis.test.CSVResultSetProvider;
import java.sql.SQLException;
import static org.mockito.MockitoAnnotations.initMocks;
import au.com.bytecode.opencsv.CSVReader;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import java.sql.ResultSet;
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
    
    SaltProject p = instance.extractData(resultSet);
    
    assertNotNull(p);
    
    assertEquals(1, p.getSCorpusGraphs().size());
    
    SCorpusGraph corpusGraph =  p.getSCorpusGraphs().get(0);
    
    assertEquals(1, corpusGraph.getSCorpora().size());
    assertEquals("pcc2_plus", corpusGraph.getSCorpora().get(0).getSName());
    
    assertEquals(1, corpusGraph.getSDocuments().size());
    assertEquals("4282", corpusGraph.getSDocuments().get(0).getSName());
    
    // TODO: test actual properties of the graph
  }

}
