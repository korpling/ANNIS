/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.visualizers;

import com.vaadin.ui.Component;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.annis.gui.resultview.VisualizerPanel;
import org.corpus_tools.salt.core.SNode;

/**
 * Every visualizer must implement this interface. It' s also necessary to to load this plugin by
 * hand in {@link org.corpus_tools.annis.gui.AnnisBaseUI#initPlugins()}
 *
 * If you wish to implement an iframe visualizer you should extend the
 * 
 * <pre>
 * AbstractIFrameVisualizer
 * </pre>
 * 
 * class, because this class already has implemented the
 * {@link VisualizerPlugin#createComponent(org.corpus_tools.annis.visualizers.visualizers.VisualizerInput)} method.
 *
 * For the case of using Vaadin Component directly its recommended to extend the
 * {@link AbstractVisualizer} class. There you will have to implement the
 * {@link VisualizerPlugin#createComponent(org.corpus_tools.annis.visualizers.visualizers.VisualizerInput) } method. Normally
 * you need a inner or additional class which extends a vaadin implementation of the
 * {@link Component} interface. The {@link org.corpus_tools.annis.gui.visualizers.component.KWICPanel} is an example
 * for that.
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public interface VisualizerPlugin extends Serializable {

  public final static String DEFAULT_VISUALIZER = "grid";

  /**
   * It is used by the ANNIS plugin system to generate something viewable for vaadin.
   *
   */
  public Component createComponent(VisualizerInput visInput, VisualizerPanel visToggle);

  /**
   * Get the shorted name of the linguistic type of this visualizer ("partitur", "tree", etc.)
   *
   * @return
   */
  public String getShortName();

  /**
   * Determines if this visualizer wants to use the original text.
   *
   * <p>
   * This is a convenient and very fast method for extracting the whole text of a document, and does
   * not map anything to salt. It is recommended to use the raw text over the
   * {@link VisualizerPlugin#isUsingText()} method, which indicates, that the visualizer needs the
   * whole document graph, which can slow down the user experience.
   * </p>
   *
   * <p>
   * It can be use in parallel with {@link #isUsingText()}, but makes in most cases no sense.
   * </p>
   *
   */
  public boolean isUsingRawText();

  /**
   * Checks if the Plugin needs the primary text source.
   */
  public boolean isUsingText();

  /**
   * If applicable change the displayed segmentation.
   *
   * @param segmentationName
   */
  public void setSegmentationLayer(Component visualizerImplementation, String segmentationName,
      Map<SNode, Long> markedAndCovered);

  /**
   * If applicable change the visible token annotations.
   *
   * @param annos Which token annotations (qualified name) to show.
   */
  public void setVisibleTokenAnnosVisible(Component visualizerImplementation, Set<String> annos);
}
