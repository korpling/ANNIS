package annis.visualizers.component.visjs;


import com.vaadin.ui.Panel;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import net.xeoh.plugins.base.annotations.PluginImplementation;


@PluginImplementation
public class VisJs extends AbstractVisualizer<Panel>{


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6223666444705431088L;

	@Override
	public String getShortName() 
	{
		return "visjs";
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
	    return false;
	  }
	
	
	

}
