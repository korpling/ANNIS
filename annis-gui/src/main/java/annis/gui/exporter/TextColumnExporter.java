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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

import annis.CommonHelper;
import annis.gui.QueryController;
import annis.libgui.Helper;
import annis.model.AnnisConstants;
import annis.model.Annotation;
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
public class TextColumnExporter extends SaltBasedExporter
{
	 private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();
	private static final String TRAV_IS_DOMINATED_BY_MATCH = "IsDominatedByMatch";
	private static final String TRAV_SPEAKER_HAS_MATCHES = "SpeakerHasMatches";
	public static final String FILTER_PARAMETER_KEYWORD = "filter";
	public static final String PARAMETER_SEPARATOR = ",";
	public static final String METAKEYS_KEYWORD = "metakeys";
	private static final String NEWLINE = System.lineSeparator();    
	private static final String TAB_MARK = "\t"; 
	private static final String SPACE = " ";    
	private static HashMap <String, Boolean> speakerHasMatches = new HashMap<String, Boolean>();
	private static String speakerName;
	private boolean isFirstSpeakerWithMatch = true;   
	private static List <Long> dominatedMatchCodes = new ArrayList<Long>();
	// a helping structure to handle crossing edges, must be global over all query results
	private static Map <Integer, Long> tokenToMatchNumber = new HashMap <Integer, Long>();
	// contains filter numbers from ui, global over all query results
	private static Set<Long> filterNumbersSetByUser = new HashSet<Long>(); 
	// indicate, whether filter numbers were set by user, global over all query results
	private static boolean filterNumbersIsEmpty = true;		
	// contains metakeys, set by user, global over all query results
	private static List<String> listOfMetakeys = new ArrayList<String>(); 

	// a helping structure to determine the right order of match nodes over all records
	private static int [][] adjacencyMatrix;
	//indicates, whether the adjacency matrix is filled or not
	private static boolean matrixIsFilled = false;
	// contains single match codes per speaker, globally over all query results
	private static Set <Long> singleMatchesGlobal = new HashSet <Long>();
	// contains a  sequence of match numbers per speaker ordered according to their occurrence in text, globally over all query results
	private static List <Long> orderedMatchNumbersGlobal = new ArrayList <Long>();
	// contains a set of match numbers occurred in text, globally over all query results
	private static Set <Long> matchNumbersGlobal = new HashSet<Long>();
	// indicates, whether data is alignable or not
	private static boolean dataIsAlignable = true;
	
	 
	
	 private static final Logger log = LoggerFactory.getLogger(TextColumnExporter.class);
	 
  
	
  private static class IsDominatedByMatch implements GraphTraverseHandler
  {
   
    Long matchedNode = null;
    

    @Override
    public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        SRelation<SNode, SNode> relation, SNode fromNode, long order)
    {
    	 SFeature matchedAnno = currNode.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE);
    	 
    	 if(matchedAnno != null && (filterNumbersSetByUser.contains(matchedAnno.getValue_SNUMERIC()) || filterNumbersIsEmpty))
	      {
	        matchedNode = matchedAnno.getValue_SNUMERIC();	       
	        //
	        if (traversalId.equals(TRAV_SPEAKER_HAS_MATCHES) )
	        {
	        dominatedMatchCodes.add(matchedNode);
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
		      if(this.matchedNode != null && (filterNumbersSetByUser.contains(this.matchedNode) || filterNumbersIsEmpty))
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

  
//  @Override
 /* public void convertText(SDocumentGraph graph, List<String> annoKeys,
    Map<String, String> args, boolean alignmc, int matchNumber, Writer out) throws IOException, IllegalArgumentException
  {
	 
  	String currSpeakerName = "";
	String prevSpeakerName = "";
	//if new search
	if (matchNumber == -1){
		filterNumbers.clear();
		listOfMetakeys.clear();
		filterNumbersOrdered.clear();
		filterNumbersIsEmpty = true;
	}
	
		
	    
    if(graph != null)
    {
      List<SToken> orderedToken = graph.getSortedTokenByText();
      
      // if new search
      if (matchNumber == -1){
	      //extract filter numbers
	      if (args.containsKey(FILTER_PARAMETER_KEYWORD)){
	    	     	 
	    	  String parameters = args.get(FILTER_PARAMETER_KEYWORD);
	    	  String [] numbers = parameters.split(PARAMETER_SEPARATOR);
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
	      
	      
	      if (!filterNumbers.isEmpty())
	        {
	        	filterNumbersIsEmpty = false;
	        	
	        }
	      
	      // extract metakeys
	      if (args.containsKey(METAKEYS_KEYWORD)){
	    	  String parameters = args.get(METAKEYS_KEYWORD);
	    	  String [] metakeys = parameters.split(PARAMETER_SEPARATOR);
	    	  for (int i=0; i< metakeys.length; i++){    	
	    			 String metakey = metakeys[i].trim();
	    			 listOfMetakeys.add(metakey);  		  
	    	  }    	  
	    	  
	      }
      }
      
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
                  
                  // if filter numbers not set, take the number of the highest match node
                  if (filterNumbersIsEmpty){
                	  tokenToMatchNumber.put(counter, dominatedMatchCodes.get(dominatedMatchCodes.size() - 1));
                	               	  
                	  //set filter number to the ordered list
                	  if (!filterNumbersOrdered.contains(dominatedMatchCodes.get(dominatedMatchCodes.size() - 1))){
                		  filterNumbersOrdered.add(dominatedMatchCodes.get(dominatedMatchCodes.size() - 1));
              
                	  }
                  }
                  else{
                	  // take the highest match code, which is present in filterNumbers
                	  for (int i = dominatedMatchCodes.size() - 1; i >= 0; i--){
                    	  if (filterNumbers.contains(dominatedMatchCodes.get(i))){
                    		  tokenToMatchNumber.put(counter, dominatedMatchCodes.get(i));
                    		  
                    		  		  
                    		  if (!filterNumbersOrdered.contains(dominatedMatchCodes.get(i))){
                        		  filterNumbersOrdered.add(dominatedMatchCodes.get(i));
       
                        	  }
                    		  break;
                    	  }
                      }
                  }                  
                 
              }
                                      
    	  }
    	  
    	 log.debug("filterNumbers: " + filterNumbers);
    	 log.debug("filterNumbersOrdered: " + filterNumbersOrdered);
    	//  System.out.println("filterNumbers: " + filterNumbers);
    //	  System.out.println("filterNumbersOrdered: " + filterNumbersOrdered);
    	  
     	  
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
        
     
        	/*	System.out.println(dominanceLists);
        		System.out.println(tokenToMatchNumber);
                System.out.println(dominanceListsWithHead);
                System.out.println(dominanceListsWithoutDoubles);*/
        
  
                  
        // if filter numbers not set by user, set default filter numbers (taken from filterNumbersOrdered)
    /*   if (matchNumber == -1){
    	   
	        if (filterNumbersIsEmpty){
	        	        		
	        		for (Long filterNumber: filterNumbersOrdered){   				  
	        			filterNumbers.add(filterNumber);
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
	        					throw new IllegalArgumentException("Please use one filter number per match hierarchy only."
	        							+ NEWLINE + "Data could not be exported.");
	        					
	        					
	        				}
	        				else{
	        					usedDominanceLists.add(dominanceList);
	        					filterNumberIsValid = true;
	        					
	        				}
	        			}
	        		}
	        		
	        		//filter number was not found in dominance lists, thus it is not valid
	        		if (!filterNumberIsValid){
	        			throw new IllegalArgumentException("The filter number " + filterNumber + " is not valid."
	        					+ NEWLINE + "Data could not be exported.");     			
	        					       			
	        		}
	        		
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
        int matchesWrittenForSpeaker = 0;
        
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
	        		 //reset the counter of matches, which were written for this speaker	
	        		 matchesWrittenForSpeaker = 0;
	   
	        		 if (isFirstSpeakerWithMatch){
	        			 
	        			 out.append("match_number" + TAB_MARK);
	        			 out.append("speaker" + TAB_MARK);
	        			 
	        			 // write header for meta data columns
	        			 if (!listOfMetakeys.isEmpty()){
	        				 for(String metakey : listOfMetakeys){
	        					 out.append(metakey + TAB_MARK);
	        				 }
	        			 }
	        			 
	        			 
	        			 
	        			 out.append("left_context" + TAB_MARK);
		        		 
		        		 String prefixAlignmc = "match_";
		        		 String prefix = "match_column";
		        		 
		        		for (int i = 0; i < filterNumbersOrdered.size(); i++){		   
		        			if (alignmc){
		        				out.append(prefixAlignmc + filterNumbersOrdered.get(i) + TAB_MARK);
		        			}
		        			else{
		        				out.append(prefix + TAB_MARK);
		        			}
		        			
		        			
		        			 if (i < filterNumbersOrdered.size() - 1){
		        				 out.append("middle_context_" +  (i + 1) + TAB_MARK); 
		        			 }      			 
		        		 }
		        		 
		        		 out.append("right_context");
		        		 out.append(NEWLINE);
	        			 
	        			 isFirstSpeakerWithMatch = false;
	        		 }
	        		 else {
	        			 out.append(NEWLINE);
	        		 } 
	            		   		
	        		 	        		
	        		 // TODO why does matchNumber start with -1?
	        		 out.append(String.valueOf(matchNumber + 2) + TAB_MARK);
	        		 out.append(currSpeakerName + TAB_MARK);
	        		 
	        		 //write meta data
	        		 if (!listOfMetakeys.isEmpty()){
	        			 // get metadata
	        			  String docName = graph.getDocument().getName();
	                      List<String> corpusPath = CommonHelper.getCorpusPath(graph.getDocument().getGraph(), graph.getDocument());
	                      String corpusName = corpusPath.get(corpusPath.size() - 1);
	                      corpusName = urlPathEscape.escape(corpusName);	                      
	                      List<Annotation> metadata = Helper.getMetaData(corpusName, docName);
	                      
	                      Map <String, String> annosWithoutNamespace = new HashMap<String, String>();
	                      Map <String, Map<String, String>> annosWithNamespace = new HashMap<String, Map<String, String>>();
	                      
	                      // put metadata annotations into hash maps for better access
	                      for (Annotation metaAnno : metadata){
	                    	  String ns;
	                    	  Map<String, String> data = new HashMap<String, String>();
	                    	  data.put(metaAnno.getName(), metaAnno.getValue());
	                    	  
	                    	  // a namespace is present
	                    	  if ((ns = metaAnno.getNamespace()) != null && !ns.isEmpty()){
	                    		 Map <String, String> nsMetadata = new HashMap<String, String>();
	                    		 
	                    		 if (annosWithNamespace.get(ns) != null){
	                    			 nsMetadata = annosWithNamespace.get(ns);
	                    		 }
	                    		 nsMetadata.putAll(data);
	                    		 annosWithNamespace.put(ns, nsMetadata);
	                    	  }
	                    	  else{
	                    		  annosWithoutNamespace.putAll(data);
	                    	  }
	                    	  
	                      }
	                      
	                            			 
	        			 for (String metakey : listOfMetakeys){
	        				 String metaValue = "";
	        				 //try to get meta value specific for current speaker
	        				if (annosWithNamespace.containsKey(currSpeakerName)){
	        					Map<String, String> speakerAnnos = annosWithNamespace.get(currSpeakerName);
	        					if (speakerAnnos.containsKey(metakey)){
	        						metaValue = speakerAnnos.get(metakey).trim();
	        					}
	        				}
	        				
	        				// try to get meta value 
	        				if (metaValue.isEmpty() && annosWithoutNamespace.containsKey(metakey)){
	        					metaValue = annosWithoutNamespace.get(metakey).trim();
	        				}
	        				out.append(metaValue + TAB_MARK);
	        			 }
	        		 }
	        			 
	        		 
	        		 
	        		 lastTokenWasMatched = -1;
	        		 noPreviousTokenInLine = true;
	        		 
	        		
	        	 }// header  and speaker name ready
	        	 
	        	  String separator = SPACE; // default to space as separator
	        	       	  
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
		                       if (!alignmc){
		                    	   separator = TAB_MARK; 
		                       }
		                       else{
		                    	   int orderInList = filterNumbersOrdered.indexOf(traverser.matchedNode);
		                    	   if (orderInList >= matchesWrittenForSpeaker){
		                    		   int diff = orderInList - matchesWrittenForSpeaker;
		                    		   separator = TAB_MARK; 
		                    		   matchesWrittenForSpeaker++; 		                    		   
		                    		   for (int i = 0; i < diff; i++){
		                    			   separator += (TAB_MARK + TAB_MARK);
		                    			   matchesWrittenForSpeaker++; 
		                    		   }
		                    		  
		                    	   }
		                    	   else{
		                    		   throw new IllegalArgumentException("The result of this aql-query cannot be aligned by node number." 
		                    				   + NEWLINE
		                    				   +"Please uncheck the alignment-checkbox.");
		                    	   }
		                       }
		                    	
		                                                     
		                			                      
		                    }
		                    else if(lastTokenWasMatched != (long) traverser.matchedNode)
		                    {
		                      // always leave an empty column between two matches, even if there is no actual context
		                    	 if (!alignmc){
		                    	separator = TAB_MARK + TAB_MARK;
		                    	 }
		                    	 else{
		                    		 int orderInList = filterNumbersOrdered.indexOf(traverser.matchedNode);
			                    	   if (orderInList >= matchesWrittenForSpeaker){
			                    		   int diff = orderInList - matchesWrittenForSpeaker;
			                    		   separator = TAB_MARK + TAB_MARK; 
			                    		   matchesWrittenForSpeaker++; 
			                    		   for (int i = 0; i < diff; i++){
			                    			   separator += (TAB_MARK + TAB_MARK);
			                    			   matchesWrittenForSpeaker++; 
			                    		   }
			                    		  
			                    	   }
			                    	   else{
			                    		   throw new IllegalArgumentException("The result of this aql-query cannot be aligned by node number." 
			                    				   + NEWLINE
			                    				   +"Please uncheck the alignment-checkbox.");
			                    	   }
		                    		 
		                    	 }
		                    			                    	
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
		                    				
		                    				separator = SPACE;                     
		    		                    	lastTokenWasMatched = tokenToMatchNumber.get(counter + 1);
		                    			}	                    				
	                    				else{
	                    					             						                    
	       			                    	  separator = TAB_MARK;           			                	  
	       			                    	  lastTokenWasMatched = -1;
	                    				}
	                    				
	                    				
	                    			}
		                	// mark the end of a match with the tab
			           	  else{
		                		            
			                    separator = TAB_MARK;		                	  
			                    lastTokenWasMatched = -1;
		                	  }
	                    			 	  
		                	 
		                  }
		                  
		                  //if tok is the first token in the line and not matched, set separator to empty string
		                  if (noPreviousTokenInLine && separator.equals(SPACE))
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
    

  }*/
  
  
@Override
 public void convertText(SDocumentGraph graph, List<String> annoKeys,
  Map<String, String> args, boolean alignmc, int matchNumber, Writer out) throws IOException, IllegalArgumentException
{
	 
	String currSpeakerName = "";
	String prevSpeakerName = "";

	    
  if(graph != null)
  {
    List<SToken> orderedToken = graph.getSortedTokenByText();
    
   
    
   if(orderedToken != null)
    {    	   
  	// counter over dominance lists
  	  int counter = 0;
  

   	  
  	 //iterate over token
      ListIterator<SToken> it = orderedToken.listIterator();
      long lastTokenWasMatched = -1;
      boolean noPreviousTokenInLine = false;
     
     
    //TODO why does match number start with -1? 
  	//if match number == -1, reset global variables 
  	if (matchNumber == -1){
  		isFirstSpeakerWithMatch = true;
  	}
  	
       
  	//reset counter
      counter = 0;
      int matchesWrittenForSpeaker = 0;
      
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
	        		 //reset the counter of matches, which were written for this speaker	
	        		 matchesWrittenForSpeaker = 0;
	   
	        		 if (isFirstSpeakerWithMatch){
	        			 
	        			 out.append("match_number" + TAB_MARK);
	        			 out.append("speaker" + TAB_MARK);
	        			 
	        			 // write header for meta data columns
	        			 if (!listOfMetakeys.isEmpty()){
	        				 for(String metakey : listOfMetakeys){
	        					 out.append(metakey + TAB_MARK);
	        				 }
	        			 }
	        			 
	        			 
	        			 
	        			 out.append("left_context" + TAB_MARK);
		        		 
		        		 String prefixAlignmc = "match_";
		        		 String prefix = "match_column";
		        		 String middle_context = "middle_context_";
		        		 
		        		 if (orderedMatchNumbersGlobal.size() > 0){
		        			 			 
		        			 for (int i = 0; i < orderedMatchNumbersGlobal.size(); i++){
		        				 out.append(prefixAlignmc + orderedMatchNumbersGlobal.get(i) + TAB_MARK); 
		        				 
		        				 if (i < orderedMatchNumbersGlobal.size() - 1){
			        				 out.append(middle_context +  (i + 1) + TAB_MARK); 
			        			 }      	
		        			 }
		        		 }
		        		 else{
		        			 // TODO  message, that data are not alignable
		        			 for (int i = 0; i < matchNumbersGlobal.size(); i++){
		        				 out.append(prefix + TAB_MARK); 
		        				 
		        				 if (i < matchNumbersGlobal.size() - 1){
			        				 out.append(middle_context +  (i + 1) + TAB_MARK); 
			        			 }      	
		        			 }
		        			 
		        		 }		 
		        
		        		 
		        		 out.append("right_context");
		        		 out.append(NEWLINE);
	        			 
	        			 isFirstSpeakerWithMatch = false;
	        		 }
	        		 else {
	        			 out.append(NEWLINE);
	        		 } 
	            		   		
	        		 	        		
	        		 // TODO why does matchNumber start with -1?
	        		 out.append(String.valueOf(matchNumber + 2) + TAB_MARK);
	        		 out.append(currSpeakerName + TAB_MARK);
	        		 
	        		 //write meta data
	        		 if (!listOfMetakeys.isEmpty()){
	        			 // get metadata
	        			  String docName = graph.getDocument().getName();
	                      List<String> corpusPath = CommonHelper.getCorpusPath(graph.getDocument().getGraph(), graph.getDocument());
	                      String corpusName = corpusPath.get(corpusPath.size() - 1);
	                      corpusName = urlPathEscape.escape(corpusName);	                      
	                      List<Annotation> metadata = Helper.getMetaData(corpusName, docName);
	                      
	                      Map <String, String> annosWithoutNamespace = new HashMap<String, String>();
	                      Map <String, Map<String, String>> annosWithNamespace = new HashMap<String, Map<String, String>>();
	                      
	                      // put metadata annotations into hash maps for better access
	                      for (Annotation metaAnno : metadata){
	                    	  String ns;
	                    	  Map<String, String> data = new HashMap<String, String>();
	                    	  data.put(metaAnno.getName(), metaAnno.getValue());
	                    	  
	                    	  // a namespace is present
	                    	  if ((ns = metaAnno.getNamespace()) != null && !ns.isEmpty()){
	                    		 Map <String, String> nsMetadata = new HashMap<String, String>();
	                    		 
	                    		 if (annosWithNamespace.get(ns) != null){
	                    			 nsMetadata = annosWithNamespace.get(ns);
	                    		 }
	                    		 nsMetadata.putAll(data);
	                    		 annosWithNamespace.put(ns, nsMetadata);
	                    	  }
	                    	  else{
	                    		  annosWithoutNamespace.putAll(data);
	                    	  }
	                    	  
	                      }
	                      
	                            			 
	        			 for (String metakey : listOfMetakeys){
	        				 String metaValue = "";
	        				 //try to get meta value specific for current speaker
	        				if (annosWithNamespace.containsKey(currSpeakerName)){
	        					Map<String, String> speakerAnnos = annosWithNamespace.get(currSpeakerName);
	        					if (speakerAnnos.containsKey(metakey)){
	        						metaValue = speakerAnnos.get(metakey).trim();
	        					}
	        				}
	        				
	        				// try to get meta value 
	        				if (metaValue.isEmpty() && annosWithoutNamespace.containsKey(metakey)){
	        					metaValue = annosWithoutNamespace.get(metakey).trim();
	        				}
	        				out.append(metaValue + TAB_MARK);
	        			 }
	        		 } // metadata written
	        			 
	        		 
	        		 
	        		 lastTokenWasMatched = -1;
	        		 noPreviousTokenInLine = true;
	        		 
	        		
	        	 }// header, speaker name and metadata ready
	        	 
	        	  String separator = SPACE; // default to space as separator
	        	       	  
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
		                       if (alignmc && dataIsAlignable){
		                    	   int orderInList = orderedMatchNumbersGlobal.indexOf(traverser.matchedNode);
		                    	   if (orderInList >= matchesWrittenForSpeaker){
		                    		   int diff = orderInList - matchesWrittenForSpeaker;
		                    		   separator = TAB_MARK; 
		                    		   matchesWrittenForSpeaker++; 		                    		   
		                    		   for (int i = 0; i < diff; i++){
		                    			   separator += (TAB_MARK + TAB_MARK);
		                    			   matchesWrittenForSpeaker++; 
		                    		   }	                    		   
		                    	   }
		                    	   // TODO
		                    	/*   else{
		                    		   throw new IllegalArgumentException("The result of this aql-query cannot be aligned by node number." 
		                    				   + NEWLINE
		                    				   +"Please uncheck the alignment-checkbox.");
		                    	   }*/
		                    	   
		                       }
		                       else{		                    	   
		                    	   separator = TAB_MARK; 		                    	   
		                       }
		                    	
		                                                     
		                			                      
		                    }
		                    else if(lastTokenWasMatched != (long) traverser.matchedNode)
		                    {
		                      // always leave an empty column between two matches, even if there is no actual context
		                    	 if (alignmc && dataIsAlignable){
		                    		 int orderInList = orderedMatchNumbersGlobal.indexOf(traverser.matchedNode);
			                    	   if (orderInList >= matchesWrittenForSpeaker){
			                    		   int diff = orderInList - matchesWrittenForSpeaker;
			                    		   separator = TAB_MARK + TAB_MARK; 
			                    		   matchesWrittenForSpeaker++; 
			                    		   for (int i = 0; i < diff; i++){
			                    			   separator += (TAB_MARK + TAB_MARK);
			                    			   matchesWrittenForSpeaker++; 
			                    		   }
			                    		  
			                    	   }
			                    	   //TODO
			                    	  /* else{
			                    		   throw new IllegalArgumentException("The result of this aql-query cannot be aligned by node number." 
			                    				   + NEWLINE
			                    				   +"Please uncheck the alignment-checkbox.");
			                    	   }*/
		                    	 }
		                    	 else{
		                    		
			                    	   separator = TAB_MARK + TAB_MARK;
		                    	 }
		                    			                    	
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
		                    				
		                    				separator = SPACE;                     
		    		                    	lastTokenWasMatched = tokenToMatchNumber.get(counter + 1);
		                    			}	                    				
	                    				else{
	                    					             						                    
	       			                    	  separator = TAB_MARK;           			                	  
	       			                    	  lastTokenWasMatched = -1;
	                    				}
	                    				
	                    				
	                    			}
		                	// mark the end of a match with the tab
			           	  else{
		                		            
			                    separator = TAB_MARK;		                	  
			                    lastTokenWasMatched = -1;
		                	  }
	                    			 	  
		                	 
		                  }
		                  
		                  //if tok is the first token in the line and not matched, set separator to empty string
		                  if (noPreviousTokenInLine && separator.equals(SPACE))
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
	  return "The TextColumnExporter exports matches surrounded by the context as a csv file. "
	  			+ "The columns will be separated by tab mark. <br/>"
		        + "This exporter doesn't work well for results of aql-queries with <em>overlap</em> or <em>or</em> operators.<br/><br/>"
		        + "Parameters: <br/>"
		        + "<em>metakeys</em> - comma separated list of all meta data to include in the result (e.g. "
		        + "<code>metakeys=title,comment</code>)  <br/>"
		        + "<em>filter</em> - comma separated list of all node numbers to be represented in the result as a separated column (e.g. "
		        + "<code>filter=1,2</code>) <br/>"
		        + "</br>"
		        + "Please note, if some matched nodes build a hierarchy, you can use one node number per hierarchy only. "
		        + "For instance, the matched nodes of the aql-query <br/>"
		        + "<em>cat=\"SIMPX\" > cat = \"FRAG\" >* SPK101 = \"UNINTERPRETABLE\" </em> "
		        + "build a hierarchy by definition. "
		        + "There are three node numbers 1, 2  and 3. "
		        + "However, only one of them can be used for export. "
		        + "By default it is the highest node in the hierarchy, which determine the relevant node number. "
		        + "In our example it is the node with the node number 1. "
		        + "That means, all tokens covered by node with the node number 1 will appeare in the match column. "
		        + "If desired, by filter option you can choose an other node number from the hierarchy. In our case it could be 2 or 3.";
  }
  
  @Override
  public String getFileEnding()
  {
    return "csv";
  }


@Override
public boolean isAlignable()  
 {
	return true;
 }


@Override
public void createAdjacencyMatrix(SDocumentGraph graph, List<String> annoKeys,
		Map<String, String> args, boolean alignmc, int matchNumber, Writer out,
		int nodeCount) throws IOException, IllegalArgumentException {
	String currSpeakerName = "";
	String prevSpeakerName = "";
	List <Long> matchNumbersOrdered = new ArrayList<Long>();
	
	
	//if new search, reset adjacencyMatrix, extract parameters, set by user
	if (matchNumber == -1){
		filterNumbersSetByUser.clear();
		filterNumbersIsEmpty = true;		
		listOfMetakeys.clear();		
		adjacencyMatrix = new int [nodeCount][nodeCount];
		matrixIsFilled = false;	
		singleMatchesGlobal.clear();
		orderedMatchNumbersGlobal.clear();
		matchNumbersGlobal.clear();
		dataIsAlignable = true;
		
		//initialize adjacency matrix
		for (int i = 0; i < adjacencyMatrix.length; i++){
			for (int j = 0; j < adjacencyMatrix[0].length; j++){
				adjacencyMatrix[i][j] = -1;
			}
		}
		
	
	      //extract filter numbers, if set
	      if (args.containsKey(FILTER_PARAMETER_KEYWORD)){
	    	     	 
	    	  String parameters = args.get(FILTER_PARAMETER_KEYWORD);
	    	  String [] numbers = parameters.split(PARAMETER_SEPARATOR);
	        	  for (int i=0; i< numbers.length; i++){
	    		  try {
	    			 Long number = Long.parseLong(numbers[i]);
	    			 filterNumbersSetByUser.add(number);
	     			 
	    		  }
	    		  catch(NumberFormatException e){
	    			 ;
	    		  }
	    		  
	    	  }
	    	  
	      }
	      
	      
	      if (!filterNumbersSetByUser.isEmpty())
	        {
	        	filterNumbersIsEmpty = false;
	        	
	        }
	    
	      // extract metakeys
	      if (args.containsKey(METAKEYS_KEYWORD)){
	    	  String parameters = args.get(METAKEYS_KEYWORD);
	    	  String [] metakeys = parameters.split(PARAMETER_SEPARATOR);
	    	  for (int i=0; i< metakeys.length; i++){    	
	    			 String metakey = metakeys[i].trim();
	    			 listOfMetakeys.add(metakey);  		  
	    	  }    	  
	    	  
	      }      
	      
	} // end extraction of parameters set by user
	
	
	
		
	    
    if(graph != null)
    {
      List<SToken> orderedToken = graph.getSortedTokenByText();      
      
     //iterate over all token
     if(orderedToken != null)
      {    	   
    	
    	  
 
    	 // counter over dominance lists
    	  int counter = 0;
    
    	// iterate first time over tokens to figure out which speaker has matches and to recognize the hierarchical structure of matches as well
    	  for(SToken token : orderedToken){
    		  counter++;
    		             
    		  
              STextualDS textualDS = CommonHelper.getTextualDSForNode(token, graph);
             // speakerName = textualDS.getName();
              //TODO make speaker name global unique
              currSpeakerName = textualDS.getName();
         
                        
              // reset data structures for new speaker
              if (!currSpeakerName.equals(prevSpeakerName)){
            	
            	  speakerHasMatches.clear();    	  
            	  tokenToMatchNumber.clear();        
	        	  matchNumbersOrdered.clear();
              }
           
              // TODO make  speakerHasMatches suitable for global speaker names, 
              // TODO make currSpeakerName unique over all query results and put it into     speakerHasMatches
              /*if (!speakerHasMatches.containsKey(currSpeakerName))
              {
            	  speakerHasMatches.put(currSpeakerName, false);
              }*/
                  
              
    		  List<SNode> root = new LinkedList<>();
              root.add(token);
              IsDominatedByMatch traverserSpeakerSearch = new IsDominatedByMatch();
              
                          
              //reset list
        	  dominatedMatchCodes.clear();
        	  
              
              graph.traverse(root, GRAPH_TRAVERSE_TYPE.BOTTOM_UP_DEPTH_FIRST, TRAV_SPEAKER_HAS_MATCHES, traverserSpeakerSearch); 
              
              
              if (!dominatedMatchCodes.isEmpty()){
            	                    
                  // if filter numbers not set by user, take the number of the highest match node
                  if (filterNumbersIsEmpty){
                	  // TODO make tokenToMatchNumber global over all query results
                	//  tokenToMatchNumber.put(counter, dominatedMatchCodes.get(dominatedMatchCodes.size() - 1));
                	               	  
                	  //set filter number to the ordered list
                	  if (!matchNumbersOrdered.contains(dominatedMatchCodes.get(dominatedMatchCodes.size() - 1))){
                		  matchNumbersOrdered.add(dominatedMatchCodes.get(dominatedMatchCodes.size() - 1));
              
                	  }
                  }
                  else{
                	  // take the highest match code, which is present in filterNumbers
                	  boolean filterNumberFound = false;
                	  for (int i = dominatedMatchCodes.size() - 1; i >= 0; i--){
                    	  if (filterNumbersSetByUser.contains(dominatedMatchCodes.get(i))){
                    		  // TODO make tokenToMatchNumber global over all query results
                    		//  tokenToMatchNumber.put(counter, dominatedMatchCodes.get(i));
                    		  
                    		  		  
                    		  if (!matchNumbersOrdered.contains(dominatedMatchCodes.get(i))){
                    			  if (!filterNumberFound){
                    				  matchNumbersOrdered.add(dominatedMatchCodes.get(i));
                    				  filterNumberFound = true;
                    			  }
                    			  else{
                    				 //TODO output warning 
                    			  }
                        		  
                        		 
       
                        	  }
                    		//  break;
                    	  }
                      }
                  }                  
                 
                  // fill the adjacency matrix
                    
                  if (matchNumbersOrdered.size() > 1){
                	  Iterator<Long> it = matchNumbersOrdered.iterator();
                	  
                	  int prev = Integer.parseInt(String.valueOf((Long) it.next()));
                	  matchNumbersGlobal.add((long) prev);
                	  
                	  while (it.hasNext()){                		  
                		  int curr = Integer.parseInt(String.valueOf((Long) it.next()));   
                		  matchNumbersGlobal.add((long) curr);
                		  adjacencyMatrix[prev - 1][curr - 1] = 1;
                		  matrixIsFilled = true;
                		  prev = curr;
                		  
                	  }                	 
                  }
                  else{
                	  singleMatchesGlobal.add(matchNumbersOrdered.get(0));
                	  matchNumbersGlobal.add(matchNumbersOrdered.get(0));
                  }
                 
                  
                  
              }
              
              // set previous speaker name
              prevSpeakerName = currSpeakerName;
             
                                      
    	  }
    	       
      }
     
   
       
    }
	
}

public void getOrderedMatchNumbers (){
	 for (int i = 0; i < adjacencyMatrix.length; i++){
			for (int j = 0; j < adjacencyMatrix[0].length; j++){
				System.out.print(adjacencyMatrix[i][j] + "\t");
			}
			System.out.print("\n");
		}
	  
	  System.out.println("singleMatches: " + singleMatchesGlobal);      
	 
	  
	 orderedMatchNumbersGlobal =  calculateOrderedMatchNumbersGlobal(adjacencyMatrix, matrixIsFilled, singleMatchesGlobal);
	  
	 System.out.println("orderedMatchNumbers: "  +orderedMatchNumbersGlobal);
	 System.out.println("matchNumbersGlobal: " + matchNumbersGlobal);      
	 System.out.println("dataIsAlignable: " + dataIsAlignable);      
	
}

// this method returns a list with match numbers ordered according to their occurrence, if data are alignable or empty list, if not
private static List <Long> calculateOrderedMatchNumbersGlobal(int [][] adjacencyMatrix, boolean matrixIsFilled, Set<Long> singleMatches){
	
	List <Long> orderedMatchNumbers = new ArrayList<Long>();
	
	if (matrixIsFilled){
		int first = -1;
		int second = -1;
		
		//iterate first over columns and get the first pair of match numbers
outerFor: for (int i = 0; i< adjacencyMatrix[0].length; i ++){
			for (int j = 0; j < adjacencyMatrix.length; j++){
				//first pair found
				if (adjacencyMatrix[j][i] == 1){
					if (adjacencyMatrix[i][j] != 1){
						first = j + 1;
						second = i + 1;
						orderedMatchNumbers.add((long) first);
						orderedMatchNumbers.add((long)second);	
						break outerFor;
					}
					else{
						// TODO warning or pass a parameter through
						dataIsAlignable = false;
						
						System.out.println("Data not alignable.");
						break outerFor;
					}
						
					
					
				}
			}
		}
		
		
		first = second;
		second = -1;
		int i = 0;
		// get all remained match numbers
		if (dataIsAlignable){			
		
		outerDo: do{
			
					//iterate over rows
					for (i = 0; i < adjacencyMatrix.length; i++){
						if (adjacencyMatrix[first - 1][i] == 1){
							if (adjacencyMatrix[i][first - 1] != 1 ){
								second = i + 1;
								orderedMatchNumbers.add((long) (second));
								first = second;
								second = -1;
								break;
							}
							else{
								// TODO warning or pass a parameter through
								System.out.println("Data not alignable.");
								break outerDo;
							}
							
						}
					}
			}
			while((i != adjacencyMatrix.length - 1) && (second != -1));
			
			// merge single matches into the list
		//TODO test this case
			for (Long match : singleMatches){
				if (!orderedMatchNumbers.contains(match)){
					Iterator <Long> it = orderedMatchNumbers.iterator();
					
					while (it.hasNext()){
						Long next = it.next();
						if ( next > match){
							int index = orderedMatchNumbers.indexOf(next);
							if (index > 0){
								orderedMatchNumbers.add(index - 1, match);
							}
							else{
								orderedMatchNumbers.add(0, match);
							}
							break;
						}
					}
				}
			}
	}
		
		
		
	}
	// if adjacency matrix empty, just sort single matches numerically
	else{
		orderedMatchNumbers.addAll(singleMatches);
		Collections.sort(orderedMatchNumbers);
		
	}

	if (dataIsAlignable){
		return orderedMatchNumbers;	
	}
	else return new ArrayList<Long>();
	
}
 
}
