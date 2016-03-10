package annis.visualizers.component.visjs;

import annis.libgui.visualizers.VisualizerInput;

import com.vaadin.ui.Panel;

public class VisJsPanel extends Panel{
	
	VisJsPanel (VisualizerInput visInput){
		this.setHeight("400px");
		//this.setHeight("30%");
		this.setWidth("100%");
		this.setScrollLeft(10);
		this.setScrollTop(10);
		VisJsComponent visjsComponent = new VisJsComponent(visInput);		
		this.setContent(visjsComponent);
	}

}
