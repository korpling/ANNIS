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
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SNode;

/**
 * Generates a simple query for a specific tok.
 *
 * <p>The Structure of the query is like this:</p>
 *
 * <p>Query:
 * <code>"der"</code></p>
 * <p>Description:
 * <code>search for the word "der"</code></p>
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class AutoTokQuery extends AbstractAutoQuery
{
  String finalAql = "default";

  @Override
  public String getAQL()
  {
    return "tok";
  }

  @Override
  public void analyzingQuery(SaltProject saltProject)
  {
    List<SToken> tokens = new ArrayList<>();
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
                tokens.add((SToken) n);
              }
            }
          }
        }
      }
    }

    // select one random token from the result
    if (!tokens.isEmpty())
    {
      int tries = 10;
      int r = new Random().nextInt(tokens.size() - 1);
      String text = CommonHelper.getSpannedText(tokens.get(r));
      while ("".equals(text) && tries > 0)
      {
        r = new Random().nextInt(tokens.size() - 1);
        text = CommonHelper.getSpannedText(tokens.get(r));
        tries--;
      }

      if ("".equals(text))
      {
        finalAql = null;
      }
      else
      {
        finalAql = "\"" + text + "\"";
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
    return finalAql;
  }

  @Override
  public String getDescription()
  {
    return "search for the word " + finalAql;
  }
}
