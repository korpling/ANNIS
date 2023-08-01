package org.corpus_tools.annis.gui.visualizers.component.sentstructurejs;

import com.vaadin.ui.Panel;
import org.corpus_tools.annis.gui.resultview.VisualizerPanel;
import org.corpus_tools.annis.gui.visualizers.AbstractVisualizer;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.springframework.stereotype.Component;

@Component
public class SentStructureJs extends AbstractVisualizer { // NO_UCD (unused code)

  private static final long serialVersionUID = -5677329079488473862L;

  @Override
  public Panel createComponent(VisualizerInput visInput, VisualizerPanel visPanel) {
    SentStructureJsPanel panel = new SentStructureJsPanel(visInput);
    panel.setHeight("100%");
    panel.setWidth("100%");
    return panel;
  }

  @Override
  public String getShortName() {
    return "sentstructurejs";
  }

  @Override
  public boolean isUsingText() {
    return false;
  }

}
