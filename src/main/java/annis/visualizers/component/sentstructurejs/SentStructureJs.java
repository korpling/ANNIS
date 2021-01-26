package annis.visualizers.component.sentstructurejs;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.ui.Panel;
import org.springframework.stereotype.Component;

@Component
public class SentStructureJs extends AbstractVisualizer { // NO_UCD (unused code)

  private static final long serialVersionUID = -5677329079488473862L;

  @Override
  public Panel createComponent(VisualizerInput visInput, VisualizationToggle visToggle) {
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
