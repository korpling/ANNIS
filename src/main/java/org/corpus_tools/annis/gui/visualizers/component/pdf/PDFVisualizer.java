/*
 * Copyright 2013 SFB 632.
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
package org.corpus_tools.annis.gui.visualizers.component.pdf;

import static org.corpus_tools.annis.gui.PDFPageHelper.PAGE_NO_VALID_NUMBER;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Panel;
import org.corpus_tools.annis.gui.PDFPageHelper;
import org.corpus_tools.annis.gui.VisualizationToggle;
import org.corpus_tools.annis.gui.media.PDFController;
import org.corpus_tools.annis.gui.media.PDFViewer;
import org.corpus_tools.annis.gui.visualizers.AbstractVisualizer;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.annis.gui.visualizers.component.grid.GridVisualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Creates a pdf visualizer based on pdf.js. Talking with pdf.js is done in {@link PDFPanel}.
 *
 * <p>
 * There are mappings available:
 * <ul>
 * <li>Setting a fixed height (recommended for the {@link PDFFullVisualizer}):height:&lt;height in
 * px&gt;</li>
 * </ul>
 * </p>
 *
 * <p>
 * Since the pdf visualizer could be opened by clicking annotations in {@link GridVisualizer}, this
 * visualizer should set to the value "preloaded" in the resolver_vis_map.tab file.
 * </p>
 *
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
@Component
public class PDFVisualizer extends AbstractVisualizer { // NO_UCD (unused code)

  private static class PDFViewerImpl extends Panel implements PDFViewer {

    /**
     * 
     */
    private static final long serialVersionUID = -2541354371319522012L;

    VisualizerInput input;

    VisualizationToggle visToggle;

    PDFPanel pdfPanel;

    public PDFViewerImpl(VisualizerInput input, VisualizationToggle visToggle) {
      this.visToggle = visToggle;
      this.input = input;
    }

    private void initPDFPanel(String page) {

      if (pdfPanel != null) {
        pdfPanel = null;
      }

      pdfPanel = new PDFPanel(this.input, page);
      this.setContent(pdfPanel);
      this.setHeight(input.getMappings().getOrDefault("height", "-1") + "px");
    }

    @Override
    public void openPDFPage(String page) {

      if (PAGE_NO_VALID_NUMBER.equals(page)) {
        page = new PDFPageHelper(input).getMostLeftAndMostRightPageAnno();
      }

      page = (page == null) ? PAGE_NO_VALID_NUMBER : page;

      initPDFPanel(page);

      if (!this.isVisible()) {
        // set visible status
        this.setVisible(true);
        visToggle.toggleVisualizer(true, null);
      }

    }
  }

  /**
   * 
   */
  private static final long serialVersionUID = -5208334507204533281L;

  private final Logger log = LoggerFactory.getLogger(PDFVisualizer.class);

  @Override
  public Panel createComponent(VisualizerInput input, VisualizationToggle visToggle) {

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

  @Override
  public String getShortName() {
    return "pdf";
  }

}
