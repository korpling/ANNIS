package annis.visualizers.component.sentstructurejs;

import com.vaadin.ui.Panel;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class SentStructureJs extends AbstractVisualizer<Panel> {

	private static final long serialVersionUID = -5677329079488473862L;

	@Override
	public String getShortName() {
		return "sentstructurejs";
	}

	@Override
	public Panel createComponent(VisualizerInput visInput, VisualizationToggle visToggle) {
		SentStructureJsPanel panel = new SentStructureJsPanel(visInput);
		panel.setHeight("100%");
		panel.setWidth("100%");
		return panel;
	}

	@Override
	public boolean isUsingText() {
		return false;
	}

}
