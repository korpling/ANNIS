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
package annis.gui.visualizers;

import annis.gui.MainApp;
import com.vaadin.ui.Component;
import java.util.Set;
import net.xeoh.plugins.base.Plugin;

/**
 * Every visualizer must implement this interface. It' s also necessary to to
 * load this plugin by hand in {@link MainApp#initPlugins()}
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public interface VisualizerPlugin extends Plugin
{

  /**
   * Get the shorted name of the linguistic type of this visualizer ("partitur",
   * "tree", etc.)
   *
   * @return
   */
  public String getShortName();

  /**
   * It is used by the ANNIS plugin system to generate something viewable for
   * vaadin.
   *
   */
  public Component createComponent(VisualizerInput visInput);

  /**
   * The implementation is optional.
   *
   */
  public void setVisibleTokenAnnosVisible(Set<String> annos);
}
