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
 * Renders pdf documents with pdf.js. Fetches the whole pdf document, could be
 * slow since the underlying pdf.js do not support lazy loading yet.
 *
 * <p>For large pdfs it might be convenient, to set inner scrollbars with via
 * the mappings in the resolver_vis_tab file: {@code height:<Integer>}
 * </p>
 *
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class PDFFullVisualizer extends AbstractVisualizer<Panel> {

  @Override
  public String getShortName() {
    return "pdfdoc";
  }

  @Override
  public Panel createComponent(VisualizerInput input,
          VisualizationToggle visToggle) {
    Panel p = new Panel();
    p.setHeight(input.getMappings().getProperty("height", "-1") + "px");
    p.setContent(new PDFPanel(input, "-1"));
    return p;
  }
}
