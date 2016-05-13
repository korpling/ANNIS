package annis.visualizers.component.visjs;

import org.corpus_tools.salt.util.VisJsVisualizer;

import annis.libgui.visualizers.VisualizerInput;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalSplitPanel;


@StyleSheet(
		  {
			  	"visjs_legend.css"
			  	
		  })


public class VisJsPanel extends Panel{
	
	VisJsPanel (VisualizerInput visInput){
		this.setHeight("700px");
		this.setWidth("100%");
		//this.setScrollLeft(10);
		//this.setScrollTop(10);
		
		
		VerticalSplitPanel vsplit = new VerticalSplitPanel();
		VisJsComponent visjsComponent = new VisJsComponent(visInput);		
		vsplit.setFirstComponent(visjsComponent);
		vsplit.setSplitPosition((float) 90.0);
		HorizontalLayout hlayout = new HorizontalLayout();
		hlayout.setHeight("100%");
		hlayout.setWidth("100%");
		//hlayout.setSizeUndefined();
		hlayout.setSpacing(false);

		TextField tfToken = new TextField();
		tfToken.setValue("token");
		tfToken.setReadOnly(true);		
		tfToken.addStyleName("legend");
		tfToken.addStyleName("legend-token");	
		
		TextField tfSpan = new TextField();
		tfSpan.setValue("spanning node");
		tfSpan.setReadOnly(true);		
		tfSpan.addStyleName("legend");
		tfSpan.addStyleName("legend-span");	
		
		TextField tfStruct = new TextField();
		tfStruct.setValue("structure node");
		tfStruct.setReadOnly(true);		
		tfStruct.addStyleName("legend");
		tfStruct.addStyleName("legend-struct");	
		
		
		
		hlayout.addComponent(tfToken);
		hlayout.addComponent(tfSpan);
		hlayout.addComponent(tfStruct);
		
		hlayout.setExpandRatio(tfToken, 0.2f);
		hlayout.setExpandRatio(tfSpan, 0.2f);
		hlayout.setExpandRatio(tfStruct, 0.6f);
		
		vsplit.addComponent(hlayout);
		
		this.setContent(vsplit);
	}

}
