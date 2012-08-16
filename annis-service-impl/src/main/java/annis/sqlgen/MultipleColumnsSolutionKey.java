package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class MultipleColumnsSolutionKey<BaseType>
  extends AbstractSolutionKey<BaseType>
  implements SolutionKey<List<BaseType>>
{

  private static final Logger log = LoggerFactory.getLogger(MultipleColumnsSolutionKey.class);
  
  // the name of the ID array in the outer query
  private String keyColumnName;
  
  @Override
  public List<String> generateOuterQueryColumns(
      TableAccessStrategy tableAccessStrategy, int size)
  {
    List<String> columns = new ArrayList<String>();
    String nameAlias = tableAccessStrategy.aliasedColumn("solutions", getIdColumnName());
    for (int i = 1; i <= size; ++i) {
      columns.add(nameAlias + i + " AS " + keyColumnName + i);
    }
    return columns;
  }

  // FIXME: counting number of IDs in key by waiting for an exception is ugly
  // it would be better to store the number of IDs in a key somewhere
  // unfortunately we can't pass the to AnnotateSqlGenerator.extractData()
  // however, we could pass them in the query
  @Override
  public List<BaseType> retrieveKey(ResultSet resultSet)
  {
    setLastKey(getCurrentKey());
    List<BaseType> currentKey = new ArrayList<BaseType>();
    setCurrentKey(currentKey);
    SQLException sqlException = null;
    int index = 1;
    boolean hasMoreKeyColumns = true;
    do {
      try {
        @SuppressWarnings("unchecked")
        BaseType keyPart = (BaseType) resultSet.getObject(keyColumnName + index);
        currentKey.add(keyPart);
        ++index;
      }
      catch (SQLException e)
      {
        sqlException = e;
        hasMoreKeyColumns = false;
      }
    } while (hasMoreKeyColumns);
    
    if (index == 1)
    {
      log.error("Exception thrown while retrieving key column with index 1", sqlException);
      throw new IllegalStateException(
          "Could not retrieve key from JDBC results set", sqlException);
    }
    
    return currentKey;
  }
  
  @Override
  public List<String> getKeyColumns(int size)
  {
    List<String> keyColumns = new ArrayList<String>();
    for (int i = 1; i <= size; ++i)
    {
      keyColumns.add(keyColumnName + i);
    }
    return keyColumns;
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
