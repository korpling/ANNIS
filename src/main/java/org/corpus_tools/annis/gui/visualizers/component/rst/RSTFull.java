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
package org.corpus_tools.annis.gui.visualizers.component.rst;

import org.corpus_tools.annis.gui.VisualizationToggle;
import org.corpus_tools.annis.gui.visualizers.AbstractVisualizer;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.springframework.stereotype.Component;

/**
 * Imitates the RST-diagrams from the RST-Tool (http://www.wagsoft.com/RSTTool/) for a complete
 * document.
 *
 * @see RST
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
@Component
public class RSTFull extends AbstractVisualizer { // NO_UCD (unused code)

  /**
   * 
   */
  private static final long serialVersionUID = 1112108378282427967L;

  @Override
  public RSTPanel createComponent(VisualizerInput visInput, VisualizationToggle visToggle) {
    return new RSTPanel(visInput);
  }

  @Override
  public String getShortName() {
    return "rstdoc";
  }

  @Override
  public boolean isUsingText() {
    return true;
  }

}
