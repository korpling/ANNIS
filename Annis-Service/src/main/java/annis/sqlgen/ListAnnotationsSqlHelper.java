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

public class ListAnnotationsSqlHelper implements ResultSetExtractor
{

  public String createSqlQuery(List<Long> corpusList,
    boolean listValues, boolean onlyMostFrequentValue)
  {
    String sql = "select ':type' as \"type\", namespace, name, value from\n"
      + "(\n"
      + "  select *, row_number() OVER (PARTITION BY namespace, name) as row_num\n"
      + "  FROM\n"
      + "  (\n"
      + "    select \n"
      + "    :type_annotation_namespace as namespace, :type_annotation_name as name, :value AS value, \n"
      + "    count(:type_annotation_value) as frequency\n"
      + "    FROM facts\n"
      + "    WHERE\n"
      + "    :type_annotation_value <> '--' AND\n"
      + "    toplevel_corpus IN (:corpora)\n"
      + "    GROUP BY :type_annotation_namespace, :type_annotation_name, :type_annotation_value\n"
      + "    ORDER by :type_annotation_namespace, :type_annotation_name, frequency desc\n"
      + "  ) as tableAll\n"
      + ") as tableFreq\n";
    if ((listValues && onlyMostFrequentValue) || !listValues)
    {
      sql += "where row_num = 1";
    }

    sql = sql.replaceAll(":corpora", StringUtils.join(corpusList, ", "));
    sql = sql.replaceAll(":value", listValues ? ":type_annotation_value" : "NULL::varchar");

    String sqlNode = sql.replaceAll(":type", "node");
    String sqlEdge = sql.replaceAll(":type", "edge");

    String sqlBoth =
      sqlNode + "\n"
      + "UNION ALL\n"
      + sqlEdge + "\n";

    return sqlBoth;
  }

  @Override
  public Object extractData(ResultSet resultSet) throws SQLException,
    DataAccessException
  {
    Map<String, AnnisAttribute> attributesByName = new HashMap<String, AnnisAttribute>();

    while (resultSet.next())
    {

      String namespace = resultSet.getString("namespace");
      String name = resultSet.getString("name");
      String qName = AnnisNode.qName(namespace, name);

      if (!attributesByName.containsKey(qName))
      {
        attributesByName.put(qName, new AnnisAttributeImpl());
      }

      AnnisAttribute attribute = attributesByName.get(qName);
      attribute.setName(qName);
      AnnisAttribute.Type t = AnnisAttribute.Type.unknown;
      try
      {
        t = AnnisAttribute.Type.valueOf(resultSet.getString("type"));
      }
      catch(Exception ex)
      {
        // ignore
      }
      attribute.setType(t);

      String value = resultSet.getString("value");

      if (value != null)
      {
        attribute.addValue(value);
      }

    }

    return new ArrayList<AnnisAttribute>(attributesByName.values());
  }
}
