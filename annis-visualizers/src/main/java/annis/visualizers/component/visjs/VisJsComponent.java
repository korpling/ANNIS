package annis.visualizers.component.visjs;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import annis.libgui.visualizers.VisualizerInput;







import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


@SuppressWarnings("serial")
@JavaScript(
		  {"VisJs_Connector.js",
		    "vaadin://jquery.js",
		    "vis.min.js"	    
		  })
@StyleSheet(
  {
	  	"vis.min.css"
  })



public class VisJsComponent extends AbstractJavaScriptComponent {	
	
	public VisJsComponent(){		
	//	 getState().value = value;
		List<TestNode> nodes = new ArrayList<TestNode>();
		TestNode node = new TestNode();
		node.id = "id_1";
		node.label = "test";
		node.x = 100;
		node.level = 1;
		
		TestNode node2 = new TestNode();
		node2.id = "id_2";
		node2.label = "test2";
		node2.x = 200;
		node2.level = 1;
		nodes.add(node2);
		
		nodes.add(node);
		
		GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String strNode = gson.toJson(nodes);
        
        callFunction("init", strNode);
        
		
	
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
