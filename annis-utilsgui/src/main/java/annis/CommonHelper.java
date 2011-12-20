/*
 * Copyright 2011 SFB 632.
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
package annis;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class CommonHelper
{

  public static boolean containsRTLText(String str)
  {
    for (int i = 0; i < str.length(); i++)
    {
      char cc = str.charAt(i);
      // hebrew extended and basic, arabic basic and extendend
      if (cc >= 1425 && cc <= 1785)
      {
        return true;
      }
      // alphabetic presentations forms (hebrwew) to arabic presentation forms A
      else if (cc >= 64286 && cc <= 65019)
      {
        return true;
      }
      // arabic presentation forms B
      else if (cc >= 65136 && cc <= 65276)
      {
        return true;
      }
    }
    return false;
  }

  public static Set<String> getTokenAnnotationLevelSet(SaltProject p)
  {
    Set<String> result = new TreeSet<String>();

    for (SCorpusGraph corpusGraphs : p.getSCorpusGraphs())
    {
      for (SDocument doc : corpusGraphs.getSDocuments())
      {
        SDocumentGraph g = doc.getSDocumentGraph();
        for (SNode n : g.getSNodes())
        {
          if (n instanceof SToken)
          {
            for (SAnnotation anno : n.getSAnnotations())
            {
              result.add(anno.getQName());
            }
          }
        }
      }
    }

    return result;
  }

  public static List<String> getCorpusPath(SCorpusGraph corpusGraph, SDocument doc)
  {
    List<String> result = new LinkedList<String>();

    result.add(doc.getSName());
    SCorpus c = corpusGraph.getSCorpus(doc);
    
    while (c != null)
    {
      result.add(c.getSName());
      c = corpusGraph.getSCorpus(c.getSElementId());
    }
    return result;
  }
}
