/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.exporter;

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDIDS;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.lang.Exception;

import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

import com.google.common.base.Joiner;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.ui.Notification;

import annis.CommonHelper;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.model.AnnisConstants;
import annis.service.objects.Match;
import annis.service.objects.SubgraphFilter;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * An exporter that will take all token nodes and exports
 * them in a kind of grid.
 * This is useful for getting references of texts where the normal token based
 * text exporter doesn't work since there are multiple speakers or normalizations.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @author irina
 */
@PluginImplementation
public class MatchWithContextExporterDev extends SaltBasedExporter
{
	private static final String TRAV_IS_DOMINATED_BY_MATCH = "IsDominatedByMatch";
	private static final String TRAV_SPEAKER_HAS_MATCHES = "SpeakerHasMatches";
	public static final String FILTER_PARAMETER_KEYWORD = "filter";
	public static final String FILTER_PARAMETER_SEPARATOR = ",";
	private static HashMap <String, Boolean> speakerHasMatches = new HashMap<String, Boolean>();
	private static String speakerName;
	private boolean isFirstSpeakerWithMatch = true;   
	private static List <Long> dominatedMatchCodes = new ArrayList<Long>();
	private static Map <Integer, List<Long>> dominanceLists = new HashMap <Integer, List<Long>>();
	private static Map <Integer, Long> tokenToMatchNumber = new HashMap <Integer, Long>();
	private static Map <Long, List<Long>> dominanceListsWithHead = new HashMap <Long, List<Long>>();
	private static Set<Long> inDominanceRelation = new HashSet<Long>();
	private static Set<Long> filterNumbers = new HashSet<Long>(); 
  
	
  private static class IsDominatedByMatch implements GraphTraverseHandler
  {
   
    Long matchedNode = null;
    

    @Override
    public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        SRelation<SNode, SNode> relation, SNode fromNode, long order)
    {
    	 SFeature matchedAnno = currNode.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE);
    	 
    	 if(matchedAnno != null && (filterNumbers.contains(matchedAnno.getValue_SNUMERIC()) || filterNumbers.isEmpty()))
	      {
	        matchedNode = matchedAnno.getValue_SNUMERIC();	       
	        //
	        if (traversalId.equals(TRAV_SPEAKER_HAS_MATCHES) )
	        {
	        dominatedMatchCodes.add(matchedNode);
	        
	        if (dominatedMatchCodes.size() > 1){
	        	inDominanceRelation.add(matchedNode);
	        }
	       
	        speakerHasMatches.put(speakerName, true);
	        }
	      }
	      
	     
	      
	      
    }

    @Override
    public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        SRelation<SNode, SNode> relation, SNode fromNode, long order)
    {
   
     
    }

    @Override
    public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
        SRelation relation, SNode currNode, long order)
    {
    	if(traversalId.equals(TRAV_IS_DOMINATED_BY_MATCH))
    	{	
		      if(this.matchedNode != null && (filterNumbers.contains(this.matchedNode) || filterNumbers.isEmpty()))
		      { // don't traverse any further if matched node was found 
		        return false;
		      }
    	}
    	
		return 
	            relation == null
	            || relation instanceof SDominanceRelation 
	            || relation instanceof SSpanningRelation;
   
    } 
  }

  
  @Override
  public void convertText(SDocumentGraph graph, List<String> annoKeys,
    Map<String, String> args, int matchNumber, Writer out) throws IOException, IllegalArgumentException
  {
 
  	String currSpeakerName = "";
	String prevSpeakerName = "";
	filterNumbers.clear();
	
		
	    
    if(graph != null)
    {
      List<SToken> orderedToken = graph.getSortedTokenByText();
      
      //extract filter numbers
      if (args.containsKey(FILTER_PARAMETER_KEYWORD)){
    	     	 
    	  String parameters = args.get(FILTER_PARAMETER_KEYWORD);
    	  String [] numbers = parameters.split(",");
        	  for (int i=0; i< numbers.length; i++){
    		  try {
    			 Long number = Long.parseLong(numbers[i]);
    			 filterNumbers.add(number);
     			 
    		  }
    		  catch(NumberFormatException e){
    			 ;
    		  }
    		  
    	  }
    	  
      }
      
      
   /*   if(orderedToken != null)
      {
    	  for(SToken token : orderedToken){
    		  System.out.print(graph.getText(token) + "\t");
    	  }
    	  System.out.println("\n");
      }*/
      
      
      if(orderedToken != null)
      {    	   
    	 //reset the data structures for new graph
    	  speakerHasMatches.clear();    	  
    	  inDominanceRelation.clear();    	  
    	  dominanceLists.clear();    	  
    	  dominanceListsWithHead.clear();
    	  tokenToMatchNumber.clear();
    	  
 
    	 // counter over dominance lists
    	  int counter = 0;
    
    	// iterate first time over tokens to figure out which speaker has matches and to recognize the hierarchical structure of matches as well
    	  for(SToken token : orderedToken){
    		  counter++;
    		//  System.out.println(counter + ". Token:\t" + graph.getText(token));
             
              
    		  
              STextualDS textualDS = CommonHelper.getTextualDSForNode(token, graph);
              speakerName = textualDS.getName();
              
              if (!speakerHasMatches.containsKey(speakerName))
              {
            	  speakerHasMatches.put(speakerName, false);
              }
                  
              
    		  List<SNode> root = new LinkedList<>();
              root.add(token);
              IsDominatedByMatch traverserSpeakerSearch = new IsDominatedByMatch();
              
                          
              //reset list
        	  dominatedMatchCodes = new ArrayList<Long>();
        	  
              
              graph.traverse(root, GRAPH_TRAVERSE_TYPE.BOTTOM_UP_DEPTH_FIRST, TRAV_SPEAKER_HAS_MATCHES, traverserSpeakerSearch); 
              
              
              if (!dominatedMatchCodes.isEmpty()){
            	  dominanceListsWithHead.put(dominatedMatchCodes.get(0), dominatedMatchCodes);
                  dominanceLists.put(counter, dominatedMatchCodes);
                  
                  // filter numbers not set, take the number of the highest match node
                  if (filterNumbers.isEmpty()){
                	  tokenToMatchNumber.put(counter, dominatedMatchCodes.get(dominatedMatchCodes.size() - 1));
                  }
                  else{
                	  for (int i = 0; i < dominatedMatchCodes.size(); i++){
                    	  if (filterNumbers.contains(dominatedMatchCodes.get(i))){
                    		  tokenToMatchNumber.put(counter, dominatedMatchCodes.get(i));
                    	  }
                      }
                  }                  
                 
              }
                                      
    	  }
    	  
     	  
    	 //iterate again 
        ListIterator<SToken> it = orderedToken.listIterator();
        long lastTokenWasMatched = -1;
        boolean noPreviousTokenInLine = false;
       
        
        Iterator<Long> inDomIt = inDominanceRelation.iterator();        
        //eliminate entries, whose key (matching code) dominate other matching codes  
        while(inDomIt.hasNext()){
        	Long matchingCode = inDomIt.next();
        	if (dominanceListsWithHead.containsKey(matchingCode)){
        		dominanceListsWithHead.remove(matchingCode);
        	}
        }
        
        Set<Map.Entry<Integer, List<Long>>> entries = dominanceLists.entrySet();
        // a helping data structure to eliminate duplicates of dominance lists
        Map <Integer, List<Long>> dominanceListsWithoutDoubles = new HashMap<Integer, List<Long>>();
        
        for(Map.Entry<Integer, List<Long>> entry : entries){
        	if (dominanceListsWithHead.containsValue(entry.getValue()) && !dominanceListsWithoutDoubles.containsValue(entry.getValue())){
        		dominanceListsWithoutDoubles.put(entry.getKey(), entry.getValue());
        	}
         }
        
                       
      /*  System.out.println(dominanceLists);
        System.out.println(tokenToMatchNumber);
        System.out.println(dominanceListsWithHead);
        System.out.println(dominanceListsWithoutDoubles);*/
               
               
        List <Map.Entry<Integer, List<Long>>> domListsSorted =  sortByKey(dominanceListsWithoutDoubles);
     
        
        boolean filterNumbersEmpty = true;
        if (!filterNumbers.isEmpty())
        {
        	filterNumbersEmpty = false;
        	
        }
        
        // if filter numbers not set, set default filter numbers (always the root of a match hierarchy)
        if (filterNumbersEmpty){
        	
	        for (Map.Entry <Integer, List<Long>> entry : domListsSorted){
				 List<Long>  domList = entry.getValue();			 
				 filterNumbers.add(domList.get(domList.size() - 1));
				 }			 
        }
        //if filter numbers set, validate them
        else{
        	Set<List<Long>> usedDominanceLists = new HashSet<List<Long>>();
        	for (Long filterNumber : filterNumbers){
        		
        		boolean filterNumberIsValid = false;
        		for (List<Long> dominanceList : dominanceListsWithoutDoubles.values()){
        			if (dominanceList.contains(filterNumber)){
        				if (usedDominanceLists.contains(dominanceList)){
        					filterNumberIsValid = false;
        					throw new IllegalArgumentException("Please use one filter number per match hierarchy only. "
        							+ "\n Data could not be exported.");
        					
        					
        				}
        				else{
        					usedDominanceLists.add(dominanceList);
        					filterNumberIsValid = true;
        					
        				}
        			}
        		}
        		
        		//filter number was not found in dominance lists, thus it is not valid
        		if (!filterNumberIsValid){
        			throw new IllegalArgumentException("The filter number " + filterNumber + " is not valid. "
        					+ "\n Data could not be exported.");     			
        					       			
        		}
        		
        	}
        }
            
        
        
      //TODO why does match number start with -1? 
    	//if match number == -1, reset global variables 
    	if (matchNumber == -1){
    		isFirstSpeakerWithMatch = true;
    	}
    	
         
    	//reset counter
        counter = 0;
        while(it.hasNext())
        {    	
          SToken tok = it.next();    
          counter++;
          //get current speaker name
          currSpeakerName = CommonHelper.getTextualDSForNode(tok, graph).getName();
                    
          
          // if speaker has no matches, skip token
          if (speakerHasMatches.get(currSpeakerName) == false)
          {
        	  prevSpeakerName = currSpeakerName;
        	 // continue;
          }
          
          //if speaker has matches
          else
          {			
        	  
	        	  //if the current speaker is new, append his name and write header
	        	 if (!currSpeakerName.equals(prevSpeakerName))
	        	 { 
	   
	        		 if (isFirstSpeakerWithMatch){
	        			 
	        			 out.append("match_number\t");
	        			 out.append("speaker\t");
	        			 
	        			 out.append("left_context\t");
		        		 
		        		 String prefix = "M_";
		        		 
		        		for (int i = 0; i < filterNumbers.size(); i++){		        			
		        			out.append(prefix + (i + 1) + "\t");		        			        
		        			 if (i < filterNumbers.size() - 1){
		        				 out.append("middle_context_" +  (i + 1) + "\t"); 
		        			 }      			 
		        		 }
		        		 
		        		 out.append("right_context");
		        		 out.append("\n");
	        			 
	        			 isFirstSpeakerWithMatch = false;
	        		 }
	        		 else {
	        			 out.append("\n");
	        		 } 
	            		   		
	        		 	        		
	        		 // TODO why does matchNumber start with -1?
	        		 out.append(String.valueOf(matchNumber + 2) + "\t");
	        		 out.append(currSpeakerName + "\t");
	        		 
	        		 
	        		 lastTokenWasMatched = -1;
	        		 noPreviousTokenInLine = true;
	        		 
	        		
	        	 }// header ready
	        	 
	        	  String separator = " "; // default to space as separator
	        	       	  
	        	  		  List<SNode> root = new LinkedList<>();
		                  root.add(tok);
		                  IsDominatedByMatch traverser = new IsDominatedByMatch();
		                  graph.traverse(root, GRAPH_TRAVERSE_TYPE.BOTTOM_UP_DEPTH_FIRST, "IsDominatedByMatch", traverser);
		               
		                  // token matched
		                  if(traverser.matchedNode != null)
		                  {
		                    // is dominated by a (new) matched node, thus use tab to separate the non-matches from the matches
		                    if(lastTokenWasMatched < 0)
		                    {
		                       separator = "\t"; 
		                                                     
		                			                      
		                    }
		                    else if(lastTokenWasMatched != (long) traverser.matchedNode)
		                    {
		                      // always leave an empty column between two matches, even if there is no actual context
		                    	separator = "\t\t";
		                    			                    	
		                    }
		                    lastTokenWasMatched = traverser.matchedNode;
		                  }
		                  // token not matched, but last token matched
		                  else if(lastTokenWasMatched >= 0)
		                  {
		                                    	  
		                	  //handle crossing edges
		                	  if(!tokenToMatchNumber.containsKey(counter) && 
		                			  tokenToMatchNumber.containsKey(counter - 1) && tokenToMatchNumber.containsKey(counter + 1)){
		                		    
		                		  
		                    			if (tokenToMatchNumber.get(counter - 1) == tokenToMatchNumber.get(counter + 1)){
		                    				
		                    				separator = " ";                     
		    		                    	lastTokenWasMatched = tokenToMatchNumber.get(counter + 1);
		                    			}	                    				
	                    				else{
	                    					             						                    
	       			                    	  separator = "\t";           			                	  
	       			                    	  lastTokenWasMatched = -1;
	                    				}
	                    				
	                    				
	                    			}
		                	// mark the end of a match with the tab
			           	  else{
		                		            
			                    separator = "\t";		                	  
			                    lastTokenWasMatched = -1;
		                	  }
	                    			 	  
		                	 
		                  }
		                  
		                  //if tok is the first token in the line and not matched, set separator to empty string
		                  if (noPreviousTokenInLine && separator.equals(" "))
		                  {
		                	 separator = "";
		                  }
		                  out.append(separator);
		           
		       	          
		          
          // append the actual token
          out.append(graph.getText(tok));
          noPreviousTokenInLine = false; 
          prevSpeakerName = currSpeakerName;
               
         }              
          
        }
      
      }
       
    }
    

  }
  

  @Override
  public SubgraphFilter getSubgraphFilter()
  {
    return SubgraphFilter.all;
  }
  
  @Override
  public String getHelpMessage()
  {
	  return "The MatchWithContext-Exporter exports matches surrounded by the context as csv file."
		        + "The matches as well as the context will be aligned. <br/>"
		        + "This exporter doesn't work yet for results of aql-queries with <em>overlap</em> or <em>or</em> operators.<br/><br/>"
		        + "Parameters: <br/>"
		        + "<em>filter</em> - comma separated list of all match numbers to be represented in the result as a separated column (e.g. "
		        + "<code>filter=1,2</code>) <br/>"
		        + "Please note, if some matched nodes build a hierarchy, you can use one match number per hierarchy only.";
  }
  
  @Override
  public String getFileEnding()
  {
    return "csv";
  }
  
  private static <K, V> List<Map.Entry<K, V>> sortByKey(Map<K, V> map) {
		List<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(map.size());

		for (Map.Entry<K, V> e : map.entrySet()) {
			entries.add(e);
		}

		Comparator<Map.Entry<K, V>> comparator = new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
				return (Integer.valueOf(e1.getKey().toString()) - Integer.valueOf(e2.getKey().toString()));

			}
		};
		// sort key values numerical
		Collections.sort(entries, comparator);

		return entries;
	}
  
}
