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
public class AnnotationNameAndValueMatcher implements SpanMatcher
{
  private AnnotationNameMatcher nameMatcher;
  private String annotationValue;

  /**
   * 
   * @param annotationNamespace
   * @param annotationName 
   * @param annotationValue
   */
  public AnnotationNameAndValueMatcher(String annotationNamespace, 
    String annotationName, String annotationValue)
  {
    Validate.notNull(annotationName, "The annotation name parameter must never be null.");
    Validate.notNull(annotationValue, "The annotation value parameter must never be null.");
    
    this.nameMatcher = new AnnotationNameMatcher(annotationNamespace, annotationName);
    this.annotationValue = annotationValue;
  }
  
  @Override
  public String matchedAnnotation(SNode node)
  {
    if(node instanceof SSpan || node instanceof SToken)
    {
      String match = nameMatcher.matchedAnnotation(node);
      if(match != null)
      {
        SAnnotation anno = node.getAnnotation(match);
        if(anno != null && annotationValue.equals(anno.getValue_STEXT()))
        {
          return match;
        }
      }
    }
    
    return null;

  }

  public AnnotationNameMatcher getNameMatcher()
  {
    return nameMatcher;
  }

  public void setNameMatcher(AnnotationNameMatcher nameMatcher)
  {
    this.nameMatcher = nameMatcher;
  }

  public String getAnnotationValue()
  {
    return annotationValue;
  }

  public void setAnnotationValue(String annotationValue)
  {
    this.annotationValue = annotationValue;
  }

  @Override
  public List<String> getRequiredAnnotationNames()
  {
    return nameMatcher.getRequiredAnnotationNames();
  }
  
  
  
}
