/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.visualizers.component.grid;

import annis.libgui.VisualizationToggle;
import annis.libgui.media.MediaController;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.libgui.media.PDFController;
import com.vaadin.server.VaadinSession;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visualizes annotations of a spans.
 *
 *
 * Mappings: <br/>
 * <ul>
 * <li>
 * It is possible to specify the order of annotation layers in each grid. Use
 * <b>annos: anno_name1, anno_name2, anno_name3</b> to specify the order or
 * annotation layers. If <b>anno:</b> is used, additional annotation layers not
 * present in the list will not be visualized. If mappings is left empty, layers
 * will be ordered alphabetically.
 * </li>
 * <li>
 * <b>tok_anno:true</b> can be used to also display the annotations of the
 * token.
 * </li>
 * <li>
 * <b>hide_tok:true</b> switches the line with the token value off.
 * </li>
 * </ul>
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class GridVisualizer extends AbstractVisualizer<GridComponent>
{

  private static final Logger log = LoggerFactory.
    getLogger(GridVisualizer.class);

  @Override
  public String getShortName()
  {
    return "grid";
  }

  @Override
  public GridComponent createComponent(VisualizerInput visInput,
    VisualizationToggle visToggle)
  {
    MediaController mediaController = VaadinSession.getCurrent().getAttribute(
      MediaController.class);
    PDFController pdfController = VaadinSession.getCurrent().getAttribute(
      PDFController.class);
    GridComponent component = null;
    try
    {
      component = new GridComponent(visInput,
        mediaController, pdfController);
    }
    catch (Exception ex)
    {
      log.error("create {} failed", GridComponent.class.getName(), ex);
    }
    return component;
  }
}
