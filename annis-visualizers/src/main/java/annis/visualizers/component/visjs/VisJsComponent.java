package annis.visualizers.component.visjs;


import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import annis.libgui.visualizers.VisualizerInput;
import annis.visualizers.component.kwic.KWICVisualizer;

import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.util.VisJsVisualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.Scrollable;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.AbstractJavaScriptComponent;





@JavaScript(
		  {"VisJs_Connector.js",
		    "vaadin://jquery.js",
		    "vis.min.js"	    
		  })
@StyleSheet(
  {
	  	"vis.min.css"
  })



public class VisJsComponent extends AbstractJavaScriptComponent{	
	
	private String strNodes;
	private String strEdges;
	
	
	//private String visId;
	private static final Logger log = LoggerFactory.getLogger(VisJsComponent.class);
	
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
		       
	      
			}catch(IOException e){
				System.out.println(e.getStackTrace());
			}     
		
	
	}
		  
	  
	    @Override
	    protected VisJsState getState() {
	        return (VisJsState) super.getState();
	    }
	
	    @Override
	    public void attach() {
	      super.attach();
	      //set an initial size
	      //it will be adjust to the size of panel by VisJs_Connector.js
	      setHeight("400px");
	      setWidth("1000px");
	      getState().strNodes = strNodes;
	      getState().strEdges = strEdges;
	     
	    }

}
