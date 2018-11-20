package annis.sqlgen;

import annis.service.objects.AnnisBinaryMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.slf4j.LoggerFactory;

public class MetaByteHelper extends AbstractListHandler<AnnisBinaryMetaData>
{


  public static final String SQL =
        "SELECT\n"
      + "  filename, title, mime_type\n"
      + "FROM media_files \n"
      + "WHERE corpus_path = ? ";
  ;

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
