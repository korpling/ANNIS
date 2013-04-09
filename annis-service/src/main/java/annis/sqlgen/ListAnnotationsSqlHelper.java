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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import annis.model.AnnisNode;
import annis.service.objects.AnnisAttribute;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListAnnotationsSqlHelper implements ResultSetExtractor
{

  private static Logger log = LoggerFactory.getLogger(
    ListAnnotationsSqlHelper.class);

  public String createSqlQuery(List<Long> corpusList,
    boolean listValues, boolean onlyMostFrequentValue)
  {
    String sql = "select namespace, name, value, \"type\", subtype, edge_namespace, edge_name from\n"
      + "(\n"
      + "  select *, row_number() OVER (PARTITION BY namespace, name, edge_namespace, edge_name) as row_num\n"
      + "  FROM\n"
      + "  (\n"
      + "    select distinct\n"
      + "    namespace, name, \"type\", subtype, edge_name, edge_namespace, "
      + "    occurences, :value AS value\n"
      + "    FROM annotations\n"
      + "    WHERE\n"
      + "    (value IS NULL OR value <> '--')\n"
      + (corpusList.isEmpty() ? "\n" : "    AND toplevel_corpus IN (:corpora)\n")
      + "    ORDER by namespace, name, edge_namespace, edge_name, occurences desc\n"
      + "  ) as tableAll\n"
      + ") as tableFreq\n";
    if ((listValues && onlyMostFrequentValue) || !listValues)
    {
      sql += "where row_num = 1";
    }

    // fetch corpus annotations
    sql += "UNION"
      + "\nSELECT distinct"
      + "\nm.namespace, m.name, :value, 'meta' as \"type\", 'm' as subtype, "
      + "\n'' as edge_namespace, '' as ege_name"
      + "\nFROM corpus_annotation as m, corpus c, corpus p"
      + "\n WHERE p.id IN (:corpora)"
      + "\nAND c.pre > p.pre"
      + "\nAND c.post < p.post"
      + "\nAND m.corpus_ref = c.id"
      + "\nORDER BY name, edge_namespace, edge_name";

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
      if (qEdgeName != null)
      {
        key += "_" + qEdgeName;
      }

      if (!attributesByName.containsKey(key))
      {
        attributesByName.put(key, new AnnisAttribute());
      }

      AnnisAttribute attribute = attributesByName.get(key);
      attribute.setName(qName);
      attribute.setEdgeName(qEdgeName);
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

    return new ArrayList<AnnisAttribute>(attributesByName.values());
  }
}
