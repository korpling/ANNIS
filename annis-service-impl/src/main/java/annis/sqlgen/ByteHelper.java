package annis.sqlgen;

import annis.service.ifaces.AnnisBinary;
import annis.service.objects.AnnisBinaryImpl;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class ByteHelper implements ResultSetExtractor<AnnisBinary>
{

  private String corpusName;

  public String generateSql(String corpusName, int offset, int length)
  {
    this.corpusName = corpusName;

    return "SELECT substring((SELECT file FROM media_files "
      + "WHERE corpus.id = corpus_ref) from " + offset + " for " + length + "),"
      + " bytes, title, mime_type FROM media_files, corpus "
      + "WHERE corpus.name = '" + corpusName + "' AND corpus.id = corpus_ref";
    
  }

  @Override
  public AnnisBinary extractData(ResultSet rs) throws
    DataAccessException
  {
    AnnisBinary ab = new AnnisBinaryImpl();
    try
    {
      while (rs.next())
      {
        {
          ab.setBytes(rs.getBytes("substring"));
          ab.setFileName(rs.getString("title"));
          ab.setCorpusName(corpusName);
          ab.setMimeType(rs.getString("mime_type"));
          ab.setLength(rs.getInt("bytes"));
        }
      }
    }
    catch (SQLException ex)
    {
      Logger.getLogger(ByteHelper.class.getName()).log(Level.SEVERE, null, ex);
    }

    return ab;
  }
}
