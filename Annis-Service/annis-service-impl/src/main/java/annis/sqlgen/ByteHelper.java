package annis.sqlgen;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

public class ByteHelper implements ResultSetExtractor<byte[]>
{

  public String generateSql(long corpusId)
  {
    return "SELECT file FROM media_files WHERE corpus_ref=" + corpusId + ";";
  }

  public byte[] getBytes(long corpusId, JdbcTemplate jdcbTemplate)
  {
    return (byte[]) jdcbTemplate.query(generateSql(corpusId), this);
  }

  // TODO return only the last value
  @Override
  public byte[] extractData(ResultSet rs) throws SQLException,
      DataAccessException
  {
    InputStream stream = null;
    byte[] bytes = new byte[1024];

    while (rs.next())
      stream = rs.getBinaryStream("file");
    try
    {
      stream.read(bytes, 0, bytes.length);
    } catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return bytes;
  }
}
