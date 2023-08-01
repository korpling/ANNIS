package org.corpus_tools.annis.gui.visualizers.component.visjs;


import com.vaadin.ui.Panel;
import org.corpus_tools.annis.gui.resultview.VisualizerPanel;
import org.corpus_tools.annis.gui.visualizers.AbstractVisualizer;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.springframework.stereotype.Component;


@Component
public class VisJs extends AbstractVisualizer { // NO_UCD (unused code)



  /**
   * 
   */
  private static final long serialVersionUID = 6223666444705431088L;

  @Override
  public Panel createComponent(VisualizerInput visInput, VisualizerPanel visPanel) {

    VisJsPanel panel = new VisJsPanel(visInput);
    return panel;
  }

  @Override
  public String getShortName() {
    return "visjs";
  }

  @Override
  public boolean isUsingText() {
    return false;
  }

}
