/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.model;

import java.io.Serializable;
import java.util.Set;

/**
 * Parameters for a query that shows the result to the user
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class PagedResultQuery extends Query
{
  private int contextLeft;
  private int contextRight;
  private int offset;
  private int limit;
  private String segmentation;

  public PagedResultQuery()
  {
    
  }

  public PagedResultQuery(int contextLeft, int contextRight, int offset,
    int limit, String segmentation, String query,
    Set<String> corpora)
  {
    super(query, corpora);
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
    this.offset = offset;
    this.limit = limit;
    this.segmentation = segmentation;
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

  public int getOffset()
  {
    return offset;
  }

  public void setOffset(int offset)
  {
    this.offset = offset;
  }

  public int getLimit()
  {
    return limit;
  }

  public void setLimit(int limit)
  {
    this.limit = limit;
  }

  public String getSegmentation()
  {
    return segmentation;
  }

  public void setSegmentation(String segmentation)
  {
    this.segmentation = segmentation;
  }
  
}
