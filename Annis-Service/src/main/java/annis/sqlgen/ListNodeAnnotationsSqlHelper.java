/**
 * 
 */
package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import annis.model.AnnisNode;
import annis.service.ifaces.AnnisAttribute;
import annis.service.objects.AnnisAttributeImpl;
import org.apache.commons.lang.StringUtils;

public class ListNodeAnnotationsSqlHelper implements ResultSetExtractor {

	public String createSqlQuery(List<Long> corpusList, boolean listValues) {
		String template = "SELECT DISTINCT node_annotation_namespace, node_annotation_name, "
      + ":value FROM facts WHERE sample_n_na = true";

    if(corpusList != null && !corpusList.isEmpty())
    {
      template += " AND toplevel_corpus IN (" + StringUtils.join(corpusList, ", ") + ")";
    }
		String sql = template.replace(":value", listValues ? "node_annotation_value" : "NULL AS node_annotation_value");
		return sql;
	}
	
	public Object extractData(ResultSet resultSet) throws SQLException,
			DataAccessException {
		Map<String, AnnisAttribute> attributesByName = new HashMap<String, AnnisAttribute>();
		
		while (resultSet.next()) {

			String namespace = resultSet.getString("node_annotation_namespace");
			String name = resultSet.getString("node_annotation_name");
			String qName = AnnisNode.qName(namespace, name);
			
			if ( ! attributesByName.containsKey(qName) )
				attributesByName.put(qName, new AnnisAttributeImpl());
	
			AnnisAttribute attribute = attributesByName.get(qName);
			attribute.setName(qName);
			
			String value = resultSet.getString("node_annotation_value");
			
			if (value != null)
				attribute.addValue(value);
			
		}
		
		return new ArrayList<AnnisAttribute>(attributesByName.values());
	}
	
}