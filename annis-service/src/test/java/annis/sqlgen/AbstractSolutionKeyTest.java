package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.test.TestUtils.uniqueInt;
import static annis.test.TestUtils.uniqueString;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.util.Arrays.asList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class AbstractSolutionKeyTest
{

  // class under test
  AbstractSolutionKey<Integer> key = new AbstractSolutionKey<>();
  
  // test data
  @Mock private TableAccessStrategy tableAccessStrategy;
  @Mock private ResultSet resultSet;

  // the column that identifies a node
  private static String idColumnName = uniqueString(3);
  
  @Before
  public void setup()
  {
    initMocks(this);
    key.setIdColumnName(idColumnName);
  }
  
  /**
   * ANNOTATE requires columns identifying each node of a matching solution
   * in the inner query.
   */
  @Test
  public void shouldGenerateColumnsForInnerQuery()
  {
    // given
    String nameAlias = uniqueString(3);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, idColumnName)).willReturn(nameAlias);
    int index = uniqueInt(1, 10);
    // when
    List<String> actual = key.generateInnerQueryColumns(tableAccessStrategy, index);
    // then
    List<String> expected = asList(nameAlias + " AS " + idColumnName + index);
    assertThat(actual, is(expected));
  }
  
  /**
   * The node ID is the value of the ID column of aliased for the outer query.
   */
  @Test
  public void shouldReturnTheIdOfTheNode() throws SQLException
  {
    // given
    Object expected = new Object();
    String idAlias = uniqueString(3);
    given(tableAccessStrategy.columnName(NODE_TABLE, idColumnName)).willReturn(idAlias);
    given(resultSet.getObject(1)).willReturn(expected);
    given(resultSet.findColumn(idAlias)).willReturn(1);
    given(resultSet.getObject(idAlias)).willReturn(expected);
    
    // when
    Object actual = key.getNodeId(resultSet, tableAccessStrategy);
    // then
    assertThat(actual, is(expected));
  }
  
  /**
   * Signal illegal state if there is an SQL error.
   */
  @Test(expected=IllegalStateException.class)
  public void errorIfResultSetThrowsSqlExceptionInGetNodeId() throws SQLException
  {
    // given
    given(resultSet.getObject(anyString())).willThrow(new SQLException());
    given(resultSet.getObject(anyInt())).willThrow(new SQLException());
    // when
    key.getNodeId(resultSet, tableAccessStrategy);
  }
  
}
