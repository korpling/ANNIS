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
package annis.service.objects;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the configuration of the document browser. Every corpus may have
 * its own configuration. The configuration defines the visualizer and sorting
 * of the document browser as well as which annotations are displayed.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@XmlRootElement
public class DocumentBrowserConfig implements Serializable
{

  private Visualizer[] visualizers;

  private MetaDataColumn[] metaDataColumns;

  private OrderBy[] orderBy;

  public DocumentBrowserConfig()
  {

  }

  /**
   * @return the visualizers
   */
  public Visualizer[] getVisualizers()
  {
    return visualizers;
  }

  /**
   * @param visualizers the visualizers to set
   */
  public void setVisualizers(Visualizer[] visualizers)
  {
    this.visualizers = visualizers;
  }

  /**
   * @return the metaDataColumns
   */
  public MetaDataColumn[] getMetaDataColumns()
  {
    return metaDataColumns;
  }

  /**
   * @param metaDataColumns the metaDataColumns to set
   */
  public void setMetaDataColumns(MetaDataColumn[] metaDataColumns)
  {
    this.metaDataColumns = metaDataColumns;
  }

  /**
   * @return the orderBy
   */
  public OrderBy[] getOrderBy()
  {
    return orderBy;
  }

  /**
   * @param orderBy the orderBy to set
   */
  public void setOrderBy(
    OrderBy[] orderBy)
  {
    this.orderBy = orderBy;
  }
}
