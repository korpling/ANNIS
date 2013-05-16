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
import annis.examplequeries.ExampleQuery;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class AutoTokQuery implements QueriesGenerator.QueryBuilder
{

  String aql = "tok";

  String finalAql = "default";

  private static final Logger log = LoggerFactory.getLogger(AutoTokQuery.class);

  @Override
  public String getAQL()
  {
    return "tok";
  }

  @Override
  public void analyzingQuery(SaltProject saltProject)
  {

    for (SCorpusGraph g : saltProject.getSCorpusGraphs())
    {
      if (g != null)
      {
        for (SDocument doc : g.getSDocuments())
        {
          SDocumentGraph docGraph = doc.getSDocumentGraph();
          EList<SNode> sNodes = docGraph.getSNodes();

          if (sNodes != null)
          {
            List<SToken> tokens = new ArrayList<SToken>();
            for (SNode n : sNodes)
            {
              if (n instanceof SToken)
              {
                tokens.add((SToken) n);
              }
            }

            // select one random token from the result
            int r = new Random().nextInt(tokens.size() - 1);
            String text = CommonHelper.getSpannedText(tokens.get(r));
            finalAql = "\"" + (("".equals(text)) ? null : text) + "\"";
          }
        }
      }
    }
  }

  @Override
  public ExampleQuery getExampleQuery()
  {
    ExampleQuery eQ = new ExampleQuery();
    eQ.setExampleQuery(finalAql);
    eQ.setDescription("search for the word " + finalAql + "");

    return eQ;
  }
}
