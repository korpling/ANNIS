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

import annis.libgui.VisualizationToggle;
import annis.libgui.media.MediaController;
import annis.libgui.media.PDFController;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.server.VaadinSession;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.Map;
import java.util.Set;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Key words in context visualizer (KWIC).
 *
 * @author Thomas Krause <krause@informatik.hu-berlin.>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class KWICVisualizer extends AbstractVisualizer<KWICInterface>
{
  private static final Logger log = LoggerFactory.getLogger(KWICVisualizer.class);
  
  @Override
  public String getShortName()
  {
    return "kwic";
  }

  @Override
  public KWICInterface createComponent(
    VisualizerInput visInput, VisualizationToggle visToggle)
  {
    MediaController mediaController = VaadinSession.getCurrent().getAttribute(
      MediaController.class);
    PDFController pdfController = VaadinSession.getCurrent().getAttribute(
      PDFController.class);
    
    EList<STextualDS> texts = visInput.getDocument().getSDocumentGraph().
      getSTextualDSs();

    // having the KWIC nested in a panel can slow down rendering
    if (texts.size() == 1)
    {
      // directly return the single non-nested KWIC panel
      return new KWICComponent(visInput,
        mediaController, pdfController, texts.get(0));
    }
    else
    {
      // return a more complicated implementation which can handle several texts
      return new KWICMultipleTextComponent(visInput, mediaController,
        pdfController);
    }
  }

  @Override
  public void setVisibleTokenAnnosVisible(KWICInterface component,
    Set<String> annos)
  {
    component.setVisibleTokenAnnos(annos);
  }

  
  @Override
  public void setSegmentationLayer(KWICInterface visualizerImplementation,
    String segmentationName, Map<SNode, Long> markedAndCovered)
  {
    visualizerImplementation.setSegmentationLayer(segmentationName,
      markedAndCovered);
  }
  
  
  
}
