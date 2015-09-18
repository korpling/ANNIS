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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
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
      {"tok", 
        "ALTERNATIVES\n" +
        "\t{node 1; bound to '1'; is a token}"
      },
      {"/abc/", 
        "ALTERNATIVES\n" +
        "\t{node 1; bound to '1'; spans~/abc/}"
      },
      {" (node & cat=/NP/ & #1 . #2) | (/das/ & tok!=/Haus/ & #3 . #4) ", 
        "ALTERNATIVES\n" +
        "\t{node 1; bound to '1'; precedes node 2 (null 1, 1)} AND {node 2; bound to '2'; node labels: [cat ~ NP]}\n" +
        "\t{node 3; bound to '3'; spans~/das/; precedes node 4 (null 1, 1)} AND {node 4; bound to '4'; is a token; spans!~/Haus/}"
      },
      {" \"das\" & ( x#\"Haus\" | x#\"Schaf\") & #1 . #x", 
        "ALTERNATIVES\n" +
        "\t{node 1; bound to '1'; spans=\"das\"; precedes node 2 (null 1, 1)} AND {node 2; bound to 'x'; spans=\"Haus\"}\n" +
        "\t{node 1; bound to '1'; spans=\"das\"; precedes node 3 (null 1, 1)} AND {node 3; bound to 'x'; spans=\"Schaf\"}"
      }
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
    List<Long> corpusList = new LinkedList<>();
    corpusList.add(1234l);
    
    AnnisParserAntlr instance = new AnnisParserAntlr();
    
    QueryData result = instance.parse(aql, corpusList);
    assertNotNull(result);
    assertEquals(expected, result.toString());
    
  }

 
}