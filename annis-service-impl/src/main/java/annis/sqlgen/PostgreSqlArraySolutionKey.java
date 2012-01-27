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

public class PostgreSqlArraySolutionKey<BaseType> 
  implements SolutionKey<List<BaseType>>
{

  // logging with log4j
  private static Logger log = Logger.getLogger(NodeNamePostgreSqlArraySolutionKey.class);
  
  // the last key
  private List<BaseType> lastKey;
  
  // the current key
  private List<BaseType> currentKey;
  
  // the column that is used to identify a node
  private String idColumn;
  
  // the name of the column that is used for the key in the outer query
  private String keyColumn;
  
  // the SQL type of the column that is used to identify a node
  private int idSqlType;

  public PostgreSqlArraySolutionKey(String idColumn, String keyColumn, int idSqlType)
  {
    this.idColumn = idColumn;
    this.keyColumn = keyColumn;
    this.idSqlType = idSqlType;
  }

  @Override
  public List<String> generateInnerQueryColumns(TableAccessStrategy tableAccessStrategy, int index)
  {
    List<String> columns = new ArrayList<String>();
    columns.add(tableAccessStrategy.aliasedColumn(NODE_TABLE, idColumn)
        + " AS " + idColumn + index);
    return columns;
  }

  @Override
  public List<String> generateOuterQueryColumns(TableAccessStrategy tableAccessStrategy, int size)
  {
    String nameAlias = tableAccessStrategy.aliasedColumn("solutions", idColumn);
    List<String> columns = new ArrayList<String>();
    columns.add(createKeyArray(nameAlias, keyColumn, size));
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
      Array sqlArray = resultSet.getArray(keyColumn);
      if (resultSet.wasNull()) {
        throw new IllegalStateException("Match group identifier must not be null");
      }
      int baseType = sqlArray.getBaseType();
      String baseTypeName = sqlArray.getBaseTypeName();
      if (baseType != idSqlType)
      {
        throw new IllegalStateException("Wrong key column type: " + baseTypeName);
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
  public String getKeyColumnName()
  {
    return keyColumn;
  }

}