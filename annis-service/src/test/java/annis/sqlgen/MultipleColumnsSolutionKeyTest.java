package annis.sqlgen;

import static annis.test.TestUtils.uniqueInt;
import static annis.test.TestUtils.uniqueString;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class MultipleColumnsSolutionKeyTest
{
  
  // class under test
  private MultipleColumnsSolutionKey<Integer> key = new MultipleColumnsSolutionKey<Integer>();

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
   * The key for an annotation graph is are the ID columns
   * of the matched nodes in a solution. 
   */
  @Test
  public void shouldGenerateColumnsForOuterQuery()
  {
    // given
    String nameAlias = uniqueString(3);
    given(tableAccessStrategy.aliasedColumn("solutions", idColumnName)).willReturn(nameAlias);
    int size = 3;
    // when
    List<String> actual = key.generateOuterQueryColumns(tableAccessStrategy, size);
    // then
    List<String> expected = asList(
        nameAlias + "1" + " AS " + keyColumnName + "1",
        nameAlias + "2" + " AS " + keyColumnName + "2",
        nameAlias + "3" + " AS " + keyColumnName + "3");
    assertThat(actual, is(expected));    
  }
  
  /**
   * The key should be assembled from the individual key columns. 
   * The last ID column is reached when retrieving the next column
   * throws an exception.
   */
  @Test
  public void shouldRetreiveKeyFromResultSetAndValidateIt() throws SQLException
  {
    // given
    int key1 = uniqueInt(3);
    int key2 = uniqueInt(3);
    int key3 = uniqueInt(3);
    given(resultSet.getObject(keyColumnName + 1)).willReturn(key1);
    given(resultSet.getObject(keyColumnName + 2)).willReturn(key2);
    given(resultSet.getObject(keyColumnName + 3)).willReturn(key3);
    given(resultSet.getObject(keyColumnName + 4)).willThrow(new SQLException());
    // when
    List<Integer> actual = key.retrieveKey(resultSet);
    // then
    List<Integer> expected = asList(key1, key2, key3);
    assertThat(actual, is(expected));
  }

  /**
   * Signal illegal state if there is an SQL error while retrieving 
   * the first ID column.
   */
  @Test(expected=IllegalStateException.class)
  public void errorIfResultSetThrowsSqlExceptionInRetrieveKey() throws SQLException
  {
    // given
    given(resultSet.getObject(anyString())).willThrow(new SQLException());
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
    int key1 = uniqueInt(1, 3);
    int key2 = uniqueInt(4, 6);
    given(resultSet.getObject(keyColumnName + 1)).willReturn(key1, key2);
    given(resultSet.getObject(keyColumnName + 2)).willThrow(new SQLException());
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
    given(resultSet.getObject(keyColumnName + 1)).willReturn(key1, key1);
    given(resultSet.getObject(keyColumnName + 2)).willThrow(new SQLException());
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
    int[] keys = { uniqueInt(1, 3), uniqueInt(4, 6), uniqueInt(7, 9) };
    given(resultSet.getObject(keyColumnName + 1)).willReturn(keys[0]);
    given(resultSet.getObject(keyColumnName + 2)).willReturn(keys[1]);
    given(resultSet.getObject(keyColumnName + 3)).willReturn(keys[2]);
    given(resultSet.getObject(keyColumnName + 4)).willThrow(new SQLException());
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
    int key1 = uniqueInt(1, 3);
    given(resultSet.getObject(keyColumnName + 1)).willReturn(key1);
    given(resultSet.getObject(keyColumnName + 2)).willThrow(new SQLException());
    // when
    key.retrieveKey(resultSet);
    // then
    assertThat(key.getMatchedNodeIndex(uniqueInt(4, 6)), is(nullValue()));
  }
  
  /**
   * The string representation is the node names concatenated with ","
   */
  @Test
  public void shouldCreateStringRepresentationOfKey() throws SQLException
  {
    // given
    int key1 = uniqueInt(3);
    int key2 = uniqueInt(3);
    int key3 = uniqueInt(3);
    given(resultSet.getObject(keyColumnName + 1)).willReturn(key1);
    given(resultSet.getObject(keyColumnName + 2)).willReturn(key2);
    given(resultSet.getObject(keyColumnName + 3)).willReturn(key3);
    given(resultSet.getObject(keyColumnName + 4)).willThrow(new SQLException());
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
    int size = 3;
    List<String> keyColumns = key.getKeyColumns(size);
    // then
    assertThat(keyColumns, is(
        asList(keyColumnName + 1, keyColumnName + 2, keyColumnName + 3)));
  }
  
}