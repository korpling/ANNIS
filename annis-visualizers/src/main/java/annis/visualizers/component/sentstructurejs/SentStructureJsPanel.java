package annis.visualizers.component.sentstructurejs;

import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * {@link SentStructureJsPanel} contains a {@link SentStructureJsComponent}.
 */
public class SentStructureJsPanel extends Panel {

  private static final long serialVersionUID = -8486858065402844608L;

  SentStructureJsPanel(VisualizerInput visInput) {
    this.setHeight("100%");
    this.setWidth("100%");

    SentStructureJsComponent sentstructurejsComponent = new SentStructureJsComponent(visInput);

    VerticalLayout layout = new VerticalLayout();
    layout.addComponent(sentstructurejsComponent);
    layout.getComponent(0).setId(sentstructurejsComponent.getContainerId());
    this.setContent(layout);
  }

}
