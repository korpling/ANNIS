package annis.sqlgen;

import static java.util.Arrays.asList;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class ArrayCorpusPathExtractor implements CorpusPathExtractor
{

  @Override
  public List<String> extractCorpusPath(ResultSet resultSet, String columnName) throws SQLException
  {
    Array jdbcArray = resultSet.getArray(columnName);
    String[] pathArray = (String[]) jdbcArray.getArray();
    List<String> path = asList(pathArray);
    Collections.reverse(path);
    return path;
  }
  
}
