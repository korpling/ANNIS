/*
 * Copyright 2014 SFB 632.
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

package annis.visualizers.component.kwic;

import annis.libgui.media.MediaController;
import annis.libgui.media.PDFController;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.ui.CssLayout;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.salt.common.STextualDS;

/**
 * Implementation that can display several texts but has slower rendering due
 * to an extra div.
 */
public class KWICMultipleTextComponent extends CssLayout implements KWICInterface
{
  List<KWICInterface> kwicPanels;

  public KWICMultipleTextComponent(VisualizerInput visInput,
    MediaController mediaController, PDFController pdfController)
  {
    this.kwicPanels = new LinkedList<KWICInterface>();
    if (visInput != null)
    {
      List<STextualDS> texts
        = visInput.getDocument().getDocumentGraph().getTextualDSs();
      for (STextualDS t : texts)
      {
        KWICComponent kwic
          = new KWICComponent(visInput, mediaController, pdfController, t);
        kwicPanels.add(kwic);
        addComponent(kwic);
      }
    }
  }

  @Override
  public void setVisibleTokenAnnos(Set<String> annos)
  {
    for (KWICInterface kwic : kwicPanels)
    {
      kwic.setVisibleTokenAnnos(annos);
    }
  }

  @Override
  public void setSegmentationLayer(String segmentationName,
    Map<String, Long> markedAndCovered)
  {
    for (KWICInterface kwic : kwicPanels)
    {
      kwic.setSegmentationLayer(segmentationName, markedAndCovered);
    }
  }
  
} // end class KWICMultipleTextImpl
