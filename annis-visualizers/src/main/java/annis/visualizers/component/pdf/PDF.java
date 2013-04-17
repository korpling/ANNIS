/*
 * Copyright 2013 SFB 632.
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
package annis.visualizers.component.pdf;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.ui.Panel;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class PDF extends AbstractVisualizer<Panel> {

  @Override
  public String getShortName() {
    return "pdf";
  }

  @Override
  public Panel createComponent(VisualizerInput visInput,
          VisualizationToggle visToggle) {

    PDFPanel pdf =  new PDFPanel(visInput, visToggle);
    Panel p = new Panel();

    p.setContent(pdf);
    p.setSizeFull();

    return p;
  }
}
