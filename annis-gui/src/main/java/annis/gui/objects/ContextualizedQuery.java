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

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @param <T>
 */
public class ContextualizedQuery<T extends ContextualizedQuery> extends Query<T>
{
  private int contextLeft;
  private int contextRight;
  private String segmentation;
  
  public ContextualizedQuery()
  {
    
  }

  public int getContextLeft()
  {
    return contextLeft;
  }

  public void setContextLeft(int contextLeft)
  {
    this.contextLeft = contextLeft;
  }

  public int getContextRight()
  {
    return contextRight;
  }

  public void setContextRight(int contextRight)
  {
    this.contextRight = contextRight;
  }

  public String getSegmentation()
  {
    return segmentation;
  }

  public void setSegmentation(String segmentation)
  {
    this.segmentation = segmentation;
  }
  
  public T left(int left)
  {
    this.contextLeft = left;
    return (T) this;
  }
  
  public T right(int right)
  {
    this.contextRight = right;
    return (T) this;
  }
  
  
}
