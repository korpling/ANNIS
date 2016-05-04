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

import java.util.List;
import org.apache.commons.lang3.Validate;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
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
  public String matchedAnnotation(SNode node)
  {
    if(node instanceof SSpan || node instanceof SToken)
    {
      for(SAnnotation anno : node.getAnnotations())
      {
        if(annotationValue.equals(anno.getValue_STEXT()))
        {
          return anno.getQName();
        }
      }
    }
    return null;
  }

  public String getAnnotationValue()
  {
    return annotationValue;
  }

  @Override
  public List<String> getRequiredAnnotationNames()
  {
    // always require all annotations
    return null;
  }
  
  
  
}
