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
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;

import annis.CommonHelper;
import annis.libgui.Helper;
import annis.model.AnnisConstants;
import annis.model.Annotation;
import annis.service.objects.SubgraphFilter;
import java.util.Objects;
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
	private static final String TRAV_PREPROCESSING = "travPreprocessing";
	public static final String FILTER_PARAMETER_KEYWORD = "filter";
	public static final String PARAMETER_SEPARATOR = ",";
	public static final String METAKEYS_KEYWORD = "metakeys";
	private static final String NEWLINE = System.lineSeparator();    
	private static final String TAB_MARK = "\t"; 
	private static final String SPACE = " ";    
	private final HashMap <String, Boolean> speakerHasMatches = new HashMap<>();
	private String speakerName = "";
	private boolean isFirstSpeakerWithMatch = true;   
	private final List <Long> dominatedMatchCodes = new ArrayList<>();
	// a helping structure to handle crossing edges, must be global over all query results
	private final Map <Long, Long> tokenToMatchNumber = new HashMap <>();
	// contains filter numbers from ui, global over all query results
	private final Set<Long> filterNumbersSetByUser = new HashSet<>(); 
	// indicate, whether filter numbers were set by user, global over all query results
	private boolean filterNumbersIsEmpty = true;		
	// contains metakeys, set by user, global over all query results
	private final List<String> listOfMetakeys = new ArrayList<>(); 

	// a helping structure to determine the right order of match nodes over all records
	private int [][] adjacencyMatrix;
	//indicates, whether the adjacency matrix is filled or not
	private boolean matrixIsFilled = false;
	// contains single match codes per speaker, globally over all query results
	private final Set <Long> singleMatchesGlobal = new HashSet <>();
	// contains a  sequence of match numbers per speaker ordered according to their occurrence in text, globally over all query results
	private List <Long> orderedMatchNumbersGlobal = new ArrayList <>();
	// set of match numbers, globally over all query results
	private final  Set <Integer> matchNumbersGlobal = new HashSet <>();	
	// indicates, whether data is alignable or not
	private boolean dataIsAlignable = true;
	//max. count of matches per line
	private int maxMatchesPerLine = 0;
	//counter over token, globally over all query results
	private long counterGlobal;
	
	 	 
  
	
  private class IsDominatedByMatch implements GraphTraverseHandler
  {
   
    Long matchedNode = null;
    
    /**
	 * Implements the nodeReached method of the {@link org.corpus_tools.salt.core.GraphTraverseHandler} interface.
	 */
    @Override
    public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        SRelation<SNode, SNode> relation, SNode fromNode, long order)
    {
    	 SFeature matchedAnno = currNode.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE);
    	 
    	 if(matchedAnno != null && (filterNumbersSetByUser.contains(matchedAnno.getValue_SNUMERIC()) 
    			 || filterNumbersIsEmpty || orderedMatchNumbersGlobal.contains(matchedAnno.getValue_SNUMERIC())))
	      {
	        matchedNode = matchedAnno.getValue_SNUMERIC();	       
	        dominatedMatchCodes.add(matchedNode);
	        speakerHasMatches.put(speakerName, true);
	      }
	      
	     
	      
	      
    }
    
    /**
	 * Implements the nodeLeft method of the {@link org.corpus_tools.salt.core.GraphTraverseHandler} interface.
	 */
    @Override
    public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        SRelation<SNode, SNode> relation, SNode fromNode, long order)
    {
   
     
    }

    /**
	 * Implements the checkConstraint method of the {@link org.corpus_tools.salt.core.GraphTraverseHandler} interface.
	 */
    @Override
    public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
        SRelation relation, SNode currNode, long order)
    {    	
		return 
	            relation == null
	            || relation instanceof SDominanceRelation 
	            || relation instanceof SSpanningRelation;
   
    } 
  }

  
  /**
   * Writes the specified record (if applicable, as multiple result lines) from query result set to the output file.
   * 
   * @param graph the org.corpus_tools.salt.common.SDocumentGraph representation of a specified record
   * @param alignmc a boolean, which indicates, whether the data should be aligned by match numbers or not
   * @param recordNumber the number of record within  the record set
   * @param out the specified Writer
   * 
   * @throws IOException, if an I/O error occurs
   *  
   */
  
@Override
 public void outputText(SDocumentGraph graph, boolean alignmc, int recordNumber, Writer out) throws IOException
{
	
	
	 
	String currSpeakerName = "";
	String prevSpeakerName = "";

	    
  if(graph != null)
  {
    List<SToken> orderedToken = graph.getSortedTokenByText();
    
       
   if(orderedToken != null)
    {    	   
  	  	  
  	 //iterate over token
      ListIterator<SToken> it = orderedToken.listIterator();
      long lastTokenWasMatched = -1;
      boolean noPreviousTokenInLine = false;
     
     
  	//if match number == 0, reset global variables and output warning, if necessary
  	if (recordNumber == 0){
  		isFirstSpeakerWithMatch = true;
  		counterGlobal = 0;
  		
  		// create warning message
		 String numbersString = "";        				
		 String warnMessage = "";
		 StringBuilder sb = new StringBuilder();
		 
		 List <Integer> copyOfFilterNumbersSetByUser = new ArrayList <Integer>();
		 
		 for (Long filterNumber : filterNumbersSetByUser){
			 copyOfFilterNumbersSetByUser.add(Integer.parseInt(String.valueOf(filterNumber)));
		 }
		 
		 for (Integer matchNumberGlobal : matchNumbersGlobal){
			 copyOfFilterNumbersSetByUser.remove(matchNumberGlobal);
		 }
		 
		 Collections.sort(copyOfFilterNumbersSetByUser);
		 
		 if (!copyOfFilterNumbersSetByUser.isEmpty()){
			 for (Integer filterNumber : copyOfFilterNumbersSetByUser){	        					
				 sb.append(filterNumber + ", ");
			 }
			
			
			 if (copyOfFilterNumbersSetByUser.size() == 1){
				numbersString = "number";
			 }
			 else {
				 numbersString = "numbers";
			 }
			
			 warnMessage =  "1. Filter " + numbersString + " " + sb.toString().substring(0, sb.lastIndexOf(",")) 
					 	+ " couldn't be represented.";
			        				 
			
		 }
		 
		 if (alignmc && !dataIsAlignable){
			if (!warnMessage.isEmpty()){
				warnMessage += (NEWLINE  + NEWLINE  + "2. ");
			}
			else{
				warnMessage += "1. ";
			}
			
			warnMessage += "You have tried to align matches by node number via check box."
					+ "Unfortunately this option is not applicable for this data set, "
					+ "so the data couldn't be aligned.";

		 }
		 
		 if (!warnMessage.isEmpty()){
			 
			 String warnCaption = "Some export options couldn't be realized.";
			 Notification warn = new Notification(warnCaption, warnMessage, Notification.Type.WARNING_MESSAGE);	        	     			
			 warn.setDelayMsec(20000);
			 warn.show(Page.getCurrent());
		 }
  	} // global variables reset; warning issued
  	
       
      int matchesWrittenForSpeaker = 0;
      
      while(it.hasNext())
      {    	
        SToken tok = it.next();    
        counterGlobal++;
        //get current speaker name
        String name;
        if ((name = CommonHelper.getTextualDSForNode(tok, graph).getName()) == null){
        	name = "";
        }
         
        currSpeakerName = (recordNumber + 1) + "_" + name;
        
         
        // if speaker has no matches, skip token
        if (speakerHasMatches.get(currSpeakerName) == false)
        {
      	  prevSpeakerName = currSpeakerName;
      	 // continue;
        }
        
        //if speaker has matches
        else
        {	 
 	 
      	  
	        	  //if the current speaker is new, write header and append his name 
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
		        		 
		        		 
		        		 if (alignmc && dataIsAlignable){
		        			 			 
		        			 for (int i = 0; i < orderedMatchNumbersGlobal.size(); i++){
		        				 out.append(prefixAlignmc + orderedMatchNumbersGlobal.get(i) + TAB_MARK); 
		        				 
		        				 if (i < orderedMatchNumbersGlobal.size() - 1){
			        				 out.append(middle_context +  (i + 1) + TAB_MARK); 
			        			 }      	
		        			 }
		        		 }
		        		 else{
		        			
		        			 for (int i = 0; i < maxMatchesPerLine; i++){
		        				 out.append(prefix + TAB_MARK); 
		        				 
		        				 if (i < (maxMatchesPerLine - 1)){
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
	            		   		
	        		 	        		
	        		 
	        		 out.append(String.valueOf(recordNumber + 1) + TAB_MARK);
	        		 
	        		 String trimmedName = "";
	        		 if (currSpeakerName.indexOf("_") <  currSpeakerName.length()){
    					 trimmedName = currSpeakerName.substring(currSpeakerName.indexOf("_") + 1);
    				 }
	        		 
	        		 out.append(trimmedName + TAB_MARK);
	        		 
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
	        				if (!trimmedName.isEmpty() && annosWithNamespace.containsKey(trimmedName)){
	        					
	        					Map<String, String> speakerAnnos = annosWithNamespace.get(trimmedName);
	        					if (speakerAnnos.containsKey(metakey)){
	        						metaValue = speakerAnnos.get(metakey).trim();
	        					}
	        				}
	        				
	        				// try to get meta value, if metaValue is not set 	        				
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
		                		               
		                  Long matchedNode;
		                  // token matched
		                  if ((matchedNode =tokenToMatchNumber.get(counterGlobal)) != null)
		                  {
		                    // is dominated by a (new) matched node, thus use tab to separate the non-matches from the matches
		                    if(lastTokenWasMatched < 0)
		                    {
		                       if (alignmc && dataIsAlignable){
		                    	   int orderInList = orderedMatchNumbersGlobal.indexOf(matchedNode);
		                    	   if (orderInList >= matchesWrittenForSpeaker){
		                    		   int diff = orderInList - matchesWrittenForSpeaker;
		                    		   matchesWrittenForSpeaker++; 		
                               
                               StringBuilder sb = new StringBuilder(TAB_MARK);
		                    		   for (int i = 0; i < diff; i++){
                                 sb.append(TAB_MARK + TAB_MARK);
		                    			   matchesWrittenForSpeaker++; 
		                    		   }	                    		   
                               separator = sb.toString();
		                    	   }
		            		                    	   
		                       }
		                       else{		                    	   
		                    	   separator = TAB_MARK; 		                    	   
		                       }
		                    	
		                                                     
		                			                      
		                    }
		                    else if(lastTokenWasMatched != matchedNode)
		                    {
		                      // always leave an empty column between two matches, even if there is no actual context
		                    	 if (alignmc && dataIsAlignable){
		                    	   	   int orderInList = orderedMatchNumbersGlobal.indexOf(matchedNode);
			                    	   if (orderInList >= matchesWrittenForSpeaker){
			                    		   int diff = orderInList - matchesWrittenForSpeaker;
			                    		   matchesWrittenForSpeaker++; 
                                 
                                 StringBuilder sb = new StringBuilder(TAB_MARK + TAB_MARK);
			                    		   for (int i = 0; i < diff; i++){
                                   sb.append(TAB_MARK + TAB_MARK);
			                    			   matchesWrittenForSpeaker++; 
			                    		   }
                                 
                                 separator = sb.toString(); 
			                    		   
			                    		  
			                    	   }
			                    	  
		                    	 }
		                    	 else{
		                    		
			                    	   separator = TAB_MARK + TAB_MARK;
		                    	 }
		                    			                    	
		                    }
		                    lastTokenWasMatched = matchedNode;
		                  }
		                  // token not matched, but last token matched
		                  else if(lastTokenWasMatched >= 0)
		                  {
		                                    	  
		                	  //handle crossing edges
		                	  if(!tokenToMatchNumber.containsKey(counterGlobal) && 
		                			  tokenToMatchNumber.containsKey(counterGlobal - 1) && tokenToMatchNumber.containsKey(counterGlobal + 1)){
		                		    
		                		  
		                    			if (Objects.equals(tokenToMatchNumber.get(counterGlobal - 1), 
                                    tokenToMatchNumber.get(counterGlobal + 1))){
		                    				
		                    				separator = SPACE;                     
		    		                    	lastTokenWasMatched = tokenToMatchNumber.get(counterGlobal + 1);
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
		           
		       	          
		          
        // append the current token
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
  
  /**
   * @return a String, that represents a short description of this exporter 
   */
  @Override
  public String getHelpMessage()
  {
	  return "The TextColumnExporter exports matches surrounded by the context as a csv file. "
	  			+ "The columns will be separated by tab mark. <br/>"
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
  

  /**
   * @see annis.gui.exporter.SaltBasedExporter#getFileEnding()
   * 
   * @return a constant String  ("csv") for file ending
   */
  @Override
  public String getFileEnding()
  {
    /* 
      On Windows and Excel "csv" won't trigger the configuration dialog and will
      use different defaults for importing the data depending on the locale of Windows.
      Thus use the more general "txt" ending which will trigger a configuration dialog.
    */
    return "txt";
  }

/**
 * Indicates, that this exporter is alignable, so the check box will appear in the [ExportPanel](\ref annis.gui.ExportPanel), 
 * if this exporter is chosen.
 * 
 * @return always true, since this exporter is alignable
 */
@Override
public boolean isAlignable()  
 {
	return true;
 }

/**
 * Implements the abstract method of the [SaltBasedExporter](\ref annis.gui.exporter.SaltBasedExporter).
 * This method creates and fills an adjacency matrix of dimension (nodeCount x nodeCount), which keeps the relative order 
 * of match numbers to each other of each query result line. A result line is a part of a record, which belongs to a speaker.
 * 
 * The adjacency matrix is a global two-dimensional array of integers, which allows to recognize the valid order of match numbers globally, after all query results are processed. 
 *
 * @param graph  an org.corpus_tools.salt.common.SDocumentGraph representation of a record 
 * @param args a map containing parameters like 'filter' or 'metakeys', set by user
 * @param recordNumber the number of record within  the record set returned for the user query 
 * @param nodeCount the count of distinct match numbers in the whole record set returned for the user query
 */
@Override
public void createAdjacencyMatrix(SDocumentGraph graph, Map<String, String> args, int recordNumber, int nodeCount) throws IOException {
	String currSpeakerName = "";
	String prevSpeakerName = "";
	List <Long> matchNumbersOrdered = new ArrayList<Long>();
	
	
	//if new search, reset adjacencyMatrix, extract parameters, set by user
	if (recordNumber == 0){
		speakerHasMatches.clear();
		speakerName = "";
		tokenToMatchNumber.clear();
		filterNumbersSetByUser.clear();
		filterNumbersIsEmpty = true;		
		listOfMetakeys.clear();		
		adjacencyMatrix = new int [nodeCount][nodeCount];
		matrixIsFilled = false;	
		singleMatchesGlobal.clear();
		orderedMatchNumbersGlobal.clear();
		matchNumbersGlobal.clear();
		dataIsAlignable = true;
		maxMatchesPerLine = 0;
		
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
    	 // reset counter over all the tokens
    	 if (recordNumber == 0){
    		 counterGlobal = 0;
    	 }
    	
    
    	// iterate first time over tokens to figure out which speaker has matches and to recognize the hierarchical structure of matches as well
    	  for(SToken token : orderedToken){
    		  counterGlobal++;
              
              String name;
              
              if ((name = CommonHelper.getTextualDSForNode(token, graph).getName()) == null){
              	name = "";
              }
              speakerName =  (recordNumber + 1) + "_" + name;
              currSpeakerName = speakerName;
         
                        
              // reset data structures for new speaker
              if (!currSpeakerName.equals(prevSpeakerName)){
             
	        	  matchNumbersOrdered.clear();
              }
           
              
              if (!speakerHasMatches.containsKey(currSpeakerName))
              {
            	  speakerHasMatches.put(currSpeakerName, false);
              }
                  
              
    		  List<SNode> root = new LinkedList<>();
              root.add(token);
              IsDominatedByMatch traverserSpeakerSearch = new IsDominatedByMatch();
              
                          
              //reset list
        	  dominatedMatchCodes.clear();
        	  
              
              graph.traverse(root, GRAPH_TRAVERSE_TYPE.BOTTOM_UP_DEPTH_FIRST, TRAV_PREPROCESSING, traverserSpeakerSearch); 
              
              
              if (!dominatedMatchCodes.isEmpty()){
            	                    
                  // if filter numbers not set by user, take the number of the highest match node
                  if (filterNumbersIsEmpty){
               
                	  tokenToMatchNumber.put(counterGlobal, dominatedMatchCodes.get(dominatedMatchCodes.size() - 1));
                	               	  
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
                    		  tokenToMatchNumber.put(counterGlobal, dominatedMatchCodes.get(i));
                    		  
                    		  		  
                    		  if (!matchNumbersOrdered.contains(dominatedMatchCodes.get(i))){
                    			  if (!filterNumberFound){
                    				  matchNumbersOrdered.add(dominatedMatchCodes.get(i));
                    				  filterNumberFound = true;
                    			  }            		 
       
                        	  }
                    		  break;
                    	  }
                      }
                  }                  
                 
                  // reset maxMatchesPerLine
                  if (maxMatchesPerLine < matchNumbersOrdered.size()){
                	  maxMatchesPerLine = matchNumbersOrdered.size();
                  }
                  
                  
                  // fill the adjacency matrix                    
                  if (matchNumbersOrdered.size() > 1){
                	  Iterator<Long> it = matchNumbersOrdered.iterator();
                	  
                	  int prev = Integer.parseInt(String.valueOf((Long) it.next()));
                	  matchNumbersGlobal.add(prev);
                	  
                	  while (it.hasNext()){                		  
                		  int curr = Integer.parseInt(String.valueOf((Long) it.next()));  
                		  matchNumbersGlobal.add(curr);
                		  adjacencyMatrix[prev - 1][curr - 1] = 1;
                		  matrixIsFilled = true;
                		  prev = curr;
                		  
                	  }                	 
                  }
                  else{
                	  matchNumbersGlobal.add(Integer.parseInt(String.valueOf(matchNumbersOrdered.get(0))));
                	  singleMatchesGlobal.add(matchNumbersOrdered.get(0));
                  }
                 
                  
                  
              }
              
              // set previous speaker name
              prevSpeakerName = currSpeakerName;
             
                                      
    	  }
    	       
      }
     
   
       
    }
	
}

/**
 * Invokes the private static method {@link #calculateOrderedMatchNumbersGlobally}.
 */
   @Override	 
   public void getOrderedMatchNumbers (){	 
		  
	 orderedMatchNumbersGlobal =  calculateOrderedMatchNumbersGlobally(adjacencyMatrix, matrixIsFilled, singleMatchesGlobal);
	 	  	
}

/**
 *  This method determine a valid order of match numbers and returns them as a list. 
 *  If the underlying result set is not alignable,  it returns an empty list.
 *  
 * @param adjacencyMatrix 
 * @param matrixIsFilled a boolean, which indicates, whether the adjacency is filled or not 
 * @param singleMatches a list of singleton match numbers
 * 
 * @return a list of globally ordered match numbers, if data alignable or an empty list otherwise
 */
private List <Long> calculateOrderedMatchNumbersGlobally(int [][] adjacencyMatrix, boolean matrixIsFilled, Set<Long> singleMatches){
	
	List <Long> orderedMatchNumbers = new ArrayList<Long>();
	
	if (matrixIsFilled){
		int first = -1;
		int second = -1;		
	
		//iterate over columns and get the pairs of match numbers
		outerFor: for (int i = 0; i< adjacencyMatrix[0].length; i ++){
					for (int j = 0; j < adjacencyMatrix.length; j++){
						//a match number pair found
						if (adjacencyMatrix[j][i] == 1){
							if (adjacencyMatrix[i][j] != 1){
								first = j + 1;
								second = i + 1;	
								//  first number is already in the list
								if (orderedMatchNumbers.contains((long) first)){
									if (!orderedMatchNumbers.contains((long) second)){
										//add the second one, if absent
										orderedMatchNumbers.add((long)second);	
									}									
								}
								//first number is not in the list
								else{
									if (orderedMatchNumbers.contains((long) second)){
										int index = orderedMatchNumbers.indexOf((long) second);
										orderedMatchNumbers.add(index, (long) first);										
									}
									else{
										orderedMatchNumbers.add((long) first);
										orderedMatchNumbers.add((long)second);	
									}
									
								}
							}
							else{
								dataIsAlignable = false;						
								break outerFor;
							}
								
							
							
						}
					}
				} 	
			
			
			// merge single matches into the list
			if (dataIsAlignable){
				for (Long match : singleMatches){		
					
					if (!orderedMatchNumbers.contains(match)){
						
						boolean matchIsMerged = false;						
						Iterator <Long> it = orderedMatchNumbers.iterator();
						
						while (it.hasNext()){
							Long next = it.next();
							if ( next > match){
							
								int index = orderedMatchNumbers.indexOf(next);
								orderedMatchNumbers.add(index, match);
								matchIsMerged = true;								
								break;
							}
						}
						
						if (!matchIsMerged){
							orderedMatchNumbers.add(match);
						}
					}
				}
			}
	
		}
		
	// if adjacency matrix empty, just sort single matches numerically
	else{
		for (Long match : singleMatches){
			orderedMatchNumbers.add(match);
		}
		Collections.sort(orderedMatchNumbers);
		
	}

	if (dataIsAlignable){
		return orderedMatchNumbers;	
	}
	else return new ArrayList<Long>();
	
}
 
}
