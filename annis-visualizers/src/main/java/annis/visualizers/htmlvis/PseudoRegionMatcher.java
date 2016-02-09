/*
 * Copyright 2014 SFB 632.
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

import java.util.LinkedList;
import java.util.List;
import org.corpus_tools.salt.core.SNode;

/**
 *
 * @author Amir Zeldes
 */
public class PseudoRegionMatcher implements SpanMatcher
{

  private String annotationName;

  enum PseudoRegion
  {

    BEGIN,
    END,
    ALL;
  }

  final private PseudoRegion psdRegion;

  @Override
  public String matchedAnnotation(SNode node)
  {

    return null;

  }

  public PseudoRegionMatcher(PseudoRegion psdRegion)
  {

    this.psdRegion = psdRegion;
    if (null != psdRegion)
    {
      switch (psdRegion)
      {
        case BEGIN:
          this.annotationName = "annis_BEGIN";
          break;
        case END:
          this.annotationName = "annis_END";
          break;
        case ALL:
          this.annotationName = "annis_ALL";
          break;
        default:
          break;
      }
    }

  }

  public PseudoRegion getPsdRegion()
  {
    return psdRegion;
  }

  @Override
  public List<String> getRequiredAnnotationNames()
  {
    // we don't need any annotation, so return empty list
    return new LinkedList<>();
  }

  public String getAnnotationName()
  {
    return annotationName;
  }

}
