/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import annis.model.AnnisNode;
import annis.service.objects.AnnisAttribute;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class ListAnnotationsSqlHelper implements ResultSetExtractor
{

  private static Logger log = LoggerFactory.getLogger(
    ListAnnotationsSqlHelper.class);

  public String createSqlQuery(List<Long> corpusList,
    boolean listValues, boolean onlyMostFrequentValue)
  {
    String sqlAnnos = "select namespace, name, value, \"type\", subtype, relation_namespace, relation_name from\n"
      + "(\n"
      + "  select *, row_number() OVER (PARTITION BY namespace, name, relation_namespace, relation_name) as row_num\n"
      + "  FROM\n"
      + "  (\n"
      + "    select\n"
      + "    namespace, name, \"type\", subtype, relation_name, relation_namespace, "
      + "    occurences, :value AS value\n"
      + "    FROM annotations\n"
      + "    WHERE\n"
      + "    (value IS NULL OR value <> '--')\n"
      + (corpusList.isEmpty() ? "\n" : "    AND toplevel_corpus IN (:corpora)\n")
      + "    ORDER by namespace, name, relation_namespace, relation_name, occurences desc\n"
      + "  ) as tableAll\n"
      + ") as tableFreq\n";
    if ((listValues && onlyMostFrequentValue) || !listValues)
    {
      sqlAnnos += "where row_num = 1";
    }

    // fetch corpus annotations
    String sqlMeta =  
      "SELECT namespace, name, value, \"type\", subtype, relation_namespace, relation_name\n"
      + "FROM\n" + "(\n" + "	SELECT\n"
      + "	m.namespace AS namespace, m.name AS \"name\", :value AS value, 'meta'::varchar as \"type\", 'm'::char(1) as subtype, \n"
      + "	''::varchar as relation_namespace, ''::varchar as relation_name, row_number() OVER (PARTITION BY m.namespace, m.name) as row_num\n"
      + "	FROM corpus_annotation as m, corpus c, corpus p\n"
      + "	 WHERE p.id IN (:corpora)\n" + "	AND c.pre > p.pre\n"
      + "	AND c.post < p.post\n" + "	AND m.corpus_ref = c.id\n"
      + ") as metaFreq\n";
    if ((listValues && onlyMostFrequentValue) || !listValues)
    {
      sqlMeta += "WHERE row_num = 1";
    }
    

    String sql = sqlAnnos + "\nUNION\n" + sqlMeta +  "\nORDER BY name, relation_namespace, relation_name";
    
    sql = sql.replaceAll(":corpora", StringUtils.join(corpusList, ", "));
    sql = sql.replaceAll(":value", listValues ? "value" : "NULL::varchar");

    return sql;
  }

  @Override
  public Object extractData(ResultSet resultSet) throws SQLException,
    DataAccessException
  {
    Map<String, AnnisAttribute> attributesByName = new HashMap<>();

    while (resultSet.next())
    {

      String namespace = resultSet.getString("namespace");
      String name = resultSet.getString("name");
      String qName = AnnisNode.qName(namespace, name);

      String relationNamespace = resultSet.getString("relation_namespace");
      String relationName = resultSet.getString("relation_name");
      String qRelationName = AnnisNode.qName(relationNamespace, relationName);

      String key = qName;
      if (qRelationName != null)
      {
        key += "_" + qRelationName;
      }

      if (!attributesByName.containsKey(key))
      {
        attributesByName.put(key, new AnnisAttribute());
      }

      AnnisAttribute attribute = attributesByName.get(key);
      attribute.setName(qName);
      attribute.setEdgeName(qRelationName);
      AnnisAttribute.Type t = AnnisAttribute.Type.unknown;

      try
      {
        t = AnnisAttribute.Type.valueOf(resultSet.getString("type"));
      }
      catch (Exception ex)
      {
        log.warn("annotation type is unknown {}", ex);
      }

      attribute.setType(t);
      AnnisAttribute.SubType st = AnnisAttribute.SubType.unknown;
      String subTypeValue = resultSet.getString("subtype");
      if(subTypeValue != null)
      {
        try
        {
          st = AnnisAttribute.SubType.valueOf(subTypeValue);
        }
        catch (Exception ex)
        {
          log.warn("annotation sub type is unknown {}", ex);
        }
      }

      attribute.setSubtype(st);
      String value = resultSet.getString("value");
      if (value != null)
      {
        attribute.addValue(value);
      }
    }

    return new ArrayList<>(attributesByName.values());
  }
}
