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

public class TestAnnisKey
{

  // class under test
  private AnnisKey key = new AnnisKey();
  
  // test data
  @Mock private TableAccessStrategy tableAccessStrategy;
  @Mock private ResultSet resultSet;

  @Before
  public void setup()
  {
    initMocks(this);
  }

  /**
   * ANNOTATE requires columns identifying each node of a matching solution
   * in the inner query.
   */
  @Test
  public void shouldGenerateColumnsForInnerQuery()
  {
    // given
    String idAlias = uniqueString(3);
    String nameAlias = uniqueString(3);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "id")).willReturn(idAlias);
    given(tableAccessStrategy.aliasedColumn(NODE_TABLE, "name")).willReturn(nameAlias);
    int index = uniqueInt(1, 10);
    // when
    List<String> actual = key.generateInnerQueryColumns(tableAccessStrategy, index);
    // then
    List<String> expected = asList(
        idAlias + index + " AS id" + index,
        nameAlias + index + " AS name" + index);
    assertThat(actual, is(expected));
  }

  /**
   * The key for an annotation graph is an array containing the IDs and/or 
   * names of the matched nodes in a solution. 
   */
  @Test
  public void shouldGenerateColumnsForOuterQuery()
  {
    // given
    String idAlias = uniqueString(3);
    String nameAlias = uniqueString(3);
    given(tableAccessStrategy.aliasedColumn("solutions", "id")).willReturn(idAlias);
    given(tableAccessStrategy.aliasedColumn("solutions", "name")).willReturn(nameAlias);
    int size = 3;
    // when
    List<String> actual = key.generateOuterQueryColumns(tableAccessStrategy, size);
    // then
    List<String> expected = asList(
        "ARRAY[" + idAlias + "1" + ", " + idAlias + "2" + ", " + idAlias + "3" + "] AS key",
        "ARRAY[" + nameAlias + "1" + ", " + nameAlias + "2" + ", " + nameAlias + "3" + "] AS key_names");
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
    given(resultSet.getArray("key_names")).willReturn(array);
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
  public void errorIfResultSetThrowsSqlException() throws SQLException
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
   * Signal illegal state if the JDBC array base type is not VARCHAR.
   */
  @Test(expected=IllegalStateException.class)
  public void errorIfVarCharIsNotBaseTypeOfKeyArray() throws SQLException
  {
    // given
    Array array = mock(Array.class);
    given(array.getBaseType()).willReturn(Types.BIGINT);
    given(resultSet.getArray("key_names")).willReturn(array);
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
    given(resultSet.getArray("key_names")).willReturn(array1, array2);
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
    given(resultSet.getArray("key_names")).willReturn(array1, array2);
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
    given(resultSet.getArray("key_names")).willReturn(array);
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
    given(resultSet.getArray("key_names")).willReturn(array);
    // when
    key.retrieveKey(resultSet);
    // then
    assertThat(key.getMatchedNodeIndex(uniqueString()), is(nullValue()));
  }
  
}
