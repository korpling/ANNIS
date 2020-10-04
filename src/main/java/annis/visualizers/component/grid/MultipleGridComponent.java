package annis.visualizers.component.grid;

import annis.libgui.media.MediaController;
import annis.libgui.media.PDFController;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
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
