package annis.visualizers.component.visjs;



import annis.libgui.visualizers.VisualizerInput;



import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

public class VisJsComponentTest extends Panel{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public VisJsComponentTest(VisualizerInput visInput){
	
		    HorizontalLayout grid = new HorizontalLayout();
		   		 
		    
		    this.setHeight("-1px");
		    this.setWidth("100%");
		    grid.setHeight("-1px");
		    grid.setWidth("100%");
		
		final Panel panel = new Panel("Testpanel");
	


		// Create a layout inside the panel
		final FormLayout form = new FormLayout();


		// Have some margin around it.
		form.setMargin(true);


		// Add some components
		form.addComponent(new TextField("Hello!"));		


		// Set the layout as the root layout of the panel
		panel.setContent(form);
		
		
		grid.addComponent(panel);
	    setContent(grid);
	    grid.setExpandRatio(panel, 1.0f);
	}

	
}
