package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.handlers.AbstractListHandler;

import com.google.common.base.Splitter;

import annis.model.Annotation;

public class MetadataCacheHelper extends AbstractListHandler<Annotation> {

    @Override
    protected Annotation handleRow(ResultSet rs) throws SQLException {
        Annotation anno = new Annotation();
        anno.setCorpusName(rs.getString("corpus"));
        anno.setAnnotationPath(Splitter.on('/').splitToList(rs.getString("path")));
        anno.setType(rs.getString("type"));
        anno.setNamespace(rs.getString("namespace"));
        anno.setName(rs.getString("name"));
        anno.setValue(rs.getString("value"));
        
        return anno;

    }

}
