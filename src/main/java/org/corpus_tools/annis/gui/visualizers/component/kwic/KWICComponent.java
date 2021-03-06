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

import java.util.Map;
import java.util.Set;
import org.corpus_tools.annis.gui.media.MediaController;
import org.corpus_tools.annis.gui.media.PDFController;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.annis.gui.visualizers.component.grid.SingleGridComponent;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.core.SNode;

/**
 * A component to visualize matched token and their context as "Keyword in context"
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class KWICComponent extends SingleGridComponent implements KWICInterface {

  /**
   * 
   */
  private static final long serialVersionUID = -4337532971711640051L;

  public KWICComponent(VisualizerInput input, MediaController mediaController,
      PDFController pdfController, STextualDS text) {
    super(input, mediaController, pdfController, false, text);
  }

  @Override
  public void attach() {
    super.attach();

    getGrid().setShowCaption(false);
  }

  @Override
  protected boolean canShowEmptyTokenWarning() {
    return true;
  }

  @Override
  protected String getMainStyle() {
    return "kwic";
  }

  @Override
  protected boolean isAddingPlaybackRow() {
    return true;
  }

  @Override
  protected boolean isCoveredTokenMarked() {
    return true;
  }

  @Override
  protected boolean isFilteringMediaLayer() {
    return true;
  }

  @Override
  protected boolean isHidingToken() {
    return false;
  }

  @Override
  protected boolean isShowingSpanAnnotations() {
    // never show span annotations
    return false;
  }

  @Override
  protected boolean isShowingTokenAnnotations() {
    // always show token annnotations
    return true;
  }

  @Override
  protected boolean isTokenFirst() {
    return true;
  }

  @Override
  public boolean setSegmentationLayer(String segmentationName, Map<SNode, Long> markedAndCovered) {
    boolean result = super.setSegmentationLayer(segmentationName, markedAndCovered);
    getGrid().setShowCaption(false);

    return result;
  }

  @Override
  public final void setVisibleTokenAnnos(Set<String> annos) {
    super.setVisibleTokenAnnos(annos);
    getGrid().setShowCaption(false);
  }

}
