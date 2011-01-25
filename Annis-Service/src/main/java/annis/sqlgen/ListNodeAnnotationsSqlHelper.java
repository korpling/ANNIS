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

	public String createSqlQuery(List<Long> corpusList, boolean listValues, boolean onlyMostFrequentValue)
  {


    String sql = "select node_annotation_namespace, node_annotation_name, node_annotation_value from\n"
     + "(\n"
     + "  select *, row_number() OVER (PARTITION BY node_annotation_namespace, node_annotation_name) as row_num\n"
     + "  FROM\n"
     + "  (\n"
     + "    select \n"
     + "    node_annotation_namespace, node_annotation_name, :value AS node_annotation_value, \n"
     + "    count(node_annotation_value) as frequency\n"
     + "    FROM facts\n"
     + "    WHERE\n"
     + "    sample_node_annotation = true AND\n"
     + "    node_annotation_value <> '--' AND\n"
     + "    toplevel_corpus IN (:corpora)\n"
     + "    GROUP BY node_annotation_namespace, node_annotation_name, node_annotation_value\n"
     + "    ORDER by node_annotation_namespace, node_annotation_name, frequency desc\n"
     + "  ) as tableAll\n"
     + ") as tableFreq\n";
    if( (listValues && onlyMostFrequentValue) || !listValues )
    {
      sql += "where row_num = 1";
    }

    sql = sql.replace(":corpora",  StringUtils.join(corpusList, ", "));
    sql = sql.replace(":value", listValues ? "node_annotation_value" : "NULL");
    
		return sql;
	}
	
  @Override
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