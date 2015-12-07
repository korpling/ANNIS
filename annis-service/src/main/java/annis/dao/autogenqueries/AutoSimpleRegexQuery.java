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
package annis.dao.autogenqueries;

import annis.CommonHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SNode;

/**
 * Generates a simple regex query.
 *
 * <p>The Structure of the query is like this:</p>
 *
 * <p>Query:
 * <code>/[Ss]ie/</code></p>
 * <p>Description:
 * <code>Search for the "sie" with upper or lower-case 's' (regular expression)</code></p>
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class AutoSimpleRegexQuery extends AbstractAutoQuery
{

  // this is put as example query into the example query object.
  private String finalAQL;

  // the word which is transformed to the regex.
  private String text;

  @Override
  public String getAQL()
  {
    return "tok";
  }

  @Override
  public void analyzingQuery(SaltProject saltProject)
  {

    List<String> tokens = new ArrayList<>();
    for (SCorpusGraph g : saltProject.getCorpusGraphs())
    {
      if (g != null)
      {
        for (SDocument doc : g.getDocuments())
        {
          SDocumentGraph docGraph = doc.getDocumentGraph();
          List<SNode> sNodes = docGraph.getNodes();

          if (sNodes != null)
          {
            for (SNode n : sNodes)
            {
              if (n instanceof SToken)
              {
                tokens.add(CommonHelper.getSpannedText((SToken) n));
              }
            }
          }
        }
      }
    }

    // try to find a word with which is contained twice with Capitalize letter.
    text = null;
    for (int i = 0; i < tokens.size(); i++)
    {
      for (int j = i + 1; j < tokens.size(); j++)
      {
        if (tokens.get(i).equalsIgnoreCase(tokens.get(j)))
        {

          if (tokens.get(i).length() > 1
            && ((Character.isLowerCase(tokens.get(i).charAt(0))
            && Character.isUpperCase(tokens.get(j).charAt(0)))
            || (Character.isLowerCase(tokens.get(j).charAt(0))
            && Character.isUpperCase(tokens.get(i).charAt(0)))))
          {
            text = tokens.get(i);
            break;
          }
        }
      }
    }

    if (text != null)
    {
      Character upperLetter = Character.toUpperCase(text.charAt(0));
      Character lowerLetter = Character.toLowerCase(text.charAt(0));
      String rest = StringUtils.
        substring(text, -(text.length() - 1));

      finalAQL = "/[" + upperLetter + lowerLetter + "]" + rest + "/";
    }
    else
    {
      // select one random token from the result
      int tries = 10;
      int r = new Random().nextInt(tokens.size() - 1);
      text = tokens.get(r);
      while ("".equals(text) && tries > 0)
      {
        r = new Random().nextInt(tokens.size() - 1);
        text = tokens.get(r);
        tries--;
      }

      if (!"".equals(text) && text.length() > 1)
      {
        Character upperLetter = Character.toUpperCase(text.charAt(0));
        Character lowerLetter = Character.toLowerCase(text.charAt(0));
        String rest = StringUtils.substring(text, -(text.length() - 1));

        finalAQL = "/[" + upperLetter + lowerLetter + "]" + rest + "/";
      }
      else
      {
        finalAQL = "";
      }
    }
  }

  @Override
  public int getNodes()
  {
    return 1;
  }

  @Override
  public String getFinalAQLQuery()
  {
    return finalAQL;
  }

  @Override
  public String getDescription()
  {

    if (text != null && text.length() > 0)
    {
      return "Search for the \"" + text.toLowerCase()
        + "\" with upper or lower-case  ''" + text.toLowerCase().charAt(0)
        + "'' (regular expression)";
    }
    else
    {
      return null;
    }
  }
}
