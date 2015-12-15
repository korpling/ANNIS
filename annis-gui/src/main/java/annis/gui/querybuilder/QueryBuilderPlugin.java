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
package annis.gui.querybuilder;

import annis.gui.QueryController;
import annis.libgui.AnnisBaseUI;
import com.vaadin.ui.Component;
import java.io.Serializable;
import net.xeoh.plugins.base.Plugin;

/**
 * Every query builder must implement this interface. It' s also necessary to to
 * load this plugin by hand in {@link AnnisBaseUI#initPlugins()}
 *
 * @author Thomas Krause <b.pixeldrama@gmail.com>
 */
public interface QueryBuilderPlugin<I extends Component> extends Plugin, Serializable
{

  /**
   * Get the shorted name of the linguistic type of this visualizer ("partitur",
   * "tree", etc.)
   *
   * @return
   */
  public String getShortName();
  
  public String getCaption();

  /**
   * It is used by the ANNIS plugin system to generate something viewable for
   * Vaadin.
   *
   * @param controller
   */
  public I createComponent(QueryController controller);

}
