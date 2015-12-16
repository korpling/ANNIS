package annis.visualizers.component.visjs;




import annis.libgui.visualizers.VisualizerInput;



import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;


@SuppressWarnings("serial")
@JavaScript(
		  {
		    "vaadin://jquery.js",
		    "../resources/annis/visualizers/visjs/vis.min.js",
		    "../resources/annis/visualizers/visjs/VisJsComponent.js"
		  })
@StyleSheet(
  {
	  	"../resources/annis/visualizers/visjs/vis.min.css"
  })


public class VisJsComponent extends AbstractJavaScriptComponent {	
	
	public VisJsComponent(String xhtml){		
		 getState().xhtml = xhtml;
	
	}
	


	  @Override
	  protected VisJsState getState() {
	    return (VisJsState) super.getState();
	  }
	
	
}
