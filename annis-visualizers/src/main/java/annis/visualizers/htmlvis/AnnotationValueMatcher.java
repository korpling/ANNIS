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

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import org.apache.commons.lang3.Validate;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AnnotationValueMatcher implements SpanMatcher
{
  private String annotationValue;

  public AnnotationValueMatcher(String annotationValue)
  {
    Validate.notNull(annotationValue, "The annotation value parameter must never be null.");
    this.annotationValue = annotationValue;
  }
  
  @Override
  public boolean matches(SNode node)
  {
    if(node instanceof SSpan)
    {
      SSpan span = (SSpan) node;
      for(SAnnotation anno : span.getSAnnotations())
      {
        if(annotationValue.equals(anno.getSValueSTEXT()))
        {
          return true;
        }
      }
    }
    return false;
  }

  public String getAnnotationValue()
  {
    return annotationValue;
  }
  
}
