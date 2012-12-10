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
package annis.ql.parser;

import annis.model.QueryNode;
import annis.ql.node.AAndExpr;
import annis.ql.node.AAnyNodeSearchExpr;
import annis.ql.node.ADirectPrecedenceSpec;
import annis.ql.node.AIndirectPrecedenceSpec;
import annis.ql.node.APrecedenceLingOp;
import annis.ql.node.ARangePrecedenceSpec;
import annis.ql.node.ARangeSpec;
import annis.ql.node.PExpr;
import annis.ql.node.PPrecedenceSpec;
import annis.ql.node.Start;
import annis.ql.node.TDigits;
import annis.sqlgen.model.Precedence;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import static annis.ql.parser.AstBuilder.newAndExpr;
import static annis.ql.parser.AstBuilder.newStart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class TransitivePrecedenceOptimizerTest
{

 	private AnnisParser parser;
  private AAnyNodeSearchExpr[] nodes;

  public TransitivePrecedenceOptimizerTest()
  {
    parser = new AnnisParser();
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
   * TransitivePrecedenceOptimizer.
   */
  @Test
  public void testWhereConditions()
  {
    System.out.println("whereConditions");
    
    String aql = "node & node & node & node "
      + "& #1 .3 #2 "
      + "& #2 .5,10 #3 "
      + "& #3 .* #4 "
      + "& #3 .* #2";
    
    Start start = parser.parse(aql);
    
    TransitivePrecedenceOptimizer instance = new TransitivePrecedenceOptimizer();
    start.apply(instance);

    // TODO test that joins are added
    
  }
}
