package annis.visualizers.component.visjs;

import com.vaadin.ui.Panel;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class VisJsDoc extends AbstractVisualizer<Panel>{

	private static final long serialVersionUID = -4818088208741889964L;

	@Override
	public String getShortName() 
	{
		return "visjsdoc";
	}

	@Override
	public Panel createComponent(VisualizerInput visInput,
			VisualizationToggle visToggle) 
	{
		VisJsPanel panel = new VisJsPanel(visInput);
		return panel;
	}
	
	@Override
  public boolean isUsingText()
  {
    return true;
  }

}
