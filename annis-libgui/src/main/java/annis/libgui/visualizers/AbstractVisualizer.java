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
package annis.libgui.visualizers;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.salt.core.SNode;

/**
 * Base class for all Visualizer. This class sets some defaults, so you may implement the
 * {@link VisualizerPlugin#createComponent(annis.gui.visualizers.VisualizerInput)} method for pure
 * Vaadin component plugins.
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public abstract class AbstractVisualizer<I extends Component>
    implements VisualizerPlugin<I>, FilteringVisualizerPlugin {

  /**
   * 
   */
  private static final long serialVersionUID = -1146320864910250586L;

  @Override
  public List<String> getFilteredNodeAnnotationNames(String toplevelCorpusName, String documentName,
      Map<String, String> mappings, UI ui) {
    return null;
  }

  @Override
  public boolean isUsingRawText() {
    return false;
  }

  /**
   * Return if this visualizer is using the complete text.
   */
  @Override
  public boolean isUsingText() {
    return false;
  }

  @Override
  public void setSegmentationLayer(I visualizerImplementation, String segmentationName,
      Map<SNode, Long> markedAndCovered) {}

  @Override
  public void setVisibleTokenAnnosVisible(I visualizerImplementation, Set<String> annos) {}


}
