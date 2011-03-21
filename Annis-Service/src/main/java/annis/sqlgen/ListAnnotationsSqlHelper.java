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
    String sql = "select namespace, name, value, \"type\", subtype, edge_namespace, edge_name from\n"
      + "(\n"
      + "  select *, row_number() OVER (PARTITION BY namespace, name, edge_namespace, edge_name) as row_num\n"
      + "  FROM\n"
      + "  (\n"
      + "    select distinct\n"
      + "    namespace, name, \"type\", subtype, edge_name, edge_namespace, occurences, :value AS value\n"
      + "    FROM annotations\n"
      + "    WHERE\n"
      + "    value <> '--'\n"
      + (
        corpusList.isEmpty() ?
          "\n" : "    AND toplevel_corpus IN (:corpora)\n"
        )
      + "    ORDER by namespace, name, edge_namespace, edge_name, occurences desc\n"
      + "  ) as tableAll\n"
      + ") as tableFreq\n";
    if ((listValues && onlyMostFrequentValue) || !listValues)
    {
      sql += "where row_num = 1";
    }

    sql = sql.replaceAll(":corpora", StringUtils.join(corpusList, ", "));
    sql = sql.replaceAll(":value", listValues ? "value" : "NULL::varchar");


    return sql;
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

      String edgeNamespace = resultSet.getString("edge_namespace");
      String edgeName = resultSet.getString("edge_name");
      String qEdgeName = AnnisNode.qName(edgeNamespace, edgeName);

      String key = qName;
      if(qEdgeName != null)
      {
        key += "_" + qEdgeName;
      }
      if (!attributesByName.containsKey(key))
      {
        attributesByName.put(key, new AnnisAttributeImpl());
      }

      AnnisAttribute attribute = attributesByName.get(key);
      attribute.setName(qName);

      attribute.setEdgeName(qEdgeName);

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

      AnnisAttribute.SubType st = AnnisAttribute.SubType.unknown;
      try
      {
        st = AnnisAttribute.SubType.valueOf(resultSet.getString("subtype"));
      }
      catch(Exception ex)
      {
        // ignore
      }
      attribute.setSubtype(st);

      String value = resultSet.getString("value");

      if (value != null)
      {
        attribute.addValue(value);
      }

    }

    return new ArrayList<AnnisAttribute>(attributesByName.values());
  }
}
