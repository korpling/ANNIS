package annis.visualizers.component.visjs;


import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import annis.libgui.visualizers.VisualizerInput;
import annis.visualizers.component.grid.EventExtractor;
import annis.visualizers.component.kwic.KWICVisualizer;

import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.ExportFilter;
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



public class VisJsComponent extends AbstractJavaScriptComponent implements ExportFilter{	
	
	private String strNodes;
	private String strEdges;
	// a HashMap for storage of filter annotations with associated namespaces
	
	private Map<String, Set<String>> filterAnnotations;
	
	
	//private String visId;
	private static final Logger log = LoggerFactory.getLogger(VisJsComponent.class);
	
	public VisJsComponent(VisualizerInput visInput){	
		
			filterAnnotations = new HashMap<String, Set<String>>();
			System.out.println("hashMap initial:" + filterAnnotations);
			
			SDocument doc =  visInput.getDocument();
			List<String> annotations = EventExtractor.computeDisplayAnnotations(visInput, SNode.class);
			System.out.println("annotSize: " + annotations.size());
			
			
			for(String annotation: annotations)
			{ 	String anno = null;
				String ns = null;
				Set<String> namespaces = null;
			
				if (annotation.contains("::"))
				{
					String [] annotationParts = annotation.split("::");
					if (annotationParts.length == 2)
					{
						anno = annotationParts[1];
						ns = annotationParts[0];
						
					}
					else
					{
						//TODO
					}
					
				}
				else
				{
				anno = annotation;	
				}
				
				if (filterAnnotations.containsKey(anno))
				{
					 namespaces = filterAnnotations.get(anno);
				}
				else
				{
					namespaces = new HashSet<String>();
				}
				
				
				if (ns != null)
				{
					namespaces.add(ns);
				}
				
				filterAnnotations.put(anno, namespaces);
				
				System.out.println(annotation);
				
			}
			System.out.println("\n");
			
			System.out.println("hashMap fertig:" + filterAnnotations);
			
			
			
			VisJsVisualizer VisJsVisualizer = new VisJsVisualizer(doc, this);
			 
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
	      //it will be adjusted to the size of panel by VisJs_Connector.js
	      setHeight("400px");
	      setWidth("1000px");
	      getState().strNodes = strNodes;
	      getState().strEdges = strEdges;
	     
	    }


		@Override
		public boolean excludeNode(SNode node) {
					
			Set<SAnnotation> nodeAnnotations =  node.getAnnotations();
			
			for (SAnnotation nodeAnnotation : nodeAnnotations)
			{
				String nodeAnno = nodeAnnotation.getName();
				String nodeNs = nodeAnnotation.getNamespace();
				System.out.println(nodeNs +  "::" + nodeAnno);
				
				if (filterAnnotations.containsKey(nodeAnno))
				{
					if (filterAnnotations.get(nodeAnno).isEmpty())
					{
						return false;
					}
					else if (filterAnnotations.get(nodeAnno).contains(nodeNs))
					{
						return false;
					}
				}
				
			}
			
			return true;
		}


		@Override
		public boolean excludeRelation(SRelation relation) {
			// TODO Auto-generated method stub
			return false;
		}

}
