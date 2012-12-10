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

import annis.ql.node.AAndExpr;
import annis.ql.node.AAnyNodeSearchExpr;
import annis.ql.node.AIndirectPrecedenceSpec;
import annis.ql.node.ALinguisticConstraintExpr;
import annis.ql.node.APrecedenceLingOp;
import annis.ql.node.ARangePrecedenceSpec;
import annis.ql.node.ARangeSpec;
import annis.ql.node.Start;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
   * Test wether new linguistic operators are added.
   */
  @Test
  public void testAddTransitivePrecedenceOperators()
  {
    System.out.println("whereConditions");
    
    // query to extend
    String aql = "node & node & node & node "
      + "& #1 .3 #2 "
      + "& #2 .5,10 #3 "
      + "& #3 .* #4 "
      + "& #3 .* #2";
    
    // perform the initial parsing
    Start start = parser.parse(aql);
    
    // apply the optimizer
    TransitivePrecedenceOptimizer instance = new TransitivePrecedenceOptimizer();
    start.apply(instance);
    
    assertTrue(start.getPExpr() instanceof AAndExpr);
    AAndExpr and = (AAndExpr) start.getPExpr();

    int offset = 8;
    // we only add a concrete number of new linguistic contraints and not more
    // (especially since we might introduce a loop by accident)
    assertEquals(offset + 3, and.getExpr().size());  
    
    // we don't add any new nodes, only constraints
    assertTrue(and.getExpr().get(offset + 0) instanceof ALinguisticConstraintExpr);
    assertTrue(and.getExpr().get(offset + 1) instanceof ALinguisticConstraintExpr);
    assertTrue(and.getExpr().get(offset + 2) instanceof ALinguisticConstraintExpr);
    
    // this constraint must be a precedence operator
    assertTrue(((ALinguisticConstraintExpr) and.getExpr().get(offset + 0)).getLingOp() instanceof APrecedenceLingOp);
    assertTrue(((ALinguisticConstraintExpr) and.getExpr().get(offset + 1)).getLingOp() instanceof APrecedenceLingOp);
    assertTrue(((ALinguisticConstraintExpr) and.getExpr().get(offset + 2)).getLingOp() instanceof APrecedenceLingOp);
    
    // get the precdedence operators
    APrecedenceLingOp op0 = (APrecedenceLingOp) ((ALinguisticConstraintExpr) and.getExpr().get(offset + 0)).getLingOp();
    APrecedenceLingOp op1 = (APrecedenceLingOp) ((ALinguisticConstraintExpr) and.getExpr().get(offset + 1)).getLingOp();
    APrecedenceLingOp op2 = (APrecedenceLingOp) ((ALinguisticConstraintExpr) and.getExpr().get(offset + 2)).getLingOp();
    
    // must be specified range
    assertTrue(op0.getPrecedenceSpec() instanceof ARangePrecedenceSpec);
    // unknown range but later than the other node
    assertTrue(op1.getPrecedenceSpec() instanceof AIndirectPrecedenceSpec);
    assertTrue(op2.getPrecedenceSpec() instanceof AIndirectPrecedenceSpec);
    
    // the range is between 8 (3+5) and 13 (3+10) 
    assertEquals("8",
      ((ARangeSpec) ((ARangePrecedenceSpec) op0.getPrecedenceSpec()).getRangeSpec())
      .getMin().getText());
    assertEquals("13",
      ((ARangeSpec) ((ARangePrecedenceSpec) op0.getPrecedenceSpec()).getRangeSpec())
      .getMax().getText());
    
  }
}
