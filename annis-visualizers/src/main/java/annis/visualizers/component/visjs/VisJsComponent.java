package annis.visualizers.component.visjs;


import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;

import annis.libgui.MatchedNodeColors;
import annis.libgui.visualizers.VisualizerInput;
import annis.visualizers.component.grid.EventExtractor;


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
 * {@link VisJsComponent} visualizes Salt model of a specified [VisualizerInput](\ref annis.libgui.visualizers.VisualizerInput).
 * 
 * 
 * @author irina
 *
 */

public class VisJsComponent extends AbstractJavaScriptComponent implements ExportFilter, StyleImporter{	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9006240832319119407L;
	
	private final static Logger log = LoggerFactory.getLogger(VisJsComponent.class);
	
	private String strNodes;
	private String strEdges;
	
	public enum Annos_Keyword {
		    NODE_ANNOS_KW(annis.visualizers.component.grid.GridComponent.MAPPING_ANNOS_KEY),
			POINTING_REL_ANNOS_KW ("pointingRelAnnos"), 
			SPANNING_REL_ANNOS_KW ("spanningRelAnnos"), 
			DOMINANCE_REL_ANNOS_KW ("dominanceRelAnnos");
			
		 private final String value;
		    
		  private Annos_Keyword(String value) {
		    this.value = value;
		  }
		  
		  public String getValue() {
		    return value;
		  }
	}
	
	
	// a HashMap for storage of node annotations for export filter with associated namespaces	
	private Map<String, Set<String>> displayedNodeAnnotations = new HashMap<String, Set<String>>();
	
	// a HashMap to storage of annotations of pointing relations for export filter with associated namespaces	
	private Map<String, Set<String>> displayedPointingRelAnnotations = new HashMap<String, Set<String>>();
	
	// a HashMap to storage of annotations of pointing relations for export filter with associated namespaces	
	private Map<String, Set<String>> displayedSpanningRelAnnotations = new HashMap<String, Set<String>>();
		
	// a HashMap to storage of annotations of pointing relations for export filter with associated namespaces	
	private Map<String, Set<String>> displayedDominanceRelAnnotations = new HashMap<String, Set<String>>();
	

	
	//a list to keep annotation configurations put by user
	//0th entry for nodes, 1st for pointing relations, 2nd for spanning relations, 3rd for dominance relations
	private final List<String> configurations = new ArrayList<String>();
	
	
	/**
	 * Creates a new {@link VisJsComponent} instance.
	 * 
	 * @param visInput the specified [VisualizerInput](\ref annis.libgui.visualizers.VisualizerInput)
	 */	
	public VisJsComponent(VisualizerInput visInput){
		
			//put user configurations to the configuration list
			for(Annos_Keyword kw: Annos_Keyword.values())
			{
				configurations.add(visInput.getMappings().getProperty(kw.getValue()));
				fillFilterAnnotations(visInput, kw.ordinal());
			}
			
				
			SDocument doc =  visInput.getDocument();
			
    try(ByteArrayOutputStream osNodes = new ByteArrayOutputStream();
        ByteArrayOutputStream osEdges = new ByteArrayOutputStream())
    {
      VisJsVisualizer visualizer = new VisJsVisualizer(doc, this, this);

      
      visualizer.setNodeWriter(osNodes);
      visualizer.setEdgeWriter(osEdges);
      visualizer.buildJSON();
      
      strNodes = osNodes.toString(Charsets.UTF_8.name());
      strEdges = osEdges.toString(Charsets.UTF_8.name());

      osNodes.close();
      osEdges.close();

    }
    catch (IOException e)
    {
      log.error("Could not write the VisJS output", e);
    } 
				
	}
	
		
	// fills export filter annotations for the specified type (0 -> nodes; 1 -> pointing relations, 2 -> spanning relations, 3 -> dominance relations)
	//private void fillFilterAnnotations(VisualizerInput visInput, int type){
		private void fillFilterAnnotations(VisualizerInput visInput, int type){
		List<String> displayedAnnotations = null;
		Map<String, Set<String>> displayedAnnotationsMap = null;
		
		switch(type){
		case(0):{
			displayedAnnotations = EventExtractor.computeDisplayAnnotations(visInput, SNode.class);	
			displayedAnnotationsMap = displayedNodeAnnotations;
			break;
		}
		case(1):{
			displayedAnnotations = computeDisplayedRelAnnotations(visInput, configurations.get(type), SPointingRelation.class);	
			displayedAnnotationsMap = displayedPointingRelAnnotations;
			break;
		}
		case(2):{
			displayedAnnotations = computeDisplayedRelAnnotations(visInput, configurations.get(type), SSpanningRelation.class);	
			displayedAnnotationsMap = displayedSpanningRelAnnotations;
			break;
		}
		case(3):{
			displayedAnnotations = computeDisplayedRelAnnotations(visInput, configurations.get(type), SDominanceRelation.class);	
			displayedAnnotationsMap = displayedDominanceRelAnnotations;
			break;
		}
		default: {
			throw new IllegalArgumentException();
		}
		
		}
		
		
	 	for(String annotation: displayedAnnotations)
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
						throw new IllegalArgumentException("The annotation string in resolver_vis_map table is not well formed.");
					}
					
				}
				else
				{
				anno = annotation;	
				}
				
				
				if (displayedAnnotationsMap.containsKey(anno))
				{
					 namespaces = displayedAnnotationsMap.get(anno);
				}
				else
				{
					namespaces = new HashSet<String>();
				}
				
				
				if (ns != null)
				{
					namespaces.add(ns);
				}
				
				displayedAnnotationsMap.put(anno, namespaces);
								
			}
	
	}

		  
	  
	    @Override
	    protected VisJsState getState() {
	        return (VisJsState) super.getState();
	    }
	
	    @Override
	    public void attach() {
	      super.attach();
	      setHeight("100%");
	      setWidth("100%");
	      getState().strNodes = strNodes;
	      getState().strEdges = strEdges;
	     
	    }
	    
		
		
		/**
		   * Returns the annotations to display according to the mappings configuration.
		   *
		   * This will check the "relation" parameter for determining the annotations to display. 
		   * It also iterates over all nodes of the graph
		   * matching the type.
		   *
		   * @param input The input for the visualizer.
		   * @param type Which type of relations to include
		   * @return
		   */
		  private static List<String> computeDisplayedRelAnnotations(VisualizerInput input, 
				  								String relAnnosConfiguration, Class<? extends SRelation> type) {
		    if (input == null) {
		      return new LinkedList<>();
		    }

		    SDocumentGraph graph = input.getDocument().getDocumentGraph();

		    Set<String> annotationPool = getRelationLevelSet(graph, null, type);
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
		
		  private static Set<String> getRelationLevelSet(SDocumentGraph graph,
		          String namespace, Class<? extends SRelation> type) {
		    Set<String> result = new TreeSet<>();
		    

		    if (graph != null) {
		      List<? extends SRelation> edges = null;
		      if (type == SDominanceRelation.class) {
		        edges = graph.getDominanceRelations();
		      } else if (type == SPointingRelation.class){
		    	  edges = graph.getPointingRelations();
		      } else if (type == SSpanningRelation.class){
		    	  edges = graph.getSpanningRelations();
		      }
		      
		      
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
		    return result;
		  }
	
	/**
	 * Implements the includeNode method of the org.corpus_tools.salt.util.ExportFilter interface.
	 */
	  @Override
		public boolean includeNode(SNode node) {		  
			// if node is a token or no configuration set, output the node
			if (node instanceof SToken || configurations.get(0) == null)
			{
				return true;
			}
					
			Set<SAnnotation> nodeAnnotations =  node.getAnnotations();
			
			return includeObject(nodeAnnotations, displayedNodeAnnotations);
		}
		

	  	/**
		 * Implements the includeRelation method of the org.corpus_tools.salt.util.ExportFilter interface.
		 */

		@Override
		public boolean includeRelation(SRelation relation) {
			Map<String, Set<String>> displayedRelAnnotations = new HashMap<String, Set<String>> ();
		
			// if no configuration set output the relation
			if (relation instanceof SPointingRelation)
			{
				if (configurations.get(1) == null)
					{
						return true;
					}
				else 
					{
						displayedRelAnnotations = displayedPointingRelAnnotations;
					}
			}
			
			if (relation instanceof SSpanningRelation)
			{
				if (configurations.get(2) == null)
					{
					    return true;
					}
				else
					{
						displayedRelAnnotations = displayedSpanningRelAnnotations;
					}
			}
			
			if (relation instanceof SDominanceRelation)
				{
				if (configurations.get(3) == null)
					{
					  return true;
					}
				else
					{
					displayedRelAnnotations = displayedDominanceRelAnnotations;
					}
			}
				
			
			Set<SAnnotation> relAnnotations =  relation.getAnnotations();			
			
			return includeObject(relAnnotations, displayedRelAnnotations);
		}
		
		
		private static boolean includeObject (Set<SAnnotation> objectAnnotations, Map<String, Set<String>> displayedAnnotationsMap){
			
			for (SAnnotation objectAnnotation : objectAnnotations)
			{
				String annotation = objectAnnotation.getName();
				String namespace = objectAnnotation.getNamespace();
				
				if (displayedAnnotationsMap.containsKey(annotation))
				{
					//namespace has not to be considered
					if (displayedAnnotationsMap.get(annotation).isEmpty())
					{
						return true;
					}
					else if (displayedAnnotationsMap.get(annotation).contains(namespace))
					{
						return true;
					}
				}
				
			}
			
			return false;	
		}
		
		/**
		 * Implements the getHighlightingColor method of the org.corpus_tools.salt.util.StyleImporter interface.
		 */
		@Override
		public String setHighlightingColor(SNode node) {
			String color = null;
					    	
    		SFeature featMatched = node.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
		    Long matchRaw = featMatched == null ? null : featMatched.getValue_SNUMERIC();				    
			// token is matched			    
		    if (matchRaw != null)
		    {
		    	color = MatchedNodeColors.getHTMLColorByMatch(matchRaw);
		    	return color;
		    }	
    	
			return color;
			     
		}
		

}
