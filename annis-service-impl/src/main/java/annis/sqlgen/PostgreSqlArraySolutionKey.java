package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static java.util.Arrays.asList;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * TODO Document semantics of of multiple ID columns
 * TODO Code supporting multiple ID columns complicates things unnecessarily
 *
 * @param <BaseType>
 */
public class PostgreSqlArraySolutionKey<BaseType> 
  implements SolutionKey<List<BaseType>>
{

  // logging with log4j
  private static Logger log = Logger.getLogger(PostgreSqlArraySolutionKey.class);
  
  // the last key
  private List<BaseType> lastKey;
  
  // the current key
  private List<BaseType> currentKey;
  
  // the column name identifying a node
  private String idColumnName;
  
  // the name of the ID array in the outer query
  private String keyColumnName;
  
  @Override
  public List<String> generateInnerQueryColumns(TableAccessStrategy tableAccessStrategy, int index)
  {
    List<String> columns = new ArrayList<String>();
    columns.add(tableAccessStrategy.aliasedColumn(NODE_TABLE, idColumnName)
        + " AS " + idColumnName + index);
    return columns;
  }

  @Override
  public List<String> generateOuterQueryColumns(TableAccessStrategy tableAccessStrategy, int size)
  {
    List<String> columns = new ArrayList<String>();
    String nameAlias = tableAccessStrategy.aliasedColumn("solutions", idColumnName);
    columns.add(createKeyArray(nameAlias, keyColumnName, size));
    return columns;
  }

  protected String createKeyArray(String column, String alias, int size)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("ARRAY[");
    sb.append(column);
    sb.append(1);
    for (int i = 2; i <= size; ++i) {
      sb.append(", ");
      sb.append(column);
      sb.append(i);
    }
    sb.append("]");
    sb.append(" AS ");
    sb.append(alias);
    String s = sb.toString();
    return s;
  }

  @Override
  public List<BaseType> retrieveKey(ResultSet resultSet)
  {
    try
    {
      Array sqlArray = resultSet.getArray(keyColumnName);
      if (resultSet.wasNull()) {
        throw new IllegalStateException("Match group identifier must not be null");
      }
      @SuppressWarnings("unchecked")
      BaseType[] keyArray = (BaseType[]) sqlArray.getArray();
      lastKey = currentKey;
      currentKey = asList(keyArray);
      return currentKey;
    } catch (SQLException e)
    {
      log.error("Exception thrown while retrieving key array", e);
      throw new IllegalStateException(
          "Could not retrieve key from JDBC results set", e);
    }
  }

  @Override
  public boolean isNewKey()
  {
    return currentKey == null || ! currentKey.equals(lastKey);
  }

  @Override
  public Integer getMatchedNodeIndex(Object id)
  {
    int index = currentKey.indexOf(id);
    return index == -1 ? null : index + 1;
  }

  @Override
  public String getCurrentKeyAsString()
  {
    return StringUtils.join(currentKey, ",");
  }

  @Override
  public List<String> getKeyColumns()
  {
    return asList(keyColumnName);
  }

  @Override
  public Object getNodeId(ResultSet resultSet, TableAccessStrategy tableAccessStrategy)
  {
    try {
      String idAlias = tableAccessStrategy.columnName(NODE_TABLE, idColumnName);
      Object nodeId = resultSet.getObject(idAlias);
      return nodeId;
    }
    catch (SQLException e)
    {
      log.error("Exception thrown while retrieving node ID", e);
      throw new IllegalStateException(
          "Could not retrieve node ID from JDBC results set", e);
    }
  }
  
  public String getIdColumnName()
  {
    return idColumnName;
  }

  public void setIdColumnName(String idColumnName)
  {
    this.idColumnName = idColumnName;
  }

  public String getKeyColumnName()
  {
    return keyColumnName;
  }

  public void setKeyColumnName(String keyColumnName)
  {
    this.keyColumnName = keyColumnName;
  }

}