/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.objects;

import java.util.Objects;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ContextualizedQuery extends Query
{
  private int leftContext;
  private int rightContext;
  private String segmentation;
  
  public ContextualizedQuery()
  {
    
  }

  public int getLeftContext()
  {
    return leftContext;
  }

  public void setLeftContext(int leftContext)
  {
    this.leftContext = leftContext;
  }

  public int getRightContext()
  {
    return rightContext;
  }

  public void setRightContext(int rightContext)
  {
    this.rightContext = rightContext;
  }

  public String getSegmentation()
  {
    return segmentation;
  }

  public void setSegmentation(String segmentation)
  {
    this.segmentation = segmentation;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(getCorpora(), getQuery(), leftContext, rightContext, segmentation);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final ContextualizedQuery other = (ContextualizedQuery) obj;
    return 
      Objects.equals(getQuery(), other.getQuery())
      && Objects.equals(getCorpora(), other.getCorpora())
      && Objects.equals(leftContext, other.leftContext)
      && Objects.equals(rightContext, other.rightContext)
      && Objects.equals(segmentation, other.segmentation);
  }
}
