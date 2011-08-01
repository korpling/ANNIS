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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author thomas
 */
public class WekaHelper
{

  public String exportAsArff(List<AnnotatedMatch> annotatedMatches)
  {
    StringBuffer sb = new StringBuffer();

    // header: relation name (unused)
    sb.append("@relation name\n");
    sb.append("\n");

    // figure out what annotations are used at each match position
    SortedMap<Integer, SortedSet<String>> columnsByNodePos = new TreeMap<Integer, SortedSet<String>>();
    for (int i = 0; i < annotatedMatches.size(); ++i)
    {
      AnnotatedMatch match = annotatedMatches.get(i);
      for (int j = 0; j < match.size(); ++j)
      {
        AnnotatedSpan span = match.get(j);
        if (columnsByNodePos.get(j) == null)
        {
          columnsByNodePos.put(j, new TreeSet<String>());
        }
        for (Annotation annotation : span.getAnnotations())
        {
          columnsByNodePos.get(j).add(annotation.getQualifiedName());
        }
      }
    }

    // print column names and data types
    int count = columnsByNodePos.keySet().size();
    for (int j = 0; j < count; ++j)
    {
      sb.append("@attribute " + fullColumnName(j + 1, "id") + " string\n");
      sb.append("@attribute " + fullColumnName(j + 1, "span") + " string\n");
      SortedSet<String> annotationNames = columnsByNodePos.get(j);
      for (String name : annotationNames)
      {
        sb.append("@attribute " + fullColumnName(j + 1, name) + " string\n");
      }
    }
    sb.append("\n@data\n\n");

    // print values
    for (AnnotatedMatch match : annotatedMatches)
    {
      List<String> line = new ArrayList<String>();
      int k = 0;
      for (; k < match.size(); ++k)
      {
        AnnotatedSpan span = match.get(k);
        Map<String, String> valueByName = new HashMap<String, String>();
        if (span != null && span.getAnnotations() != null)
        {
          for (Annotation annotation : span.getAnnotations())
          {
            valueByName.put(annotation.getQualifiedName(), annotation.getValue());
          }
          line.add("'" + span.getId() + "'");
          line.add("'" + span.getCoveredText().replace("'", "\\'") + "'");
        }

        for (String name : columnsByNodePos.get(k))
        {
          if (valueByName.containsKey(name))
          {
            line.add("'" + valueByName.get(name).replace("'", "\\'") + "'");
          }
          else
          {
            line.add("'NULL'");
          }
        }
      }
      for (int l = k; l < count; ++l)
      {
        line.add("'NULL'");
        for (int m = 0; m <= columnsByNodePos.get(l).size(); ++m)
        {
          line.add("'NULL'");
        }
      }
      sb.append(StringUtils.join(line, ","));
      sb.append("\n");
    }

    return sb.toString();
  }

  private String fullColumnName(int i, String name)
  {
    return "#" + i + "_" + name;
  }
}
