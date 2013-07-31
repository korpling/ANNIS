/*
 * Copyright 2012 SFB 632.
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
package annis.service.objects;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@XmlRootElement
public class SubgraphQuery implements Serializable
{
  private String segmentationLayer;
  private int left;
  private int right;
  private SaltURIGroupSet matches;
  private SubgraphFilter filter = SubgraphFilter.All;

  public String getSegmentationLayer()
  {
    return segmentationLayer;
  }

  public void setSegmentationLayer(String segmentationLayer)
  {
    this.segmentationLayer = segmentationLayer;
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

  public SaltURIGroupSet getMatches()
  {
    return matches;
  }

  public void setMatches(SaltURIGroupSet matches)
  {
    this.matches = matches;
  }

  public SubgraphFilter getFilter()
  {
    return filter;
  }

  public void setFilter(SubgraphFilter filter)
  {
    this.filter = filter;
  }

}
