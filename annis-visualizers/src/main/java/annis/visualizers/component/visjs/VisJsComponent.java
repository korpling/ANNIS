package annis.visualizers.component.visjs;


import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;
import static annis.visualizers.component.grid.GridComponent.MAPPING_ANNOS_KEY;
import static annis.visualizers.component.grid.GridComponent.MAPPING_ANNO_REGEX_KEY;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import annis.libgui.MatchedNodeColors;
import annis.libgui.visualizers.VisualizerInput;
import annis.visualizers.component.grid.EventExtractor;

import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.ExportFilter;
import org.corpus_tools.salt.util.StyleImporter;
import org.corpus_tools.salt.util.VisJsVisualizer;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;





@JavaScript(
		  {"VisJs_Connector.js",
			"vaadin://jquery.js",
		    "vis.min.js"
		  })
@StyleSheet(
  {
	  "vaadin://themes/annis-visualizer-theme/visjs/vis.min.css"
	  	
  })



/**
 * 
 * @author irina
 *
 */

public class VisJsComponent extends AbstractJavaScriptComponent implements ExportFilter, StyleImporter{	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9006240832319119407L;
	private String strNodes;
	private String strEdges;
	//public static final String MAPPING_EDGES = "edges";
	public static final String POINTING_REL_ANNOS = "pointingRelAnnos";
	public static final String SPANNING_REL_ANNOS = "spanningRelAnnos";
	public static final String DOMINANCE_REL_ANNOS = "dominanceRelAnnos";
	//public static final String EMPTY_NAMESPACE = "";
	
	
	
	// a HashMap for storage of node annotations for export filter with associated namespaces	
	private Map<String, Set<String>> filterNodeAnnotations = new HashMap<String, Set<String>>();
	// a HashMap for storage edge annotations for export filter with associated namespaces	
	//private Map<String, Set<String>> filterEdgeAnnotations = new HashMap<String, Set<String>>();
	
	// a HashMap to storage of annotations of pointing relations for export filter with associated namespaces	
	private Map<String, Set<String>> filterPointingRelAnnotations = new HashMap<String, Set<String>>();
	
	// a HashMap to storage of annotations of pointing relations for export filter with associated namespaces	
	private Map<String, Set<String>> filterSpanningRelAnnotations = new HashMap<String, Set<String>>();
		
	// a HashMap to storage of annotations of pointing relations for export filter with associated namespaces	
	private Map<String, Set<String>> filterDominanceRelAnnotations = new HashMap<String, Set<String>>();
	

	private static String nodeAnnosConfiguration;
	//private static String nodeAnnosRegexConfiguration;
	//private static String edgeAnnosConfiguration;
	
	private static String pointingRelAnnosConfiguration;
	private static String spanningRelAnnosConfiguration;
	private static String dominanceRelAnnosConfiguration;
	
	
	
	
	
	//private static final Logger log = LoggerFactory.getLogger(VisJsComponent.class);
	
	SDocument doc;
	/**
	 * Creates a new VisJsComponent instance.
	 * 
	 * @param visInput The input for the visualizer.
	 */	
	public VisJsComponent(VisualizerInput visInput){
		
			nodeAnnosConfiguration = visInput.getMappings().getProperty(MAPPING_ANNOS_KEY);
			//nodeAnnosRegexConfiguration = visInput.getMappings().getProperty(MAPPING_ANNO_REGEX_KEY);
			//edgeAnnosConfiguration = visInput.getMappings().getProperty(MAPPING_EDGES);
			
			
			pointingRelAnnosConfiguration = visInput.getMappings().getProperty(POINTING_REL_ANNOS);
			spanningRelAnnosConfiguration = visInput.getMappings().getProperty(SPANNING_REL_ANNOS);
			dominanceRelAnnosConfiguration = visInput.getMappings().getProperty(DOMINANCE_REL_ANNOS);
			
			
			
			for(int i = 0; i < 4; i++){
				fillFilterAnnotations(visInput, i);
			}
						
			
			SDocument doc =  visInput.getDocument();
			this.doc = doc;
			
			VisJsVisualizer VisJsVisualizer = new VisJsVisualizer(doc,this, this);
			 
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
	
	// fills export filter annotations for the specified type (0 -> nodes; 1 -> pointing relations, 2 -> spanning relations, 3 -> dominance relations)
	private void fillFilterAnnotations(VisualizerInput visInput, int type){
		Map<String, Set<String>> myFilterAnnotations;
		List<String> annotations;
		if(type == 0) {
			annotations = EventExtractor.computeDisplayAnnotations(visInput, SNode.class);	
			System.out.println("NodeAnno all: " + annotations);
			myFilterAnnotations = filterNodeAnnotations;
		}
		else if (type == 1){
			annotations = computeDisplayedRelAnnotations(visInput, pointingRelAnnosConfiguration, SPointingRelation.class);	
			System.out.println("PointingAnno all: " + annotations);
			myFilterAnnotations = filterPointingRelAnnotations;
		}
		else if (type == 2){
			annotations = computeDisplayedRelAnnotations(visInput, spanningRelAnnosConfiguration, SSpanningRelation.class);	
			System.out.println("SpanningAnno all: " + annotations);
			myFilterAnnotations = filterSpanningRelAnnotations;
		}
		else {
			annotations = computeDisplayedRelAnnotations(visInput, dominanceRelAnnosConfiguration, SDominanceRelation.class);	
			System.out.println("DominanceAnno all: " + annotations);
			myFilterAnnotations = filterDominanceRelAnnotations;
		}	
		
		
			
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
						throw new IllegalArgumentException();
					}
					
				}
				else
				{
				anno = annotation;	
				}
				
				
				if (myFilterAnnotations.containsKey(anno))
				{
					 namespaces = myFilterAnnotations.get(anno);
				}
				else
				{
					namespaces = new HashSet<String>();
				}
				
				
				if (ns != null)
				{
					namespaces.add(ns);
				}
				
				myFilterAnnotations.put(anno, namespaces);
				
				System.out.println("myFilterAnnotation: \t" +type + "\t" + myFilterAnnotations);
				
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
	     // setHeight("600px");
	      //setWidth("1000px");
	      setHeight("100%");
	      setWidth("100%");
	      getState().strNodes = strNodes;
	      getState().strEdges = strEdges;
	     
	    }
	    
	    
	/*   public  List<String> computeDisplayedRelAnnotations(VisualizerInput input){
	    	 if (input == null) {
			      return new ArrayList<>();
			    }
	    	 
	    	List<String> relDisplayedAnnotations = new ArrayList<String>();
		
	    	
	    	relDisplayedAnnotations.addAll(computeDisplayedRelAnnotations(input, pointingRelAnnosConfiguration, SPointingRelation.class));
	   		relDisplayedAnnotations.addAll(computeDisplayedRelAnnotations(input, spanningRelAnnosConfiguration, SSpanningRelation.class));
	   		relDisplayedAnnotations.addAll(computeDisplayedRelAnnotations(input, dominanceRelAnnosConfiguration, SDominanceRelation.class));
	    			
	    	
	    	return relDisplayedAnnotations;
	    	
	    }*/
		
		
		
		/**
		   * Returns the annotations to display according to the mappings configuration.
		   *
		   * This will check the "edge" parameter for determining the annotations to display. 
		   * It also iterates over all nodes of the graph
		   * matching the type.
		   *
		   * @param input The input for the visualizer.
		   * @param type Which type of relations to include
		   * @return
		   */
		  public static List<String> computeDisplayedRelAnnotations(VisualizerInput input, 
				  								String relAnnosConfiguration, Class<? extends SRelation> type) {
		    if (input == null) {
		      return new LinkedList<>();
		    }

		    SDocumentGraph graph = input.getDocument().getDocumentGraph();

		    Set<String> annotationPool = getRelationLevelSet(graph, null, type);
		    
		    System.out.println(relAnnosConfiguration  + "\t annoPool: "+ annotationPool);
		    List<String> confAnnotations = new LinkedList<>(annotationPool);
		  
		    
		    if (relAnnosConfiguration != null && relAnnosConfiguration.trim().length() > 0) {
		      String[] confSplit = relAnnosConfiguration.split(",");
		      confAnnotations.clear();
		      for (String entry : confSplit) {
		        entry = entry.trim();
		        // is regular expression?
		        if (entry.startsWith("/") && entry.endsWith("/")) {
		          // go over all remaining items in our pool of all annotations and
		          // check if they match
		          Pattern regex = Pattern.compile(StringUtils.strip(entry, "/"));

		          LinkedList<String> matchingAnnotations = new LinkedList<>();
		          for (String anno : annotationPool) {
		            if (regex.matcher(anno).matches()) {
		              matchingAnnotations.add(anno);
		            
		            }
		          }
		          confAnnotations.addAll(matchingAnnotations);
		          annotationPool.removeAll(matchingAnnotations);

		        } else {
		        	confAnnotations.add(entry);
		        	annotationPool.remove(entry);
		        }
		      }
		    }
		    
		    System.out.println(relAnnosConfiguration + " \t" +confAnnotations);
		    
		    return confAnnotations;
		  }

		
		
		
		
		 /**
		   * Get the qualified name of all annotations belonging to relations having a
		   * specific namespace.
		   *
		   * @param graph The graph.
		   * @param namespace The namespace of the relation (not the annotation) to search 
		   * for. If namespace is null all namespaces will be considered.
		   * @param type Which type of relation to include
		   * @return
		   *
		   */
		
		// TODO test
		  public static Set<String> getRelationLevelSet(SDocumentGraph graph,
		          String namespace, Class<? extends SRelation> type) {
		    Set<String> result = new TreeSet<>();
		    

		    if (graph != null) {
		      List<? extends SRelation> edges = null;
		      // catch most common cases directly
		      if (type == SDominanceRelation.class) {
		        edges = graph.getDominanceRelations();
		      } else if (type == SPointingRelation.class){
		    	  edges = graph.getPointingRelations();
		      } else if (type == SSpanningRelation.class){
		    	  edges = graph.getSpanningRelations();
		      }
		     /* else {
		    	  edges = graph.getRelations();
		      }*/
		      
		      
		      if (edges != null) {
		        for (SRelation<?, ?> edge : edges) {
		        	  Set <SLayer> layers = edge.getLayers();
		            for (SLayer layer : layers) {
		              if (namespace == null || namespace.equals(layer.getName())) {
		                for (SAnnotation anno : edge.getAnnotations()) {
		                  result.add(anno.getQName());
		                }
		                // we got all annotations of this edge, jump to next edge
		                break;
		              } // end if namespace equals layer name
		            } // end for each layer
		        } // end for each edge
		      }
		    }
		    System.out.println("RelLevelSet \t" + result);
		    return result;
		  }
		  
	  @Override
		public boolean includeNode(SNode node) {		  
			// if node is a token or no configuration set, output the node
			if (node instanceof SToken || nodeAnnosConfiguration == null)
			{
				return true;
			}
					
			Set<SAnnotation> nodeAnnotations =  node.getAnnotations();
			
			for (SAnnotation nodeAnnotation : nodeAnnotations)
			{
				String nodeAnno = nodeAnnotation.getName();
				String nodeNs = nodeAnnotation.getNamespace();
				
				if (filterNodeAnnotations.containsKey(nodeAnno))
				{
					// namespace have not to be considered
					if (filterNodeAnnotations.get(nodeAnno).isEmpty())
					{
						return true;
					}
					else if (filterNodeAnnotations.get(nodeAnno).contains(nodeNs))
					 	{
							return true;														
						
					  }
					
					}
					
				}
				
			return false;
		}
		


		@Override
		public boolean includeRelation(SRelation relation) {
			Map<String, Set<String>> filterEdgeAnnotations = new HashMap<String, Set<String>> ();
		
			// if no configuration set output the relation
			if (relation instanceof SPointingRelation)
			{
				if (pointingRelAnnosConfiguration == null)
					{
						return true;
					}
				else 
					{
						filterEdgeAnnotations = filterPointingRelAnnotations;
					}
			}
			
			if (relation instanceof SSpanningRelation)
			{
				if (spanningRelAnnosConfiguration == null)
					{
					    return true;
					}
				else
					{
						filterEdgeAnnotations = filterSpanningRelAnnotations;
					}
			}
			
			if (relation instanceof SDominanceRelation)
				{
				if (dominanceRelAnnosConfiguration == null)
					{
					  return true;
					}
				else
					{
					filterEdgeAnnotations = filterDominanceRelAnnotations;
					}
			}
					
			
			Set<SAnnotation> edgeAnnotations =  relation.getAnnotations();
			
			for (SAnnotation edgeAnnotation : edgeAnnotations)
			{
				String edgeAnno = edgeAnnotation.getName();
				String edgeNs = edgeAnnotation.getNamespace();
				
				if (filterEdgeAnnotations.containsKey(edgeAnno))
				{
					//namespace have not to be considered
					if (filterEdgeAnnotations.get(edgeAnno).isEmpty())
					{
						return true;
					}
					else if (filterEdgeAnnotations.get(edgeAnno).contains(edgeNs))
					{
						return true;
					}
				}
				
			}
			return false;
		}

		@Override
		public String getFontColor(SNode node) {
			String color = null;
					    	
    		SFeature featMatched = node.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
		    Long matchRaw = featMatched == null ? null : featMatched.getValue_SNUMERIC();				    
			// token is matched			    
		    if (matchRaw != null)
		    {
		    	color = MatchedNodeColors.getHTMLColorByMatch(matchRaw);
		    	//System.out.println("tokens color \t" + color);
		    	return color;
		    }	
    	
			return color;
			    
			   // String text =  doc.getDocumentGraph().getText(node);		    
			    //System.out.println(text + "\t" + matchRaw + "\t" + MatchedNodeColors.getHTMLColorByMatch(matchRaw));
			     
		}

		
		

}
