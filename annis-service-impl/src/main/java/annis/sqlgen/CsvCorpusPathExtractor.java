package annis.sqlgen;

import static java.util.Arrays.asList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class CsvCorpusPathExtractor implements CorpusPathExtractor
{

  @Override
  public List<String> extractCorpusPath(ResultSet resultSet, String columnName)
      throws SQLException
  {
    String csv = resultSet.getString(columnName);
    String[] pathArray = csv.split(",");
    List<String> path = asList(pathArray);
    Collections.reverse(path);
    return path;
  }

}
