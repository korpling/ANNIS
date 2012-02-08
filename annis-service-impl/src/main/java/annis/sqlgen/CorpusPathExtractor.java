package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface CorpusPathExtractor
{
  
  /**
   * Extracts the path of the document in the corpus tree
   * from a JDBC result set, starting from the root corpus.
   * 
   * Note: The path is stored in reverse order in the database.
   * 
   * FIXME: Why is the order in the database reversed? Because
   * the ANNOTATE query used to ask for path_name[0] AS document_name?
   * That code appears to be unused.
   * 
   * @param resultSet The JDBC result set.
   * @param columnName TODO
   */
  public List<String> extractCorpusPath(ResultSet resultSet, String columnName) throws SQLException;

}
