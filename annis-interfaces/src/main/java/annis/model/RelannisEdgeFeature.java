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
package annis.model;

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_RELANNIS_EDGE;
import java.io.Serializable;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SRelation;

/**
 * Common Features included in the Salt graph that are available
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class RelannisEdgeFeature implements Serializable
{
  static final long serialVersionUID = 0L;

  private long pre;

  private long componentID;

  private Long artificialDominancePre;

  private Long artificialDominanceComponent;

  public long getPre()
  {
    return pre;
  }

  public void setPre(long pre)
  {
    this.pre = pre;
  }

  public long getComponentID()
  {
    return componentID;
  }

  public void setComponentID(long componentID)
  {
    this.componentID = componentID;
  }

  /**
   * The pre order inside of the corresponding artificial relANNIS component if
   * this was a dominance relation.
   *
   * @return
   */
  public Long getArtificialDominancePre()
  {
    return artificialDominancePre;
  }

  public void setArtificialDominancePre(Long artificialDominancePre)
  {
    this.artificialDominancePre = artificialDominancePre;
  }

  /**
   * The ID of the corresponding artificial relANNIS component if this was a
   * dominance relation.
   *
   * @return
   */
  public Long getArtificialDominanceComponent()
  {
    return artificialDominanceComponent;
  }

  public void setArtificialDominanceComponent(Long artificialDominanceComponent)
  {
    this.artificialDominanceComponent = artificialDominanceComponent;
  }

  @Override
  public String toString()
  {
    return "["
      + "pre=" + pre + ","
      + "componentID=" + componentID + ","
      + "artificialDominancePre=" + artificialDominancePre + ","
      + "artificialDominanceComponent=" + artificialDominanceComponent
      + "]";

  }

  public static RelannisEdgeFeature extract(SRelation rel)
  {
    RelannisEdgeFeature featRelation = null;
    SFeature sfeatRelation = rel.getFeature(ANNIS_NS + "::" + FEAT_RELANNIS_EDGE);
    if (sfeatRelation != null)
    {
      featRelation = (RelannisEdgeFeature) sfeatRelation.getValue();
    }
    return featRelation;
  }
  
}
