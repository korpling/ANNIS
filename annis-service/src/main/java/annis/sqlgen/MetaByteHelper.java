package annis.sqlgen;

import annis.service.objects.AnnisBinaryMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class MetaByteHelper extends AbstractListHandler<AnnisBinaryMetaData>
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(MetaByteHelper.class);
  
  private static final int[] ARG_TYPES = new int [] {
    Types.VARCHAR
  };
  

  public static final String SQL =
        "SELECT\n"
      + "  filename, title, mime_type\n"
      + "FROM media_files \n"
      + "WHERE corpus_path = ? ";
  ;
  public static int[] getArgTypes()
  {
    return Arrays.copyOf(ARG_TYPES, ARG_TYPES.length);
  }
  
  public Object[] getArgs(String corpusPath)
  {
    return new Object[] 
    {
      corpusPath
    }; 

  }

  @Override
  protected AnnisBinaryMetaData handleRow(ResultSet rs) throws SQLException
  {
    AnnisBinaryMetaData ab = new AnnisBinaryMetaData();
    ab.setLocalFileName(rs.getString("filename"));
    ab.setFileName(rs.getString("title"));
    ab.setMimeType(rs.getString("mime_type"));
    return ab;
  }

}
