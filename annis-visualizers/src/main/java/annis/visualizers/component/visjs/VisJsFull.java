package annis.visualizers.component.visjs;

import com.vaadin.ui.Component;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class VisJsFull extends AbstractVisualizer<VisJsComponent>{



	@Override
	public String getShortName() 
	{
		return "visjsdoc";
	}

	@Override
	public VisJsComponent createComponent(VisualizerInput visInput,
			VisualizationToggle visToggle) 
	{
		return new VisJsComponent(visInput);
	}
	
	@Override
  public boolean isUsingText()
  {
    return true;
  }

}
