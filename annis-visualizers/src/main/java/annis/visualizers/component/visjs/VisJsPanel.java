package annis.visualizers.component.visjs;

import annis.libgui.visualizers.VisualizerInput;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalSplitPanel;

public class VisJsPanel extends Panel{
	
	VisJsPanel (VisualizerInput visInput){
		this.setHeight("700px");
		this.setWidth("100%");
		//this.setScrollLeft(10);
		//this.setScrollTop(10);
		
		
		VerticalSplitPanel vsplit = new VerticalSplitPanel();
		VisJsComponent visjsComponent = new VisJsComponent(visInput);		
		vsplit.setFirstComponent(visjsComponent);
		vsplit.addComponent(new Label("Lower panel"));
		vsplit.setSplitPosition((float) 90.0);
		this.setContent(vsplit);
	}

}
