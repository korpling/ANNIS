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
package annis.visualizers.htmlvis;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class TokenMatcher implements SpanMatcher
{
  public TokenMatcher()
  {
  }
  
  @Override
  public String matchedAnnotation(SNode node)
  {
    if(node instanceof SToken)
    {
      return "tok";
    }
    else
    {
      return null;
    }
  }
  
}
