/*
 * Copyright 2013 SFB 632.
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
package annis.ql.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@RunWith(value= Parameterized.class)
public class AnnisParserAntlrTest
{
  
  private String aql;
  private String expected;
  
  public AnnisParserAntlrTest(String aql, String expected)
  {
    this.aql = aql;
    this.expected = expected;
  }
  
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    Object[][] data = new Object[][]
    {
      {"tok", "XXX"},
      {"/abc/", "XXX"},
      {" (node & cat=/NP/ & #1 . #2) | (/das/ & tok!=/Haus/ & #3 . #4) ", "XXX"}
    };
    return Arrays.asList(data);
  }
  
  @BeforeClass
  public static void setUpClass()
  {
  }
  
  @AfterClass
  public static void tearDownClass()
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
   * Test of parse method, of class AnnisParserAntlr.
   */
  @Test
  public void testParse()
  {
    System.out.println("parse " + aql);
    List<Long> corpusList = new LinkedList<Long>();
    corpusList.add(1234l);
    
    AnnisParserAntlr instance = new AnnisParserAntlr();
    
    QueryData result = instance.parse(aql, corpusList);
    assertNotNull(result);
    assertEquals(expected, result.toString());
    
  }

 
}