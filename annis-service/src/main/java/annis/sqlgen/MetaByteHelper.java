package annis.sqlgen;

import annis.service.objects.AnnisBinaryMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class MetaByteHelper implements ResultSetExtractor<List<AnnisBinaryMetaData>>
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(MetaByteHelper.class);
  
  private static final int[] ARG_TYPES = new int [] {
    Types.VARCHAR, Types.VARCHAR
  };
  

  public static final String SQL =
        "SELECT\n"
      + "  filename, title, mime_type, sub.name as corpus_name\n"
      + "FROM media_files, corpus AS sub, corpus AS top \n"
      + "WHERE\n"
      + "  top.top_level = true AND\n"
      + "  top.name = ? AND\n"
      + "  sub.name = ? AND\n"
      + "  sub.pre >= top.pre AND sub.post <= top.post AND\n"
      + "  sub.id = corpus_ref";
  ;
  
  public static int[] getArgTypes()
  {
    return Arrays.copyOf(ARG_TYPES, ARG_TYPES.length);
  }
  
  public Object[] getArgs(String toplevelCorpusName, String corpusName)
  {
    return new Object[] 
    {
      toplevelCorpusName, corpusName
    }; 

  }

  @Override
  public List<AnnisBinaryMetaData> extractData(ResultSet rs) throws
    DataAccessException
  {
    List<AnnisBinaryMetaData> result = new LinkedList<>();
    try
    {
      while (rs.next())
      {
        AnnisBinaryMetaData ab = new AnnisBinaryMetaData();
        ab.setLocalFileName(rs.getString("filename"));
        ab.setFileName(rs.getString("title"));
        ab.setCorpusName(rs.getString("corpus_name"));
        ab.setMimeType(rs.getString("mime_type"));
        
        result.add(ab);
      }
    }
    catch (SQLException ex)
    {
      log.error(null, ex);
    }

    return result;
  }

}
