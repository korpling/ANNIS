package annis.sqlgen;

import annis.service.ifaces.AnnisBinary;
import annis.service.objects.AnnisBinaryImpl;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class ByteHelper implements ResultSetExtractor<AnnisBinary>
{
  private int offset;
  private int length;
  private long corpusId;

  public String generateSql(long corpusId, int offset, int length)
  {
    this.offset = offset;
    this.length = length;
    this.corpusId = corpusId;

    return "SELECT (substring((SELECT file FROM media_files WHERE corpus_ref=" +
            corpusId + ") from " + offset + " for " + (offset + length - 1) + ""
            + ")), bytes, mime_type, title FROM media_files WHERE corpus_ref=" +
            corpusId;
  }

  @Override
  public AnnisBinary extractData(ResultSet rs) throws SQLException,
          DataAccessException
  {
    AnnisBinary ab = new AnnisBinaryImpl();
    byte[] bytes = new byte[length];    

    while (rs.next())
    {
      {
        try
        {
          rs.getBinaryStream("substring").read(bytes);  
          ab.setBytes(bytes);
          ab.setFileName(rs.getString("title"));
          ab.setId(corpusId);
          ab.setMimeType(rs.getString("mime_type"));
          ab.setLength(rs.getInt("bytes"));
        } catch (IOException ex)
        {
          Logger.getLogger(ByteHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
      } 
    }

    return ab;
  }

  /**
   * @return the offset
   */
  public int getOffset()
  {
    return offset;
  }

  /**
   * @param offset the offset to set
   */
  public void setOffset(int offset)
  {
    this.offset = offset;
  }

  /**
   * @return the length
   */
  public int getLength()
  {
    return length;
  }

  /**
   * @param length the length to set
   */
  public void setLength(int length)
  {
    this.length = length;
  }
}
