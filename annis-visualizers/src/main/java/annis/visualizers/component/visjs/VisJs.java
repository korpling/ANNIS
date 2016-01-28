package annis.visualizers.component.visjs;


import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class VisJs extends AbstractVisualizer<VisJsComponent>{

	
//	private static final long serialVersionUID = 1L;

	@Override
	public String getShortName() 
	{
		return "visjs";
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
	    return false;
	  }

}
