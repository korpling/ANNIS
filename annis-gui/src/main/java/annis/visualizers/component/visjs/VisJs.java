package annis.visualizers.component.visjs;


import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.ui.Panel;
import org.springframework.stereotype.Component;


@Component
public class VisJs extends AbstractVisualizer {



  /**
   * 
   */
  private static final long serialVersionUID = 6223666444705431088L;

  @Override
  public Panel createComponent(VisualizerInput visInput, VisualizationToggle visToggle) {

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
