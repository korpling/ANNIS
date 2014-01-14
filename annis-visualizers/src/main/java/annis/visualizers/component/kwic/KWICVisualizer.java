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
import annis.visualizers.component.grid.GridComponent;
import com.vaadin.server.VaadinSession;
import java.util.Set;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class KWICVisualizer extends AbstractVisualizer<KWICComponent>
{
  private static final Logger log = LoggerFactory.getLogger(KWICVisualizer.class);
  
  @Override
  public String getShortName()
  {
    return "kwic2";
  }

  @Override
  public KWICComponent createComponent(
    VisualizerInput visInput, VisualizationToggle visToggle)
  {
    MediaController mediaController = VaadinSession.getCurrent().getAttribute(
      MediaController.class);
    PDFController pdfController = VaadinSession.getCurrent().getAttribute(
      PDFController.class);
    KWICComponent component = null;
    try
    {
      component = new KWICComponent(visInput,
        mediaController, pdfController);
    }
    catch (Exception ex)
    {
      log.error("create {} failed",
        GridComponent.class.getName(), ex);
    }
    return component;
  }

  @Override
  public void setVisibleTokenAnnosVisible(KWICComponent component,
    Set<String> annos)
  {
    component.setVisibleTokenAnnos(annos);
  }

  
  
}
