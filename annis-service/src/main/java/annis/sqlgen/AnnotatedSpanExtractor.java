/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import annis.dao.objects.AnnotatedSpan;
import annis.model.Annotation;
import java.io.UnsupportedEncodingException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * Maps a {@link ResultSet} row to an {@link AnnotatedSpan}
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AnnotatedSpanExtractor implements RowMapper<AnnotatedSpan>
{
  private final Logger log = LoggerFactory.getLogger(AnnotatedSpanExtractor.class);

  private List<Annotation> extractAnnotations(Array array) throws SQLException
  {
    List<Annotation> result = new ArrayList<Annotation>();

    if (array != null)
    {
      String[] arrayLines = (String[]) array.getArray();

      for (String line : arrayLines)
      {
        if (line != null)
        {
          String namespace = null;
          String name = null;
          String value = null;

          String[] split = line.split(":");
          if (split.length > 2)
          {
            namespace = split[0];
            name = split[1];
            value = split[2];
          }
          else if (split.length > 1)
          {
            name = split[0];
            value = split[1];
          }
          else
          {
            name = split[0];
          }

          if (value != null)
          {
            try
            {
              value = new String(Base64.decodeBase64(value), "UTF-8");
            }
            catch (UnsupportedEncodingException ex)
            {
              log.error(null, ex);
            }
          }

          result.add(new annis.model.Annotation(namespace, name, value));
        } // if line not null
      }
    }

    return result;
  }

  @Override
  public AnnotatedSpan mapRow(ResultSet resultSet, int rowNum) throws SQLException
  {
    long id = resultSet.getLong("id");
    String coveredText = resultSet.getString("span");

    Array arrayAnnotation = resultSet.getArray("annotations");
    ResultSetMetaData rsMeta = resultSet.getMetaData();
    Array arrayMeta = null;
    for(int i=1; i <= rsMeta.getColumnCount(); i++)
    {
      if("metadata".equals(rsMeta.getColumnName(i)))
      {
        arrayMeta = resultSet.getArray(i);
        break;
      }
    }
   
    List<Annotation> annotations = extractAnnotations(arrayAnnotation);
    List<Annotation> metaData = arrayMeta == null ? new LinkedList<Annotation>() 
      : extractAnnotations(arrayMeta);

    // create key
    Array sqlKey = resultSet.getArray("key");
    Validate.isTrue(!resultSet.wasNull(),
      "Match group identifier must not be null");
    Validate.isTrue(sqlKey.getBaseType() == Types.BIGINT,
      "Key in database must be from the type \"bigint\" but was \"" + sqlKey.
      getBaseTypeName() + "\"");
    
    List<Long> key = Arrays.asList((Long[]) sqlKey.getArray());
    
    return new AnnotatedSpan(id, coveredText, annotations, metaData, key);
  }
}
