package annis.visualizers.component.visjs;

import annis.libgui.visualizers.VisualizerInput;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;


@StyleSheet(
		  {
			  	"visjs_legend.css"
			  	
		  })

/**
 * {@link VisJsPanel} contains a {@link VisJsComponent}.
 */
public class VisJsPanel extends Panel{
	
	
	private static final long serialVersionUID = 6231027893280529982L;
	
	VisJsPanel (VisualizerInput visInput){
		this.setHeight("700px");
		this.setWidth("100%");
		
		
		HorizontalSplitPanel hsplit = new HorizontalSplitPanel();		
		hsplit.setSplitPosition(10.0f);
		
		
		VerticalLayout vlayout = new VerticalLayout();
		vlayout.setHeight("100%");
		vlayout.setWidth("100%");
		vlayout.setSpacing(false);
		
		
		TextArea legendLabel = new TextArea();
		legendLabel.setValue("Legend:");
		legendLabel.setReadOnly(true);	
		legendLabel.addStyleName("borderless");
		legendLabel.addStyleName("legend");
		legendLabel.addStyleName("legend-label");				
		
		TextArea tfStruct = new TextArea();
		tfStruct.setValue("structure" + "\n" + "node");
		tfStruct.setReadOnly(true);			
		tfStruct.addStyleName("legend");
		tfStruct.addStyleName("legend-struct");				
		
		TextArea tfSpan = new TextArea();
		tfSpan.setValue("spanning" + "\n" +"node");
		tfSpan.setReadOnly(true);			
		tfSpan.addStyleName("legend");
		tfSpan.addStyleName("legend-span");		
		
		TextArea tfToken = new TextArea();
		tfToken.setValue("token");
		tfToken.setReadOnly(true);			
		tfToken.addStyleName("legend");
		tfToken.addStyleName("legend-token");	
		
		vlayout.addComponent(legendLabel);		
		vlayout.addComponent(tfStruct);		
		vlayout.addComponent(tfSpan);
		vlayout.addComponent(tfToken);	
		
		
		vlayout.setExpandRatio(legendLabel, 0.10f);		
		vlayout.setExpandRatio(tfStruct, 0.10f);		
		vlayout.setExpandRatio(tfSpan, 0.10f);
		vlayout.setExpandRatio(tfToken, 0.70f);
		
		hsplit.setFirstComponent(vlayout);
		
		VisJsComponent visjsComponent = new VisJsComponent(visInput);		
		hsplit.addComponent(visjsComponent);
		
		this.setContent(hsplit);
		
	}

}
