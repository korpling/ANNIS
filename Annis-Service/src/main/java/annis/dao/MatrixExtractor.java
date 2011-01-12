/*
 *  Copyright 2010 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package annis.dao;

import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;
import annis.model.Annotation;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author thomas
 */
public class MatrixExtractor implements ResultSetExtractor
{

  private String matchedNodesViewName;

  @Override
  public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
  {
    List<AnnotatedMatch> matches = new ArrayList<AnnotatedMatch>();

    Map<List<Long>, AnnotatedSpan[]> matchesByGroup = new HashMap<List<Long>, AnnotatedSpan[]>();

    while (resultSet.next())
    {
      long id = resultSet.getLong("id");
      String coveredText = resultSet.getString("span");
      List<Annotation> annotations = new ArrayList<Annotation>();

      Array array = resultSet.getArray("annotations");
      if (array != null)
      {
        /* I'd rather get the type components directly, but PostgreSQL doesn't support this?
         * A CSV parser that handles quoted elements would work!
         * Try: http://opencsv.sourceforge.net/
        System.out.println(array.getBaseTypeName());
        ResultSet annotationRs = array.getResultSet();
        ResultSetMetaData meta = annotationRs.getMetaData();
        int count = meta.getColumnCount();
        System.out.println("columns in annotation type: " + count);
        for (int j = 1; j <= count; ++j) {
        System.out.println("column[" + j + "]: " + meta.getColumnName(j) + "(" + meta.getColumnTypeName(j) + ")");
        }
        int k = 0;
        while (annotationRs.next()) {
        PGobject anno = (PGobject) annotationRs.getObject("value");
        PGtokenizer t = new PGtokenizer(PGtokenizer.remove(anno.getValue(), "(", ")"), ',');
        if (t.getSize() != 3)
        throw new DataRetrievalFailureException("Could not read to annotation type: " + anno.getValue());
        String namespace = t.getToken(0);
        String name = t.getToken(1);
        String value = t.getToken(2);
        annotations.add(new Annotation(namespace, name, value));
        }
        System.out.println("rows: " + k);
         */

        String[] annotationStrings = (String[]) array.getArray();

        for (String annotationString : annotationStrings)
        {
          String namespace = null;
          String name = null;
          String value = null;

          // fugly, but array_agg(ARRAY['a'::varchar, 'b', 'c']) does not work
          String[] split1 = annotationString.split("=");
          String[] split2 = split1[0].split(":");
          if (split2.length > 1)
          {
            namespace = split2[0];
            name = split2[1];
          }
          else
          {
            name = split2[0];
          }
          value = split1[1];

          annotations.add(new Annotation(namespace, name, value));
        }

        // create key
        Array sqlKey = resultSet.getArray("key");
        Validate.isTrue(!resultSet.wasNull(), "Match group identifier must not be null");
        Validate.isTrue(sqlKey.getBaseType() == Types.BIGINT,
          "Key in database must be from the type \"bigint\" but was \"" + sqlKey.getBaseTypeName() + "\"");

        Long[] keyArray = (Long[]) sqlKey.getArray();
        int matchWidth = keyArray.length;
        List<Long> key = Arrays.asList(keyArray);

        if (!matchesByGroup.containsKey(key))
        {
          matchesByGroup.put(key, new AnnotatedSpan[matchWidth]);
        }

        // set annotation spans for *all* positions of the id
        // (node could have matched several times)
        for(int posInMatch=0; posInMatch < key.size(); posInMatch++)
        {
          if(key.get(posInMatch) == id)
          {
            matchesByGroup.get(key)[posInMatch] = new AnnotatedSpan(id, coveredText, annotations);
          }
        }
      }
    }

    for(AnnotatedSpan[] match : matchesByGroup.values())
    {
      matches.add(new AnnotatedMatch(Arrays.asList(match)));
    }

    return matches;

  }

  private String getContextQuery(List<Long> corpusList, int maxWidth)
  {
    StringBuilder keySb = new StringBuilder();
    keySb.append("ARRAY[matches.id1");
    for (int i = 2; i <= maxWidth; ++i)
    {
      keySb.append(",");
      keySb.append("matches.id");
      keySb.append(i);
    }
    keySb.append("] AS key");
    String key = keySb.toString();

    StringBuilder sb = new StringBuilder();

    sb.append("SELECT \n");
    sb.append("\t");
    sb.append(key);
    sb.append(",\nfacts.id AS id,\n");
    sb.append("min(substr(text.text, facts.left+1,facts.right-facts.left)) AS span,\n");
    sb.append("array_agg(DISTINCT coalesce(facts.node_annotation_namespace || ':', '') || facts.node_annotation_name || '=' || facts.node_annotation_value) AS annotations");
    sb.append("\nFROM\n");
    sb.append("\t");
    sb.append(matchedNodesViewName);
    sb.append(" AS matches,\n");

    sb.append("\t");
    sb.append(FACTS_TABLE);
    sb.append(" AS facts,\n");
    sb.append("text AS text\n");

    sb.append("WHERE\n");

    if (corpusList != null)
    {
      sb.append("facts.toplevel_corpus IN (");
      sb.append(corpusList.isEmpty() ? "NULL" : StringUtils.join(corpusList, ","));
      sb.append(") AND\n");
    }
    sb.append("facts.text_ref = text.id AND \n");

    sb.append("(");
    for (int i = 1; i <= maxWidth; i++)
    {
      sb.append("facts.id = matches.id").append(i);
      if (i < maxWidth)
      {
        sb.append(" OR ");
      }
    }
    sb.append(")\n");
    sb.append("GROUP BY key, facts.id, span");

    Logger.getLogger(MatrixExtractor.class).debug("generated SQL for matrix:\n" + sb.toString());

    return sb.toString();
  }

  public List<AnnotatedMatch> queryMatrix(JdbcTemplate jdbcTemplate, List<Long> corpusList, int maxWidth)
  {
    return (List<AnnotatedMatch>) jdbcTemplate.query(getContextQuery(corpusList, maxWidth), this);
  }

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }
}
