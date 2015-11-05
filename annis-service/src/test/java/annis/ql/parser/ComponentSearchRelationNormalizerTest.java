/*
 * Copyright 2015 SFB 632.
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

import annis.AnnisXmlContextLoader;
import annis.model.QueryNode;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
  "file:src/main/distribution/conf/spring/Common.xml",
  "file:src/main/distribution/conf/spring/Dao.xml",
  "classpath:annis/ql/parser/AnnisParser-context.xml", 
  "classpath:annis/AutowiredContext.xml"},
  loader = AnnisXmlContextLoader.class)
public class ComponentSearchRelationNormalizerTest
{
 private boolean postProcessorExists = false;

 	@Autowired private AnnisParserAntlr parser;
  
  public ComponentSearchRelationNormalizerTest()
  {
    parser = new AnnisParserAntlr();
  }

  @Before
  public void setUp()
  {
    parser.setPrecedenceBound(0);
    postProcessorExists = false;
    for(QueryDataTransformer t : parser.getPostProcessors())
    {
      if(t instanceof ComponentSearchRelationNormalizer)
      {
        postProcessorExists = true;
        break;
      }
    }
  }

  @After
  public void tearDown()
  {
  } 
  
  @Test
  public void testUniqueNameForDoubleNormalized()
  {
    Assume.assumeTrue(postProcessorExists);
    
    String aql = "cat=/(S|.P)/ >[func=/M(O|NR)/] cat=\"PP\" & #2 >[func=\"AC\"] lemma=\"an\" & #3 _=_ pos=/APPR(ART)?/ & #2 >[func=\"NK\"] pos=\"NN\"";
    
    
    // perform the initial parsing
    QueryData data = parser.parse(aql, new LinkedList<Long>());
    Assert.assertEquals(1, data.getAlternatives().size());
    List<QueryNode> alt = data.getAlternatives().get(0);
    Assert.assertEquals(7, alt.size());
    
    Assert.assertEquals(6, alt.get(5).getId());
    Assert.assertEquals("x6(2)", alt.get(5).getVariable());
    
    Assert.assertEquals(7, alt.get(6).getId());
    Assert.assertEquals("x7(2)", alt.get(6).getVariable());
    
    
  }
}
