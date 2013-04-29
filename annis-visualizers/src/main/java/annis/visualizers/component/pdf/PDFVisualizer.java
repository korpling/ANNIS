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

import annis.libgui.PDFPageHelper;
import annis.libgui.VisualizationToggle;
import annis.libgui.media.PDFController;
import annis.libgui.media.PDFViewer;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import annis.visualizers.component.grid.GridVisualizer;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a pdf visualizer based on pdf.js. Talking with pdf.js is done in
 * {@link PDFPanel}.
 *
 * <p>There are mappings available:
 * <ul>
 * <li>
 * For the page annotation:
 * <code>page:&lt;annotation_name&gt;</code>
 * </li>
 * <li> Setting a fixed height (recommended for the
 * {@link PDFFullVisualizer}):height:&lt;height in px&gt;</li>
 * </ul></p>
 *
 * <p>Since the pdf visualizer could be opened by clicking annotations in
 * {@link GridVisualizer}, this visualizer should set to the value "preloaded"
 * in the resolver_vis_map.tab file.</p>
 *
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class PDFVisualizer extends AbstractVisualizer<Panel> {

  private final Logger log = LoggerFactory.getLogger(PDFVisualizer.class);

  @Override
  public String getShortName() {
    return "pdf";
  }

  @Override
  public Panel createComponent(VisualizerInput input,
          VisualizationToggle visToggle) {

    PDFViewer pdfViewer = null;

    try {

      if (VaadinSession.getCurrent().getAttribute(PDFController.class) != null) {

        VaadinSession session = VaadinSession.getCurrent();
        PDFController pdfController = session.getAttribute(PDFController.class);
        pdfViewer = new PDFViewerImpl(input, visToggle);
        pdfController.addPDF(input.getId(), pdfViewer);
      }

    } catch (Exception ex) {
      log.error("could not create pdf vis", ex);
    }

    return (Panel) pdfViewer;
  }

  private class PDFViewerImpl extends Panel implements PDFViewer {

    VisualizerInput input;

    VisualizationToggle visToggle;

    PDFPanel pdfPanel;

    private boolean openedPage = false;

    public PDFViewerImpl(VisualizerInput input, VisualizationToggle visToggle) {
      this.visToggle = visToggle;
      this.input = input;
    }

    @Override
    public void openPDFPage(String page) {

      openedPage = true;

      initPDFPanel(page);

      if (!this.isVisible()) {
        // set visible status
        this.setVisible(true);
        visToggle.toggleVisualizer(true, null);
      }

      Notification.show("opening page " + page);
    }

    @Override
    public void openPDFViewer() {

      if (!openedPage) {

        String page = new PDFPageHelper(input).getMostLeftAndMostRightPageAnno();

        if (page == null) {
          initPDFPanel("-1");
          Notification.show("opening pdf");
        } else {
          initPDFPanel(page);
          Notification.show("opening pdf pages " + page);
        }
      } else {
        openedPage = false;
      }
    }

    private void initPDFPanel(String page) {

      if (pdfPanel != null) {
        pdfPanel = null;
      }

      pdfPanel = new PDFPanel(this.input, page);
      this.setContent(pdfPanel);
      this.setHeight(input.getMappings().getProperty("height", "-1") + "px");
    }
  }
}
