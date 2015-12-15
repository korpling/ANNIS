package annis.sqlgen;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * TODO Document semantics of of multiple ID columns TODO Code supporting
 * multiple ID columns complicates things unnecessarily
 *
 * @param <BaseType>
 */
public class PostgreSqlArraySolutionKey<BaseType> extends AbstractSolutionKey<BaseType>
  implements SolutionKey<List<BaseType>>
{

  // logging with log4j
  private static final Logger log = LoggerFactory.getLogger(PostgreSqlArraySolutionKey.class);
  // the name of the ID array in the outer query
  private String keyColumnName;

  @Override
  public List<String> generateOuterQueryColumns(
    TableAccessStrategy tableAccessStrategy, int size)
  {
    List<String> columns = new ArrayList<>();
    String nameAlias = tableAccessStrategy.aliasedColumn("solutions",
      getIdColumnName());
    columns.add(createKeyArray(nameAlias, keyColumnName, size));
    return columns;
  }

  protected String createKeyArray(String column, String alias, int size)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("ARRAY[");
    sb.append(column);
    sb.append(1);
    for (int i = 2; i <= size; ++i)
    {
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
      if (resultSet.wasNull())
      {
        throw new IllegalStateException(
          "Match group identifier must not be null");
      }
      @SuppressWarnings("unchecked")
      BaseType[] keyArray = (BaseType[]) sqlArray.getArray();
      setLastKey(getCurrentKey());
      setCurrentKey(asList(keyArray));
      return getCurrentKey();
    }
    catch (SQLException e)
    {
      log.error("Exception thrown while retrieving key array", e);
      throw new IllegalStateException(
        "Could not retrieve key from JDBC results set", e);
    }
  }

  @Override
  public List<String> getKeyColumns(int size)
  {
    return asList(keyColumnName);
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