package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static java.util.Arrays.asList;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

//FIXME: key and key_names are hard-coded
public class NodeNameSolutionKey implements SolutionKey<List<String>>
{
  
  // logging with log4j
  private static Logger log = Logger.getLogger(NodeNameSolutionKey.class);
  
  // the last key
  private List<String> lastKey;
  
  // the current key
  private List<String> currentKey;

  @Override
  public List<String> generateInnerQueryColumns(
      TableAccessStrategy tableAccessStrategy, int index)
  {
    List<String> columns = new ArrayList<String>();
    columns.add(tableAccessStrategy.aliasedColumn(NODE_TABLE, "id")
        + " AS " + "id" + index);
    columns.add(tableAccessStrategy.aliasedColumn(NODE_TABLE, "name")
        + " AS " + "name" + index);
    return columns;
  }

  @Override
  public List<String> generateOuterQueryColumns(
      TableAccessStrategy tableAccessStrategy, int size)
  {
    String idAlias = tableAccessStrategy.aliasedColumn("solutions", "id");
    String nameAlias = tableAccessStrategy.aliasedColumn("solutions", "name");
    List<String> columns = new ArrayList<String>();
    columns.add(createKeyArray(idAlias, "key", size));
    columns.add(createKeyArray(nameAlias, "key_names", size));
    return columns;
  }

  // create a key array
  // example: ARRAY[solutions.id1, solutions.id2, solutions.id3] AS key
  private String createKeyArray(String column, String alias, int size)
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
  public List<String> retrieveKey(ResultSet resultSet)
  {
    try
    {
      Array sqlArray = resultSet.getArray("key_names");
      if (resultSet.wasNull()) {
        throw new IllegalStateException("Match group identifier must not be null");
      }
      int baseType = sqlArray.getBaseType();
      String baseTypeName = sqlArray.getBaseTypeName();
      if (baseType != Types.VARCHAR)
      {
        throw new IllegalStateException(
            "Key must be of the type 'varchar' but was: " + baseTypeName);
      }
      String[] keyArray = (String[]) sqlArray.getArray();
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
  public Integer getMatchedNodeIndex(String name)
  {
    int index = currentKey.indexOf(name);
    return index == -1 ? null : index + 1;
  }

  @Override
  public String getCurrentKeyAsString()
  {
    return StringUtils.join(currentKey, ",");
  }
  
}
