package annis.sqlgen;

import annis.service.objects.AnnisBinary;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class ByteHelper implements ResultSetExtractor<AnnisBinary>
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ByteHelper.class);
  
  private static final int[] ARG_TYPES = new int [] {
    Types.INTEGER, Types.INTEGER,
    Types.VARCHAR, Types.VARCHAR, Types.VARCHAR
  };
  

  public static final String SQL =
        "SELECT\n"
      + "  substring(file from ? for ?) AS partfile,\n"
      + "  bytes, title, mime_type, sub.name as corpus_name\n"
      + "FROM media_files, corpus AS sub, corpus AS top \n"
      + "WHERE\n"
      + "  top.top_level = true AND\n"
      + "  top.name = ? AND\n"
      + "  sub.name = ? AND\n"
      + "  sub.pre >= top.pre AND sub.post <= top.post AND\n"
      + "  sub.id = corpus_ref AND\n"
      + "  mime_type = ?";
  ;
  
  public static int[] getArgTypes()
  {
    return Arrays.copyOf(ARG_TYPES, ARG_TYPES.length);
  }
  
  public Object[] getArgs(String toplevelCorpusName, String corpusName, 
    String mimeType, int offset, int length)
  {
    return new Object[] 
    {
      offset, length, toplevelCorpusName, corpusName, mimeType
    }; 

  }

  @Override
  public AnnisBinary extractData(ResultSet rs) throws
    DataAccessException
  {
    AnnisBinary ab = new AnnisBinary();
    try
    {
      while (rs.next())
      {
        ab.setBytes(rs.getBytes("partfile"));
        ab.setFileName(rs.getString("title"));
        ab.setCorpusName(rs.getString("corpus_name"));
        ab.setMimeType(rs.getString("mime_type"));
        ab.setLength(rs.getInt("bytes"));
        // we only give one matching result back
        break;
      }
    }
    catch (SQLException ex)
    {
      log.error(null, ex);
    }

    return ab;
  }

}
