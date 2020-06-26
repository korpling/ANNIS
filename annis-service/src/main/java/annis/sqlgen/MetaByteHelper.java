package annis.sqlgen;

import annis.service.objects.AnnisBinaryMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

public class MetaByteHelper extends AbstractListHandler<AnnisBinaryMetaData> {

    public static final String SQL = "SELECT\n" + "  filename, title, mime_type\n" + "FROM media_files \n"
            + "WHERE corpus_path = ? ";;

    @Override
    protected AnnisBinaryMetaData handleRow(ResultSet rs) throws SQLException {
        AnnisBinaryMetaData ab = new AnnisBinaryMetaData();
        ab.setLocalFileName(rs.getString("filename"));
        ab.setFileName(rs.getString("title"));
        ab.setMimeType(rs.getString("mime_type"));
        return ab;
    }

}
