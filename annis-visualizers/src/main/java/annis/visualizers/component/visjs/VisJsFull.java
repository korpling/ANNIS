package annis.visualizers.component.visjs;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class VisJsFull extends AbstractVisualizer<Panel>{



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
		//return new VisJsComponent(visInput);
	}
	
	@Override
  public boolean isUsingText()
  {
    return true;
  }

}
