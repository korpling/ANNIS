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

package org.corpus_tools.annis.visualizers.component.kwic;

import com.vaadin.ui.CssLayout;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.annis.libgui.media.MediaController;
import org.corpus_tools.annis.libgui.media.PDFController;
import org.corpus_tools.annis.libgui.visualizers.VisualizerInput;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.core.SNode;

/**
 * Implementation that can display several texts but has slower rendering due to an extra div.
 */
public class KWICMultipleTextComponent extends CssLayout implements KWICInterface {
  /**
   * 
   */
  private static final long serialVersionUID = 8245889282810831091L;
  List<KWICInterface> kwicPanels;

  public KWICMultipleTextComponent(VisualizerInput visInput, MediaController mediaController,
      PDFController pdfController) {
    setWidth("100%");
    setHeight("-1");
    this.kwicPanels = new LinkedList<KWICInterface>();
    if (visInput != null) {
      List<STextualDS> texts = visInput.getDocument().getDocumentGraph().getTextualDSs();
      for (STextualDS t : texts) {
        KWICComponent kwic = new KWICComponent(visInput, mediaController, pdfController, t);
        kwicPanels.add(kwic);
        addComponent(kwic);
      }
    }
  }

  @Override
  public boolean setSegmentationLayer(String segmentationName, Map<SNode, Long> markedAndCovered) {
    boolean result = false;
    for (KWICInterface kwic : kwicPanels) {
      if (kwic.setSegmentationLayer(segmentationName, markedAndCovered)) {
        result = true;
      }
    }
    return result;
  }

  @Override
  public void setVisibleTokenAnnos(Set<String> annos) {
    for (KWICInterface kwic : kwicPanels) {
      kwic.setVisibleTokenAnnos(annos);
    }
  }

} // end class KWICMultipleTextImpl
