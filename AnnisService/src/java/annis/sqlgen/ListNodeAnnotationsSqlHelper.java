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

public class ListNodeAnnotationsSqlHelper implements ResultSetExtractor {

	public String createSqlQuery(List<Long> corpusList, boolean listValues) {
		String template = "SELECT DISTINCT namespace, name, :value FROM node_annotation";
		String sql = template.replace(":value", listValues ? "value" : "NULL AS value");
		return sql;
	}
	
	public Object extractData(ResultSet resultSet) throws SQLException,
			DataAccessException {
		Map<String, AnnisAttribute> attributesByName = new HashMap<String, AnnisAttribute>();
		
		while (resultSet.next()) {

			String namespace = resultSet.getString("namespace");
			String name = resultSet.getString("name");
			String qName = AnnisNode.qName(namespace, name);
			
			if ( ! attributesByName.containsKey(qName) )
				attributesByName.put(qName, new AnnisAttributeImpl());
	
			AnnisAttribute attribute = attributesByName.get(qName);
			attribute.setName(qName);
			
			String value = resultSet.getString("value");
			
			if (value != null)
				attribute.addValue(value);
			
		}
		
		return new ArrayList<AnnisAttribute>(attributesByName.values());
	}
	
}