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
package annis;

import annis.dao.AnnotatedMatch;
import annis.dao.AnnotatedSpan;
import annis.model.Annotation;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class WekaHelper
{
  
  private static final Logger log = LoggerFactory.getLogger(WekaHelper.class);

  public static void exportAsArff(List<AnnotatedMatch> annotatedMatches, OutputStream out)
  {
    PrintWriter w = null;
    try
    {
      w = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
      // header: relation name (unused)
      w.append("@relation name\n");
      w.append("\n");
      // figure out what annotations are used at each match position
      SortedMap<Integer, SortedSet<String>> columnsByNodePos = new TreeMap<Integer, SortedSet<String>>();
      for(int i = 0; i < annotatedMatches.size(); ++i)
      {
        AnnotatedMatch match = annotatedMatches.get(i);
        for(int j = 0; j < match.size(); ++j)
        {
          AnnotatedSpan span = match.get(j);
          if(columnsByNodePos.get(j) == null)
          {
            columnsByNodePos.put(j, new TreeSet<String>());
          }
          for(Annotation annotation : span.getAnnotations())
          {
            columnsByNodePos.get(j).add("anno_" + annotation.getQualifiedName());
          }
          
          for(Annotation meta : span.getMetadata())
          {
            columnsByNodePos.get(j).add("meta_" + meta.getQualifiedName());
          }
          
        }
      }
      // print column names and data types
      int count = columnsByNodePos.keySet().size();
      for(int j = 0; j < count; ++j)
      {
        w.append("@attribute ").append(fullColumnName(j + 1, "id")).append(" string\n");
        w.append("@attribute ").append(fullColumnName(j + 1, "span")).append(" string\n");
        SortedSet<String> annotationNames = columnsByNodePos.get(j);
        for(String name : annotationNames)
        {
          w.append("@attribute ").append(fullColumnName(j + 1, name)).append(" string\n");
        }
      }
      w.append("\n@data\n\n");
      // print values
      for(AnnotatedMatch match : annotatedMatches)
      {
        List<String> line = new ArrayList<String>();
        int k = 0;
        for(; k < match.size(); ++k)
        {
          AnnotatedSpan span = match.get(k);
          Map<String, String> valueByName = new HashMap<String, String>();

          if(span != null)
          {
            if(span.getAnnotations() != null)
            {
              for(Annotation annotation : span.getAnnotations())
              {
                valueByName.put("anno_" + annotation.getQualifiedName(), annotation.getValue());
              }
            }
            if(span.getMetadata() != null)
            {
              for(Annotation meta : span.getMetadata())
              {
                valueByName.put("meta_" + meta.getQualifiedName(), meta.getValue());
              }
            }

            line.add("'" + span.getId() + "'");
            line.add("'" + span.getCoveredText().replace("'", "\\'") + "'");
          }

          for(String name : columnsByNodePos.get(k))
          {
            if(valueByName.containsKey(name))
            {
              line.add("'" + valueByName.get(name).replace("'", "\\'") + "'");
            }
            else
            {
              line.add("'NULL'");
            }
          }
        }
        for(int l = k; l < count; ++l)
        {
          line.add("'NULL'");
          for(int m = 0; m <= columnsByNodePos.get(l).size(); ++m)
          {
            line.add("'NULL'");
          }
        }
        w.append(StringUtils.join(line, ","));
        w.append("\n");
      }
    }
    catch (UnsupportedEncodingException ex)
    {
      log.error(null, ex);
    }
    finally
    {
      if(w != null)
      {
        w.flush();
      }
    }
  }

  private static String fullColumnName(int i, String name)
  {
    return "#" + i + "_" + name;
  }
}
