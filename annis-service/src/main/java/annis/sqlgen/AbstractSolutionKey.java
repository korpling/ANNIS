package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbstractSolutionKey<BaseType>
{

  // logging with log4j
  private static final Logger log = LoggerFactory.getLogger(AbstractSolutionKey.class);
  // name of the column identifying a node
  private String idColumnName;
  // the last key
  private List<BaseType> lastKey;
  // the current key
  private List<BaseType> currentKey;

  public List<String> generateInnerQueryColumns(
    TableAccessStrategy tableAccessStrategy, int index)
  {
    List<String> columns = new ArrayList<>();
    columns.add(tableAccessStrategy.aliasedColumn(NODE_TABLE, idColumnName)
      + " AS " + idColumnName + index);
    return columns;
  }

  public boolean isNewKey()
  {
    return currentKey == null || !currentKey.equals(lastKey);
  }

  public Integer getMatchedNodeIndex(Object id)
  {
    int index = getCurrentKey().indexOf(id);
    return index == -1 ? null : index + 1;
  }

  public String getCurrentKeyAsString()
  {
    return StringUtils.join(getCurrentKey(), ",");
  }

  public Object getNodeId(ResultSet resultSet,
    TableAccessStrategy tableAccessStrategy)
  {
    try
    {
      String idAlias = tableAccessStrategy.columnName(NODE_TABLE,
        getIdColumnName());
      int idAliasIdx = resultSet.findColumn(idAlias);
      if (resultSet.getMetaData() != null)
      {
        switch (resultSet.getMetaData().getColumnType(idAliasIdx))
        {
          case Types.VARCHAR:
            return resultSet.getString(idAliasIdx);
          case Types.BIGINT:
            return resultSet.getLong(idAliasIdx);
          case Types.INTEGER:
            return resultSet.getInt(idAliasIdx);
        }
      }
      // default
      return resultSet.getObject(idAliasIdx);
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

  protected List<BaseType> getLastKey()
  {
    return lastKey;
  }

  protected void setLastKey(List<BaseType> lastKey)
  {
    this.lastKey = lastKey;
  }

  protected List<BaseType> getCurrentKey()
  {
    return currentKey;
  }

  protected void setCurrentKey(List<BaseType> currentKey)
  {
    this.currentKey = currentKey;
  }

  public int getKeySize()
  {
    return currentKey.size();
  }
}