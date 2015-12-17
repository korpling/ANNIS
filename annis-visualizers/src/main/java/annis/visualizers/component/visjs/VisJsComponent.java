package annis.visualizers.component.visjs;




import java.io.Serializable;
import java.util.ArrayList;

import annis.libgui.visualizers.VisualizerInput;





import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;


@SuppressWarnings("serial")
@JavaScript(
		  {"VisJs_Connector.js",
		    "vaadin://jquery.js",
		    "vis.min.js",
		    "mylibrary.js"		    
		  })
@StyleSheet(
  {
	  	"vis.min.css"
  })


public class VisJsComponent extends AbstractJavaScriptComponent {	
	
	public VisJsComponent(String value){		
		 getState().value = value;
	
	}
		  
	  
	  public interface ValueChangeListener extends Serializable {
	        void valueChange();
	    }
	    ArrayList<ValueChangeListener> listeners =
	            new ArrayList<ValueChangeListener>();
	    public void addValueChangeListener(
	                   ValueChangeListener listener) {
	        listeners.add(listener);
	    }
	    
	    public void setValue(String value) {
	        getState().value = value;
	    }
	    
	    public String getValue() {
	        return getState().value;
	    }

	    @Override
	    protected VisJsState getState() {
	        return (VisJsState) super.getState();
	    }
	
	
}
