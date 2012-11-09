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
