package annis.visualizers.component.visjs;

import com.vaadin.ui.Component;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class VisJsVisualizer extends AbstractVisualizer<VisJsComponentTest>{

	
	//TODO change VisJsComponentTest to VisJsComponent after implementation
	private static final long serialVersionUID = 1L;

	@Override
	public String getShortName() 
	{
		return "visjs";
	}

	@Override
	public VisJsComponentTest createComponent(VisualizerInput visInput,
			VisualizationToggle visToggle) 
	{
		return new VisJsComponentTest(visInput);
	}

}
