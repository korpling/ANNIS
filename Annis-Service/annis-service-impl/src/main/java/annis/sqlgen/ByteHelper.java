package annis.sqlgen;

import annis.service.ifaces.AnnisBinary;
import annis.service.objects.AnnisBinaryImpl;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class ByteHelper implements ResultSetExtractor<AnnisBinary>
{

  private long corpusId;

  public String generateSql(long corpusId, int offset, int length)
  {
    this.corpusId = corpusId;

    return "SELECT (substring((SELECT file FROM media_files WHERE corpus_ref="
            + corpusId + ") from " + offset + " for " + length + ""
            + ")), bytes, mime_type, title FROM media_files WHERE corpus_ref="
            + corpusId;
  }

  @Override
  public AnnisBinary extractData(ResultSet rs) throws SQLException,
          DataAccessException
  {
    AnnisBinary ab = new AnnisBinaryImpl();

    while (rs.next())
    {
      {
        ab.setBytes(rs.getBytes("substring"));
        ab.setFileName(rs.getString("title"));
        ab.setId(corpusId);
        ab.setMimeType(rs.getString("mime_type"));
        ab.setLength(rs.getInt("bytes"));        
      }
    }

    return ab;
  }
}
