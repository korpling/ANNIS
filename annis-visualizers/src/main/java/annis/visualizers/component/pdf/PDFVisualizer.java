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
import annis.libgui.media.PDFController;
import annis.libgui.media.PDFViewer;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class PDFVisualizer extends AbstractVisualizer<Layout> {

  private final Logger log = LoggerFactory.getLogger(PDFVisualizer.class);

  @Override
  public String getShortName() {
    return "pdf";
  }

  @Override
  public Layout createComponent(VisualizerInput input,
          VisualizationToggle visToggle) {

    Layout wrapper = new VerticalLayout();

    try {

      if (VaadinSession.getCurrent().getAttribute(PDFController.class) != null) {

        VaadinSession session = VaadinSession.getCurrent();
        PDFController pdfController = session.getAttribute(PDFController.class);

        PDFViewer pdfViewer = new PDFViewerImpl(wrapper, input, visToggle);

        pdfController.addPDF(input.getId(), pdfViewer);
      }

    } catch (Exception ex) {
      log.error("could not create pdf vis", ex);
    }

    return wrapper;
  }

  private class PDFViewerImpl implements PDFViewer {

    Layout wrapper;

    VisualizerInput input;

    VisualizationToggle visToggle;

    public PDFViewerImpl(Layout wrapper, VisualizerInput input,
            VisualizationToggle visToggle) {
      this.wrapper = wrapper;
      this.visToggle = visToggle;
      this.input = input;
    }

    @Override
    public void openPDF(String page) {

      PDFPanel pdf = new PDFPanel(this.input, Integer.parseInt(page));
      visToggle.toggleVisualizer(true, null);
      wrapper.addComponent(pdf);

      Notification.show("open page " + page);
    }
  }
}
