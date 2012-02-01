package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.test.TestUtils.uniqueInt;
import static annis.test.TestUtils.uniqueString;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class PostgreSqlArraySolutionKeyTest
{

  // class under test
  private PostgreSqlArraySolutionKey<String> key = new PostgreSqlArraySolutionKey<String>();
  
  // test data
  @Mock private TableAccessStrategy tableAccessStrategy;
  @Mock private ResultSet resultSet;
  
  // the column that identifies a node
  private static String idColumnName = uniqueString(3);
  
  // the name of the key column in the outer query
  private static String keyColumnName = uniqueString(3);
  
  @Before
  public void setup()
  {
    initMocks(this);
    key.setIdColumnName(idColumnName);
    key.setKeyColumnName(keyColumnName);
  }
  
  /**
   * ANNOTATE requires columns identifying each node of a matching solution
   * in the inner query.
   */
  @Test
  public void shouldGenerateColumnsForInnerQuerySingleIdColumn()
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
   * The key for an annotation graph is an array containing the IDs and/or 
   * names of the matched nodes in a solution. 
   */
  @Test
  public void shouldGenerateColumnsForOuterQuerySingleIdColumn()
  {
    // given
    String nameAlias = uniqueString(3);
    given(tableAccessStrategy.aliasedColumn("solutions", idColumnName)).willReturn(nameAlias);
    int size = 3;
    // when
    List<String> actual = key.generateOuterQueryColumns(tableAccessStrategy, size);
    // then
    List<String> expected = asList(
        "ARRAY[" + nameAlias + "1" + ", " + nameAlias + "2" + ", " + nameAlias + "3" + "] AS " + keyColumnName);
    assertThat(actual, is(expected));    
  }
  
  /**
   * The key should be retrieved (and validated) from the JDBC result set.
   */
  @Test
  public void shouldRetreiveKeyFromResultSetAndValidateIt() throws SQLException
  {
    // given
    String key1 = uniqueString(3);
    String key2 = uniqueString(3);
    String key3 = uniqueString(3);
    Array array = createKeyJdbcArray(key1, key2, key3);
    given(resultSet.getArray(keyColumnName)).willReturn(array);
    // when
    List<String> actual = key.retrieveKey(resultSet);
    // then
    List<String> expected = asList(key1, key2, key3);
    assertThat(actual, is(expected));
  }

  // create a JDBC array from an array of strings
  private Array createKeyJdbcArray(String... keys) throws SQLException
  {
    Array array = mock(Array.class); 
    given(array.getBaseType()).willReturn(Types.VARCHAR);
    given(array.getArray()).willReturn(keys);
    return array;
  }
  
  /**
   * Signal illegal state if there is an SQL error.
   */
  @Test(expected=IllegalStateException.class)
  public void errorIfResultSetThrowsSqlExceptionInRetrieveKey() throws SQLException
  {
    // given
    given(resultSet.getArray(anyString())).willThrow(new SQLException());
    // when
    key.retrieveKey(resultSet);
  }
  
  /**
   * Signal illegal state if the key is NULL.
   */
  @Test(expected=IllegalStateException.class)
  public void errorIfKeyIsNull() throws SQLException
  {
    // given
    given(resultSet.wasNull()).willReturn(true);
    // when
    key.retrieveKey(resultSet);
  }
  
  /**
   * Signal if the key changes from one row to the next.
   */
  @Test
  public void shouldSignalNewKey() throws SQLException
  {
    // given
    String key1 = uniqueString(3);
    String key2 = uniqueString(3);
    Array array1 = createKeyJdbcArray(key1);
    Array array2 = createKeyJdbcArray(key2);
    given(resultSet.getArray(keyColumnName)).willReturn(array1, array2);
    // when
    key.retrieveKey(resultSet);
    resultSet.next();
    key.retrieveKey(resultSet);
    // then
    assertThat(key.isNewKey(), is(true));
  }
  
  /**
   * Don't signal new key if the same key is used in two consecutive rows.
   */
  @Test
  public void shouldRecognizeOldKey() throws SQLException
  {
    // given
    String key1 = uniqueString(3);
    Array array1 = createKeyJdbcArray(key1);
    Array array2 = createKeyJdbcArray(key1);
    given(resultSet.getArray(keyColumnName)).willReturn(array1, array2);
    // when
    key.retrieveKey(resultSet);
    resultSet.next();
    key.retrieveKey(resultSet);
    // then
    assertThat(key.isNewKey(), is(false));
  }
  
  /**
   * A node is a match of a search term in the query if its name is part of 
   * the key.
   */
  @Test
  public void shouldSignalMatchedNodes() throws SQLException
  {
    // given
    String[] keys = { uniqueString(3), uniqueString(3), uniqueString(3) };
    Array array = createKeyJdbcArray(keys);
    given(resultSet.getArray(keyColumnName)).willReturn(array);
    // when
    key.retrieveKey(resultSet);
    // then
    for (int i = 0; i < keys.length; ++i) {
      assertThat(key.getMatchedNodeIndex(keys[i]), is(i + 1));
    }
  }
  
  /**
   * Return {@code null} for unmatched nodes.
   */
  @Test
  public void shouldReturnNullForUnmatchedNodes() throws SQLException
  {
    // given
    Array array = createKeyJdbcArray(uniqueString());
    given(resultSet.getArray(keyColumnName)).willReturn(array);
    // when
    key.retrieveKey(resultSet);
    // then
    assertThat(key.getMatchedNodeIndex(uniqueString()), is(nullValue()));
  }
  
  /**
   * The string representation is the node names concatenated with ","
   */
  @Test
  public void shouldCreateStringRepresentationOfKey() throws SQLException
  {
    // given
    String key1 = uniqueString(3);
    String key2 = uniqueString(3);
    String key3 = uniqueString(3);
    Array array = createKeyJdbcArray(key1, key2, key3);
    given(resultSet.getArray(keyColumnName)).willReturn(array);
    // when
    key.retrieveKey(resultSet);
    String actual = key.getCurrentKeyAsString();
    // then
    String expected = key1 + "," + key2 + "," + key3;
    assertThat(actual, is(expected));
  }
  
  /**
   * Return the name of the key array as key column.
   */
  @Test
  public void shouldReturnKeyArrayNameAsKeyColumn()
  {
    // when
    List<String> keyColumns = key.getKeyColumns();
    // then
    assertThat(keyColumns, is(asList(keyColumnName)));
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
    // when
    key.getNodeId(resultSet, tableAccessStrategy);
  }
}
