package annis.visualizers.component.grid;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import org.corpus_tools.salt.common.STextualDS;

import annis.libgui.media.MediaController;
import annis.libgui.media.PDFController;
import annis.libgui.visualizers.VisualizerInput;

public class MultipleGridComponent extends CssLayout implements GridComponent {

    public MultipleGridComponent(VisualizerInput visInput, MediaController mediaController, PDFController pdfController,
            boolean forceToken) {
        
        if (visInput != null) {
            for (STextualDS text : visInput.getDocument().getDocumentGraph().getTextualDSs()) {
                
                GridComponent g = new SingleGridComponent(visInput, mediaController, pdfController, forceToken, text);
                Label label = new Label(text.getName());
                label.setStyleName("text-name");
                addComponent(g);
                addComponent(label);
            }
        }
    }
}