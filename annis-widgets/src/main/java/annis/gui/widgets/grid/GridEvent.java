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
package annis.gui.widgets.grid;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * An event has a right and left border (but might have holes)
 *
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class GridEvent implements Serializable
{
  private String id;
  private int left;
  private int right;
  private String value;
  private Long match;
  private List<String> coveredIDs;
  private Double startTime;
  private Double endTime;
  private boolean gap;
  private String textID;
  private String pageNumber;

  public GridEvent(String id, int left, int right, String value)
  {
    this.id = id;
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

  public Long getMatch()
  {
    return match;
  }

  public void setMatch(Long match)
  {
    this.match = match;
  }

  public Double getStartTime()
  {
    return startTime;
  }

  public void setStartTime(Double startTime)
  {
    this.startTime = startTime;
  }

  public Double getEndTime()
  {
    return endTime;
  }

  public void setEndTime(Double endTime)
  {
    this.endTime = endTime;
  }

  public boolean isGap()
  {
    return gap;
  }

  public void setGap(boolean gap)
  {
    this.gap = gap;
  }

  /**
   * Salt ID of the text this event belongs to.
   * @return
   */
  public String getTextID()
  {
    return textID;
  }

  public void setTextID(String textID)
  {
    this.textID = textID;
  }

  @Override
  public String toString()
  {
    return "" + id +  " -> " + value + " (" + left + "-" + right +")";
  }

  public String getPageNumber()
  {
    return pageNumber;
  }

  public void setPage(String pageNumber)
  {
    this.pageNumber = pageNumber;
  }
}
