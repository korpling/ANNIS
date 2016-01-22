package annis.visualizers.component.visjs;


import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import annis.libgui.visualizers.VisualizerInput;

import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.util.VisJsVisualizer;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;




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
	
	String strNodes;
	String strEdges;
	
	public VisJsComponent(VisualizerInput visInput){		

			SDocument doc =  visInput.getDocument();
			VisJsVisualizer VisJsVisualizer = new VisJsVisualizer(doc);
			 
			OutputStream osNodes = new ByteArrayOutputStream();
			OutputStream osEdges = new ByteArrayOutputStream();
			
			
			VisJsVisualizer.setNodeWriter(osNodes);
			VisJsVisualizer.setEdgeWriter(osEdges);
			VisJsVisualizer.buildJSON();				
			BufferedWriter bw;
			try {
				bw = VisJsVisualizer.getNodeWriter();
				bw.flush();	
				
				bw = VisJsVisualizer.getEdgeWriter();	
				bw.flush();		
		
				
				strNodes = osNodes.toString();
				strEdges = osEdges.toString();
				
				bw.close();
				this.setId(UUID.randomUUID().toString());
		        callFunction("init", strNodes, strEdges);
		       
	      
			}catch(IOException e){
				System.out.println(e.getStackTrace());
			}     
		
	
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
	    
	    @Override
	    protected VisJsState getState() {
	        return (VisJsState) super.getState();
	    }
	
	    @Override
	    public void attach() {
	      super.attach();

	      // set the state
	      getState().strNodes = strNodes;
	      getState().strEdges = strEdges;
	     
	    }
	
}
