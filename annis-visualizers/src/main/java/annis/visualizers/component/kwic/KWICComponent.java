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

import annis.libgui.VisibleTokenAnnoChanger;
import annis.libgui.media.MediaController;
import annis.libgui.media.PDFController;
import annis.libgui.visualizers.VisualizerInput;
import annis.visualizers.component.grid.GridComponent;
import com.vaadin.server.VaadinSession;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;

/**
 * A component to visualize matched token and their context as "Keyword in
 * context"
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class KWICComponent extends GridComponent implements KWICInterface
{

  public static final String MAPPING_HIDDEN_ANNOS = "hidden_annos";

  private Set<String> hiddenTokenAnnos = null;

  public KWICComponent(VisualizerInput input,
    MediaController mediaController, PDFController pdfController)
  {
    super(input, mediaController, pdfController, false);

    getGrid().setShowCaption(false);

    if(input != null)
    {
      /* 
      KWIC has a special mapping that manipulates the global setting
      of selectable token annotation. That's why we have get the
      VisibleTokenAnnoChanger from the session and update the value
      according to our mapping.
      */
      if (input.getMappings().containsKey(MAPPING_HIDDEN_ANNOS))
      {
        hiddenTokenAnnos = new HashSet<String>(
          Arrays.asList(
            StringUtils.split(
              input.getMappings().getProperty(
                "hidden_annos"), ",")
          )
        );
      }

      if (hiddenTokenAnnos != null)
      {
        VaadinSession s = VaadinSession.getCurrent();

        if (s != null)
        {
          VisibleTokenAnnoChanger v = s.getAttribute(
            VisibleTokenAnnoChanger.class);

          SortedSet<String> newAnnos = new TreeSet<String>();
          for (String tokenAnnos : input.getVisibleTokenAnnos())
          {
            if (!hiddenTokenAnnos.contains(tokenAnnos))
            {
              newAnnos.add(tokenAnnos);
            }
          }

          v.updateVisibleToken(newAnnos);
          setVisibleTokenAnnos(newAnnos);

        }
      }
    }
  }

  @Override
  public void setSegmentationLayer(String segmentationName,
    Map<SNode, Long> markedAndCovered)
  {
    super.setSegmentationLayer(segmentationName, markedAndCovered);
    getGrid().setShowCaption(false);
  }
  
  

  @Override
  protected boolean isShowingTokenAnnotations()
  {
    // always show token annnotations
    return true;
  }

  @Override
  protected boolean isShowingSpanAnnotations()
  {
    // never show span annotations
    return false;
  }

  @Override
  protected boolean isHidingToken()
  {
    return false;
  }

  @Override
  protected boolean isTokenFirst()
  {
    return true;
  }

  @Override
  protected String getMainStyle()
  {
    return "kwic";
  }

  @Override
  public final void setVisibleTokenAnnos(Set<String> annos)
  {
    super.setVisibleTokenAnnos(annos);
    getGrid().setShowCaption(false);
  }

}
