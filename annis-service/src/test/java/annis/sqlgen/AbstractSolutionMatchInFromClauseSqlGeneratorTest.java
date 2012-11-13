/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import static annis.test.TestUtils.uniqueString;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;

import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;

public class AbstractSolutionMatchInFromClauseSqlGeneratorTest
{

  // class under test
  @InjectMocks private AbstractSolutionMatchInFromClauseSqlGenerator<?> generator = new AbstractSolutionMatchInFromClauseSqlGenerator<Object>()
  {

    @Override
    public Object extractData(ResultSet arg0) throws SQLException,
        DataAccessException
    {
      throw new UnsupportedOperationException("BUG: This is a test case support class");
    }
  };
  
  // dependencies
  @Mock private SqlGenerator<QueryData, ?> findSqlGenerator;
  
  // test data
  @Mock private QueryData queryData;
  @Mock private List<QueryNode> alternative;
  
  @Before
  public void setup()
  {
    initMocks(this);
  }
  
  @Test
  public void shouldQueryForMatchesInFromClause()
  {
    // given
    String innerSql = uniqueString();
    given(findSqlGenerator.toSql(eq(queryData), anyString())).willReturn(innerSql);
    // when
    String actual = generator.fromClause(queryData, alternative, TABSTOP);
    // then
    String expected = "( " + innerSql + " ) AS solutions";
    assertThat(actual, equalToIgnoringWhiteSpace(expected));
  }
  
}
