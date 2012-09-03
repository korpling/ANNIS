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

import java.util.Set;

/**
 * Base class for all Visualizer. This class sets some defaults, so you may
 * implement the null
 * {@link VisualizerPlugin#createComponent(annis.gui.visualizers.VisualizerInput)}
 * method for pure Vaadin component plugins.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public abstract class AbstractVisualizer implements VisualizerPlugin
{

  /**
   * Returns the character endocing for this particular Visualizer output. For
   * more information see
   * {@link javax.servlet.ServletResponse#setCharacterEncoding(String)}. Must be
   * overridden to change default "utf-8".
   *
   * @return the CharacterEncoding
   */
  public String getCharacterEncoding()
  {
    return "utf-8";
  }

  public String getContentType()
  {
    return "text/html";
  }

  /**
   * Return if this visualizer is using the complete text.
   */
  public boolean isUsingText()
  {
    return false;
  }

  @Override
  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
  }
}
