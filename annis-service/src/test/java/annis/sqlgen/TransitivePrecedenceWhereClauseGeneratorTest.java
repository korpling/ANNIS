/*
 * Copyright 2012 SFB 632.
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

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.sqlgen.model.Precedence;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class TransitivePrecedenceWhereClauseGeneratorTest
{
  // an example node

  private QueryNode[] nodes;

  public TransitivePrecedenceWhereClauseGeneratorTest()
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
   * Test of whereConditions method, of class
   * TransitivePrecedenceWhereClauseGenerator.
   */
  @Test
  public void testWhereConditions()
  {
    System.out.println("whereConditions");
    QueryData queryData = null;
    List<QueryNode> alternative = new LinkedList<QueryNode>();
    String indent = "";
    
    nodes = new QueryNode[4];
    for(int i=0; i < 4; i++)
    {
      nodes[i] = new QueryNode(i);
      alternative.add(nodes[i]);
    }

    nodes[0].addJoin(new Precedence(nodes[1], 3));
    nodes[1].addJoin(new Precedence(nodes[2], 5, 10));
    nodes[2].addJoin(new Precedence(nodes[3]));
    
    TransitivePrecedenceWhereClauseGenerator instance = new TransitivePrecedenceWhereClauseGenerator();
    
    Set expResult = new TreeSet();



    Set result = instance.whereConditions(queryData, alternative, indent);

    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
}
