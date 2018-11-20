package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.LoggerFactory;

import annis.service.objects.AnnisBinaryMetaData;

public class ByteHelper implements ResultSetHandler<AnnisBinaryMetaData> {

    public static final String SQL = "SELECT\n" + "filename, title, mime_type\n" + "FROM media_files\n" + "WHERE\n"
            + "  corpus_path = ? AND\n" + "  (? IS NULL OR mime_type = ?) AND \n" + "  (? IS NULL OR title = ?)";;

    @Override
    public AnnisBinaryMetaData handle(ResultSet rs) throws SQLException {
        AnnisBinaryMetaData ab = new AnnisBinaryMetaData();
        while (rs.next()) {
            ab.setLocalFileName(rs.getString("filename"));
            ab.setFileName(rs.getString("title"));
            ab.setMimeType(rs.getString("mime_type"));
            // we only give one matching result back
            break;
        }
        return ab;
    }

}
