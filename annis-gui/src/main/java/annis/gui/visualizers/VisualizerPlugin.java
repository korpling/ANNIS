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
import annis.gui.visualizers.component.KWICPanel;
import com.vaadin.Application;
import com.vaadin.ui.Component;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import net.xeoh.plugins.base.Plugin;

/**
 * Every visualizer must implement this interface. It' s also necessary to to
 * load this plugin by hand in {@link MainApp#initPlugins()}
 *
 * If you wish to implement an iframe visualizer you should extend the
 * {@link AbstractIFrameVisualizer} class, because this class already has
 * implemented the
 * {@link VisualizerPlugin#createComponent(annis.gui.visualizers.VisualizerInput)}
 * method.
 *
 * For the case of using Vaadin Component directly its recommended to extend the
 * {@link AbstractVisualizer} class. There you will have to implement the {@link VisualizerPlugin#createComponent(annis.gui.visualizers.VisualizerInput)
 * } method. Normally you need a inner or additional class which extends a
 * vaadin implementation of the {@link Component} interface. The
 * {@link KWICPanel} is an example for that.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public interface VisualizerPlugin<I extends Component> extends Plugin, Serializable
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
  public I createComponent(VisualizerInput visInput, Application application);

  /**
   * Checks if the Plugin needs the primary text source.
   */
  public boolean isUsingText();
  
  /**
   * If applicable change the visible token annotations.
   *
   * @param annos Which token annotations (qualified name) to show.
   */
  public void setVisibleTokenAnnosVisible(I visualizerImplementation, Set<String> annos);
  
  /**
   * If applicable change the displayed segmentation.
   * 
   * @param segmentationName 
   */
  public void setSegmentationLayer(I visualizerImplementation, 
    String segmentationName, Map<SNode, Long> markedAndCovered);
}
