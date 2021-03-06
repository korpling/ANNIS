/*
 * Copyright 2014 SFB 632.
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
package org.corpus_tools.annis.gui.visualizers.component.kwic;

import com.vaadin.server.VaadinSession;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.annis.gui.VisualizationToggle;
import org.corpus_tools.annis.gui.media.MediaController;
import org.corpus_tools.annis.gui.media.PDFController;
import org.corpus_tools.annis.gui.visualizers.AbstractVisualizer;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.core.SNode;
import org.springframework.stereotype.Component;

/**
 * Key words in context visualizer (KWIC).
 * 
 * This visualizer has the same mappings as the
 * {@link org.corpus_tools.annis.gui.visualizers.component.grid.GridVisualizer}.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
@Component
public class KWICVisualizer extends AbstractVisualizer { // NO_UCD (unused code)
  /**
   * 
   */
  private static final long serialVersionUID = 1405603777567084847L;

  @Override
  public KWICInterface createComponent(VisualizerInput visInput, VisualizationToggle visToggle) {
    MediaController mediaController =
        VaadinSession.getCurrent().getAttribute(MediaController.class);
    PDFController pdfController = VaadinSession.getCurrent().getAttribute(PDFController.class);

    List<STextualDS> texts = visInput.getDocument().getDocumentGraph().getTextualDSs();

    // having the KWIC nested in a panel can slow down rendering
    if (texts.size() == 1) {
      // directly return the single non-nested KWIC panel
      return new KWICComponent(visInput, mediaController, pdfController, texts.get(0));
    } else {
      // return a more complicated implementation which can handle several texts
      return new KWICMultipleTextComponent(visInput, mediaController, pdfController);
    }
  }

  @Override
  public String getShortName() {
    return "kwic";
  }

  @Override
  public void setSegmentationLayer(com.vaadin.ui.Component component, String segmentationName,
      Map<SNode, Long> markedAndCovered) {
    if (component instanceof KWICInterface) {
      ((KWICInterface) component).setSegmentationLayer(segmentationName, markedAndCovered);
    }
  }

  @Override
  public void setVisibleTokenAnnosVisible(com.vaadin.ui.Component component, Set<String> annos) {
    if (component instanceof KWICInterface) {
      ((KWICInterface) component).setVisibleTokenAnnos(annos);
    }
  }

}
