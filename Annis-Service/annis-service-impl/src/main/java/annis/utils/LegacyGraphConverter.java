/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.utils;

import annis.model.AnnotationGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import org.apache.commons.lang.NotImplementedException;

/**
 * This class can convert the current Salt graph model into the legacy model 
 *  AOM (Annis Object Model)
 *  and
 *  "PaulaInline"
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class LegacyGraphConverter
{

  public static AnnotationGraph convertToAnnotationGraph(SaltProject p)
  {
    AnnotationGraph result = new AnnotationGraph();
    
    
    SCorpusGraph corpora = p.getSCorpusGraphs().get(0);
    
    throw new NotImplementedException();
    // return result;
  }

  public static String convertToPaulaInline(SCorpusGraph g)
  {
    throw new NotImplementedException();
  }
}
