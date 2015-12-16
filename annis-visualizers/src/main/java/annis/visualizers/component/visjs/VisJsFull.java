package annis.visualizers.component.visjs;

import com.vaadin.ui.Component;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class VisJsFull extends AbstractVisualizer<VisJsComponentTest>{

	
	//TODO exchange VisJsComponentTest to VisJsComponent after implementation
	private static final long serialVersionUID = 1L;

	@Override
	public String getShortName() 
	{
		return "visjsdoc";
	}

	@Override
	public VisJsComponentTest createComponent(VisualizerInput visInput,
			VisualizationToggle visToggle) 
	{
		return new VisJsComponentTest(visInput);
	}
	
	@Override
  public boolean isUsingText()
  {
    return true;
  }

}
