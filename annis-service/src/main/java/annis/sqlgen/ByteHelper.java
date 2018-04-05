package annis.sqlgen;

import annis.service.objects.AnnisBinaryMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class ByteHelper implements ResultSetExtractor<AnnisBinaryMetaData>
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ByteHelper.class);
  
  private static final int[] ARG_TYPES = new int [] {
    Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
    Types.VARCHAR, Types.VARCHAR
  };
  

  public static final String SQL =
      "SELECT\n"
    + "filename, title, mime_type\n"
    + "FROM media_files\n"
    + "WHERE\n"
    + "  corpus_path = ? AND\n"
    + "  (? IS NULL OR mime_type = ?) AND \n"
    + "  (? IS NULL OR title = ?)";
  ;
  
  public static int[] getArgTypes()
  {
    return Arrays.copyOf(ARG_TYPES, ARG_TYPES.length);
  }
  
  public Object[] getArgs(String corpusPath, 
    String mimeType, String title, int offset, int length)
  {
    return new Object[] 
    {
      corpusPath, mimeType, mimeType, title, title
    }; 

  }

  @Override
  public AnnisBinaryMetaData extractData(ResultSet rs) throws
    DataAccessException
  {
    AnnisBinaryMetaData ab = new AnnisBinaryMetaData();
    try
    {
      while (rs.next())
      {
        ab.setLocalFileName(rs.getString("filename"));
        ab.setFileName(rs.getString("title"));
        ab.setMimeType(rs.getString("mime_type"));
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
