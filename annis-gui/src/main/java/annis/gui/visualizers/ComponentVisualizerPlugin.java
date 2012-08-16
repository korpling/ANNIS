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

import com.vaadin.ui.Component;
import java.util.Set;

/**
 * All Vaadin Plugins must implement this Interface.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public interface ComponentVisualizerPlugin extends VisualizerPlugin, Component
{
  
  public void setVisualizerInput(VisualizerInput visInput); 
  
  /**
   * The implementation is optional.
   * 
   */
  public void setVisibleTokenAnnosVisible(Set<String> annos);
  
}
