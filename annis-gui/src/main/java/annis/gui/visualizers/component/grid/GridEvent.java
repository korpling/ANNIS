/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.visualizers.component.grid;

import java.util.LinkedList;
import java.util.List;

/**
 * An event has a right and left border (but might have holes)
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class GridEvent
{
  private String id;
  private int left;
  private int right;
  private String value;
  private List<String> coveredIDs;
  
  public GridEvent(String id, int left, int right, String value)
  {
    this.left = left;
    this.right = right;
    this.value = value;
    
    this.coveredIDs = new LinkedList<String>();
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }
  
  public int getLeft()
  {
    return left;
  }

  public void setLeft(int left)
  {
    this.left = left;
  }

  public int getRight()
  {
    return right;
  }

  public void setRight(int right)
  {
    this.right = right;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public List<String> getCoveredIDs()
  {
    return coveredIDs;
  }
  
}
