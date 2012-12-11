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
import annis.ql.node.Start;
import annis.sqlgen.model.Precedence;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:annis/ql/parser/AnnisParser-context.xml"})
public class TransitivePrecedenceOptimizerTest
{

 	private AnnisParser parser;
  
  // QueryAnalysis instance that is managed by Spring (has Adapters injected)
	@Autowired private QueryAnalysis queryAnalysis;
  
  private QueryAnalysis queryAnalysisNoBounds;
  
  public TransitivePrecedenceOptimizerTest()
  {
    parser = new AnnisParser();
  }

  @Before
  public void setUp()
  {
    // always create a fresh instance before each test
    // but copy most of the members from the spring managed QueryAnalysis
    queryAnalysisNoBounds = new QueryAnalysis();
    ClauseAnalysis clause = queryAnalysis.getClauseAnalysis();
    clause.setPrecedenceBound(0);
    queryAnalysisNoBounds.setClauseAnalysis(clause);
    queryAnalysisNoBounds.setDnfTransformer(queryAnalysis.getDnfTransformer());
    queryAnalysisNoBounds.setNodeRelationNormalizer(queryAnalysis.getNodeRelationNormalizer());
    queryAnalysisNoBounds.setPostProcessors(queryAnalysis.getPostProcessors());
  }

  @After
  public void tearDown()
  {
  }

  /**
   * Test wether new linguistic operators are added.
   * 
   * This test uses a fixed upper precedence bound of 50 (as default).
   */
  @Test
  public void testAddTransitivePrecedenceOperatorsWithBound()
  {
    System.out.println("addTransitivePrecedenceOperatorsWithBound");
    
    // query to extend
    String aql = "node & node & node & node "
      + "& #1 .3 #2 "
      + "& #2 .5,10 #3 "
      + "& #3 .* #4 "
      + "& #4 .* #2";
    
    // perform the initial parsing
    Start start = parser.parse(aql);
    queryAnalysis.getClauseAnalysis().setPrecedenceBound(50);
    // optimizer is applied on the fly by the query anaylsis (as injected by Spring)
    QueryData data = queryAnalysis.analyzeQuery(start, new LinkedList<Long>());
    
    assertEquals("alternative added", 1, data.getAlternatives().size());
    List<QueryNode> nodes = data.getAlternatives().get(0);
    
    // no node size change
    assertEquals("no node size change allowed", 4, nodes.size());
    
    // check that we only add a concrete number of new linguistic contraints 
    // and not more
    // (especially since we might introduce a loop by accident)
    assertEquals("wrong number of outgoing joins for node 1", 
      3, nodes.get(0).getJoins().size());
    assertEquals("wrong number of outgoing joins for node 2", 
      2, nodes.get(1).getJoins().size());
    assertEquals("wrong number of outgoing joins for node 3", 
      2, nodes.get(2).getJoins().size());
    assertEquals("wrong number of outgoing joins for node 4", 
      2, nodes.get(3).getJoins().size());
    
    // these constraints must be a precedence operator
    assertTrue("not a precedence operator (node 1)", 
      nodes.get(0).getJoins().get(0) instanceof Precedence);
    assertTrue("not a precedence operator (node 1)", 
      nodes.get(0).getJoins().get(1) instanceof Precedence);
    assertTrue("not a precedence operator (node 1)", 
      nodes.get(0).getJoins().get(2) instanceof Precedence);
    assertTrue("not a precedence operator (node 2)", 
      nodes.get(1).getJoins().get(0) instanceof Precedence);
    assertTrue("not a precedence operator (node 2)", 
      nodes.get(1).getJoins().get(1) instanceof Precedence);
    assertTrue("not a precedence operator (node 3)", 
      nodes.get(2).getJoins().get(0) instanceof Precedence);
    assertTrue("not a precedence operator (node 4)", 
      nodes.get(3).getJoins().get(0) instanceof Precedence);
    
    // test if target nodes are as expected
    assertEquals(2, ((Precedence) nodes.get(0).getJoins().get(0)).getTarget().getId());
    assertEquals(3, ((Precedence) nodes.get(0).getJoins().get(1)).getTarget().getId());
    assertEquals(4, ((Precedence) nodes.get(0).getJoins().get(2)).getTarget().getId());
    
    assertEquals(3, ((Precedence) nodes.get(1).getJoins().get(0)).getTarget().getId());
    assertEquals(4, ((Precedence) nodes.get(1).getJoins().get(1)).getTarget().getId());
    
    assertEquals(4, ((Precedence) nodes.get(2).getJoins().get(0)).getTarget().getId());
    assertEquals(2, ((Precedence) nodes.get(2).getJoins().get(1)).getTarget().getId());
    
    assertEquals(2, ((Precedence) nodes.get(3).getJoins().get(0)).getTarget().getId());
    assertEquals(3, ((Precedence) nodes.get(3).getJoins().get(1)).getTarget().getId());
    
    // test if ranges are correct
    
    // node 1
    assertEquals(3, ((Precedence) nodes.get(0).getJoins().get(0)).getMinDistance());
    assertEquals(3, ((Precedence) nodes.get(0).getJoins().get(0)).getMaxDistance());
    
    assertEquals(8, ((Precedence) nodes.get(0).getJoins().get(1)).getMinDistance());
    assertEquals(13, ((Precedence) nodes.get(0).getJoins().get(1)).getMaxDistance());
    
    assertEquals(9, ((Precedence) nodes.get(0).getJoins().get(2)).getMinDistance());
    assertEquals(63, ((Precedence) nodes.get(0).getJoins().get(2)).getMaxDistance());
    
    // node 2
    assertEquals(5, ((Precedence) nodes.get(1).getJoins().get(0)).getMinDistance());
    assertEquals(10, ((Precedence) nodes.get(1).getJoins().get(0)).getMaxDistance());
    
    assertEquals(6, ((Precedence) nodes.get(1).getJoins().get(1)).getMinDistance());
    assertEquals(60, ((Precedence) nodes.get(1).getJoins().get(1)).getMaxDistance());
    
    // node 3
    assertEquals(1, ((Precedence) nodes.get(2).getJoins().get(0)).getMinDistance());
    assertEquals(50, ((Precedence) nodes.get(2).getJoins().get(0)).getMaxDistance());
    
    assertEquals(2, ((Precedence) nodes.get(2).getJoins().get(1)).getMinDistance());
    assertEquals(100, ((Precedence) nodes.get(2).getJoins().get(1)).getMaxDistance());
    
    // node 4
    assertEquals(1, ((Precedence) nodes.get(3).getJoins().get(0)).getMinDistance());
    assertEquals(50, ((Precedence) nodes.get(3).getJoins().get(0)).getMaxDistance());
    
    assertEquals(6, ((Precedence) nodes.get(3).getJoins().get(1)).getMinDistance());
    assertEquals(60, ((Precedence) nodes.get(3).getJoins().get(1)).getMaxDistance());
  }
  
  /**
   * Test wether new linguistic operators are added.
   * 
   * This test uses no upper precedence bound.
   */
  @Test
  public void testAddTransitivePrecedenceOperatorsWithoutBound()
  {
    System.out.println("addTransitivePrecedenceOperatorsWithoutBound");
    
    // query to extend
    String aql = "node & node & node & node "
      + "& #1 .3 #2 "
      + "& #2 .5,10 #3 "
      + "& #3 .* #4 "
      + "& #4 .* #2";
    
    // perform the initial parsing
    Start start = parser.parse(aql);
    // optimizer is applied on the fly by the query anaylsis (as injected by Spring)
    QueryData data = queryAnalysisNoBounds.analyzeQuery(start, new LinkedList<Long>());
    
    assertEquals("alternative added", 1, data.getAlternatives().size());
    List<QueryNode> nodes = data.getAlternatives().get(0);
    
    // no node size change
    assertEquals("no node size change allowed", 4, nodes.size());
    
    // check that we only add a concrete number of new linguistic contraints 
    // and not more
    // (especially since we might introduce a loop by accident)
    assertEquals("wrong number of outgoing joins for node 1", 
      3, nodes.get(0).getJoins().size());
    assertEquals("wrong number of outgoing joins for node 2", 
      2, nodes.get(1).getJoins().size());
    assertEquals("wrong number of outgoing joins for node 3", 
      2, nodes.get(2).getJoins().size());
    assertEquals("wrong number of outgoing joins for node 4", 
      2, nodes.get(3).getJoins().size());
    
    // these constraints must be a precedence operator
    assertTrue("not a precedence operator (node 1)", 
      nodes.get(0).getJoins().get(0) instanceof Precedence);
    assertTrue("not a precedence operator (node 1)", 
      nodes.get(0).getJoins().get(1) instanceof Precedence);
    assertTrue("not a precedence operator (node 1)", 
      nodes.get(0).getJoins().get(2) instanceof Precedence);
    assertTrue("not a precedence operator (node 2)", 
      nodes.get(1).getJoins().get(0) instanceof Precedence);
    assertTrue("not a precedence operator (node 2)", 
      nodes.get(1).getJoins().get(1) instanceof Precedence);
    assertTrue("not a precedence operator (node 3)", 
      nodes.get(2).getJoins().get(0) instanceof Precedence);
    assertTrue("not a precedence operator (node 4)", 
      nodes.get(3).getJoins().get(0) instanceof Precedence);
    
    // test if target nodes are as expected
    assertEquals(2, ((Precedence) nodes.get(0).getJoins().get(0)).getTarget().getId());
    assertEquals(3, ((Precedence) nodes.get(0).getJoins().get(1)).getTarget().getId());
    assertEquals(4, ((Precedence) nodes.get(0).getJoins().get(2)).getTarget().getId());
    
    assertEquals(3, ((Precedence) nodes.get(1).getJoins().get(0)).getTarget().getId());
    assertEquals(4, ((Precedence) nodes.get(1).getJoins().get(1)).getTarget().getId());
    
    assertEquals(4, ((Precedence) nodes.get(2).getJoins().get(0)).getTarget().getId());
    assertEquals(2, ((Precedence) nodes.get(2).getJoins().get(1)).getTarget().getId());
    
    assertEquals(2, ((Precedence) nodes.get(3).getJoins().get(0)).getTarget().getId());
    assertEquals(3, ((Precedence) nodes.get(3).getJoins().get(1)).getTarget().getId());
    
    // test if ranges are correct
    
    // node 1
    assertEquals(3, ((Precedence) nodes.get(0).getJoins().get(0)).getMinDistance());
    assertEquals(3, ((Precedence) nodes.get(0).getJoins().get(0)).getMaxDistance());
    
    assertEquals(8, ((Precedence) nodes.get(0).getJoins().get(1)).getMinDistance());
    assertEquals(13, ((Precedence) nodes.get(0).getJoins().get(1)).getMaxDistance());
    
    assertEquals(0, ((Precedence) nodes.get(0).getJoins().get(2)).getMinDistance());
    assertEquals(0, ((Precedence) nodes.get(0).getJoins().get(2)).getMaxDistance());
    
    // node 2
    assertEquals(5, ((Precedence) nodes.get(1).getJoins().get(0)).getMinDistance());
    assertEquals(10, ((Precedence) nodes.get(1).getJoins().get(0)).getMaxDistance());
    
    assertEquals(0, ((Precedence) nodes.get(1).getJoins().get(1)).getMinDistance());
    assertEquals(0, ((Precedence) nodes.get(1).getJoins().get(1)).getMaxDistance());
    
    // node 3
    assertEquals(0, ((Precedence) nodes.get(2).getJoins().get(0)).getMinDistance());
    assertEquals(0, ((Precedence) nodes.get(2).getJoins().get(0)).getMaxDistance());
    
    assertEquals(0, ((Precedence) nodes.get(2).getJoins().get(1)).getMinDistance());
    assertEquals(0, ((Precedence) nodes.get(2).getJoins().get(1)).getMaxDistance());
    
    // node 4
    assertEquals(0, ((Precedence) nodes.get(3).getJoins().get(0)).getMinDistance());
    assertEquals(0, ((Precedence) nodes.get(3).getJoins().get(0)).getMaxDistance());
    
    assertEquals(0, ((Precedence) nodes.get(3).getJoins().get(1)).getMinDistance());
    assertEquals(0, ((Precedence) nodes.get(3).getJoins().get(1)).getMaxDistance());
  }
  
  @Test
  public void testFollowSegmentation()
  {
    System.out.println("followSegmentation");
    
    // query to extend
    String aql = "node & node & node & #1 .abc #2 & #2 .abc #3";
    
    // perform the initial parsing
    Start start = parser.parse(aql);
    // optimizer is applied on the fly by the query anaylsis (as injected by Spring)
    QueryData data = queryAnalysis.analyzeQuery(start, new LinkedList<Long>());
    
    assertEquals(1, data.getAlternatives().size());
    List<QueryNode> nodes = data.getAlternatives().get(0);
    
    assertEquals(2, nodes.get(0).getJoins().size());
  }
  
  @Test
  public void testDontFollowSegmentation()
  {
    System.out.println("dontFollowSegmentation");
    
    // query to extend
    String aql = "node & node & node & #1 .def #2 & #2 .abc #3";
    
    // perform the initial parsing
    Start start = parser.parse(aql);
    // optimizer is applied on the fly by the query anaylsis (as injected by Spring)
    QueryData data = queryAnalysis.analyzeQuery(start, new LinkedList<Long>());
    
    assertEquals(1, data.getAlternatives().size());
    List<QueryNode> nodes = data.getAlternatives().get(0);
    
    assertEquals(1, nodes.get(0).getJoins().size());
  }
  
  @Test
  public void testDontFollowSegmentationFromTok()
  {
    System.out.println("dontFollowSegmentationFromTok");
    
    // query to extend
    String aql = "node & node & node & #1 . #2 & #2 .abc #3";
    
    // perform the initial parsing
    Start start = parser.parse(aql);
    // optimizer is applied on the fly by the query anaylsis (as injected by Spring)
    QueryData data = queryAnalysis.analyzeQuery(start, new LinkedList<Long>());
    
    assertEquals(1, data.getAlternatives().size());
    List<QueryNode> nodes = data.getAlternatives().get(0);
    
    assertEquals(1, nodes.get(0).getJoins().size());
  }
}
