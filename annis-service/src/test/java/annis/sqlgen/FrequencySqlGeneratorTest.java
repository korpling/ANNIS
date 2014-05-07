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
import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import annis.sqlgen.annopool.ApFrequencySqlGenerator;
import annis.sqlgen.annotext.AtFrequencySqlGenerator;
import annis.sqlgen.extensions.FrequencyTableQueryData;
import static annis.test.TestUtils.size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import static org.hamcrest.Matchers.is;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencySqlGeneratorTest
{
  
  private FrequencySqlGenerator generator = new ApFrequencySqlGenerator();
  
  @Mock private SqlGenerator<QueryData, ?> innerSqlGenerator = mock(SqlGenerator.class);
  @Mock private QueryData queryData;
  @Mock private FrequencyTableQueryData freqTableQueryData;
  @Mock private QueryNode queryNode;
  private List<QueryNode> alternative = new ArrayList<QueryNode>();
  
  public FrequencySqlGeneratorTest()
  {
    initMocks(this);
    
    generator.setInnerQuerySqlGenerator(innerSqlGenerator);
    
    given(queryData.getExtensions()).willReturn(new HashSet(Arrays.asList(freqTableQueryData)));
    given(innerSqlGenerator.toSql(any(QueryData.class), anyString())).willReturn("<innerquery>");
    
    alternative.add(queryNode);
    given(queryData.getMaxWidth()).willReturn(3);
    
  }
  
  
  @Before
  public void setUp()
  {
    FrequencyTableEntry e1 = new FrequencyTableEntry();
    e1.setType(FrequencyTableEntryType.span);
    
    FrequencyTableEntry e2 = new FrequencyTableEntry();
    e2.setType(FrequencyTableEntryType.annotation);
    e2.setKey("lemma");
    
    FrequencyTableEntry e3 = new FrequencyTableEntry();
    e3.setType(FrequencyTableEntryType.span);
    
    freqTableQueryData.add(e1);
    freqTableQueryData.add(e2);
    freqTableQueryData.add(e3);
  }
  
  @After
  public void tearDown()
  {
  }

  /**
   * Test of whereConditions method, of class FrequencySqlGenerator.
   */
  @Test
  @Ignore
  public void testWhereConditions()
  {
    System.out.println("whereConditions");
    
    Set<String> expected = new TreeSet<String>();
    expected.add("hello world");
    
    Set<String> actual = generator.whereConditions(queryData, alternative, "");
    
    for (String item : expected)
    {
      assertThat(actual, hasItem(item));
    }
    assertThat(actual, is(size(expected.size())));
    
    
  }

  /**
   * Test of selectClause method, of class FrequencySqlGenerator.
   */
  @Test
  public void testSelectClause()
  {
    System.out.println("selectClause");
  }

  /**
   * Test of groupByAttributes method, of class FrequencySqlGenerator.
   */
  @Test
  public void testGroupByAttributes()
  {
    System.out.println("groupByAttributes");
  }

  /**
   * Test of fromClause method, of class FrequencySqlGenerator.
   */
  @Test
  @Ignore
  public void testFromClause()
  {
    System.out.println("fromClause");
    
    String expected = "(<innerquery>) AS solutions,\n"
      + "facts AS v1,\n"
      + "facts AS v2, annotation_pool AS a2,"
      + "facts AS v3";
    
    String actual = generator.fromClause(queryData, alternative, "");
    
    assertEquals(expected, actual);
    
  }
  

}
