package annis.sqlgen;

import annis.model.Annotation;
import com.google.common.base.Splitter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

public class ListDocumentsHelper extends AbstractListHandler<Annotation> {

    @Override
    protected Annotation handleRow(ResultSet rs) throws SQLException {
        Annotation anno = new Annotation();
        anno.setCorpusName(rs.getString("corpus"));
        List<String> path = Splitter.on('/').splitToList(rs.getString("path"));
        anno.setAnnotationPath(path);
        anno.setName(path.get(path.size() - 1));

        return anno;

    }

}
