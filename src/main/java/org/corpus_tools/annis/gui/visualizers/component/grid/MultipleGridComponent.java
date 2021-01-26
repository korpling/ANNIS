package org.corpus_tools.annis.gui.visualizers.component.grid;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import org.corpus_tools.annis.gui.media.MediaController;
import org.corpus_tools.annis.gui.media.PDFController;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.salt.common.STextualDS;

public class MultipleGridComponent extends CssLayout implements GridComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -2391073864143146539L;

  public MultipleGridComponent(VisualizerInput visInput, MediaController mediaController,
      PDFController pdfController, boolean forceToken) {

    setWidth("100%");
    setHeight("-1");

    if (visInput != null) {
      for (STextualDS text : visInput.getDocument().getDocumentGraph().getTextualDSs()) {

        GridComponent g =
            new SingleGridComponent(visInput, mediaController, pdfController, forceToken, text);
        Label label = new Label(text.getName());
        label.setStyleName("text-name");
        addComponent(g);
        addComponent(label);
      }
    }
  }
}
