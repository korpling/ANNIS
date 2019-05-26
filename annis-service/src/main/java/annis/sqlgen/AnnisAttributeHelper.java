package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisAttribute.SubType;
import annis.service.objects.AnnisAttribute.Type;

public class AnnisAttributeHelper extends AbstractListHandler<AnnisAttribute> {

    @Override
    public AnnisAttribute handleRow(ResultSet rs) throws SQLException {
        AnnisAttribute result = new AnnisAttribute();
        
        result.setName(rs.getString("name"));
        String typeRaw = rs.getString("type");
        if(typeRaw != null) {
            result.setType(Type.valueOf(typeRaw));
        }
        String subTypeRaw = rs.getString("sub_type");
        if(subTypeRaw != null) {
            result.setSubtype(SubType.valueOf(subTypeRaw));
        }
        result.setEdgeName(result.getEdgeName());
        String value = rs.getString("value");
        if(value != null) {
            result.getValueSet().add(value);
        }
        return result;
    }

}
