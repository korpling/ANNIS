package org.corpus_tools.annis.visualizers.component.visjs;

import com.vaadin.ui.Panel;
import org.corpus_tools.annis.libgui.VisualizationToggle;
import org.corpus_tools.annis.libgui.visualizers.AbstractVisualizer;
import org.corpus_tools.annis.libgui.visualizers.VisualizerInput;
import org.springframework.stereotype.Component;


@Component
public class VisJsDoc extends AbstractVisualizer { // NO_UCD (unused code)

  private static final long serialVersionUID = -4818088208741889964L;

  @Override
  public Panel createComponent(VisualizerInput visInput, VisualizationToggle visToggle) {
    VisJsPanel panel = new VisJsPanel(visInput);
    return panel;
  }

  @Override
  public String getShortName() {
    return "visjsdoc";
  }

  @Override
  public boolean isUsingText() {
    return true;
  }

}
