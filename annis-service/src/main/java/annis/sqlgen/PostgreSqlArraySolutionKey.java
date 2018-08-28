package annis.sqlgen;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import static java.util.Arrays.asList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annis.model.QueryNode;

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
    TableAccessStrategy tableAccessStrategy, List<QueryNode> alternative)
  {
    List<String> columns = new ArrayList<>();
    String nameAlias = tableAccessStrategy.aliasedColumn("solutions",
      getIdColumnName());
    columns.add(createKeyArray(nameAlias, keyColumnName, alternative));
    return columns;
  }

  protected String createKeyArray(String column, String alias, List<QueryNode> alternative)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("ARRAY[");
    int i = 1;
    for (Iterator<QueryNode> node_it = alternative.iterator(); node_it.hasNext();) {
      sb.append(column);

      QueryNode n = node_it.next();
      if (n.hasCustomName()) {
        sb.append(n.getVariable());
      }
      else {
        sb.append(i);
        i++;
      }

      if (node_it.hasNext()) {
        sb.append(", ");
      }
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