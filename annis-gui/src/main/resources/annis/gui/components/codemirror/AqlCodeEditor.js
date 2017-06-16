/* 
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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

//JavaScript-connector, communicates between server-side (AqlCodeEditor) and JavaScript-code (codemirror) 
window.annis_gui_components_codemirror_AqlCodeEditor = function() {
  
    var connector = this;
    var rootDiv = this.getElement(this.getConnectorId());
    
    var changeDelayTimerID = null;
    
    var changeDelayTime = 500;
    
    var lastServerRequestCounter = 0;
    
    var errorList = [];

    CodeMirror.registerHelper("lint", "aql", function(text) {
      return errorList;
    });
    
    var cmTextArea = CodeMirror(rootDiv,
    {
      mode: {
        name : 'aql',
        nodeMappings : {}
      }, 
      lineNumbers: false,
      lineWrapping: true,
      matchBrackets: true,
      gutters: ["CodeMirror-lint-markers"],
      lint: true,
      placeholder: "",
      specialChars: /[\t\u0000-\u0019\u00ad\u200b\u200c\u200d\u200f\u2028\u2029\ufeff]/g,
      inputStyle: 'textarea',
      rtlMoveVisually : true
    });
        
    this.sendTextIfNecessary = function() 
    {
      //var current = cmTextArea.getValue();
      var current = cmTextArea.getValue().replace(/\u200e/g, "");
            
      if(changeDelayTimerID)
      {
        window.clearTimeout(changeDelayTimerID);
      }
      
      if(connector.getState().text !== current)
      {
        var cursor = cmTextArea.getCursor();
        // calculate the absolute cursor position
        var absPos = 0;
        for(var i=0; i < cursor.line; i++)
        {
          absPos += cmTextArea.getLine(i).length;
          absPos++; // add one for the newline
        }
        absPos += cursor.ch;
        
        connector.textChanged(current, absPos);
      }
    };
    
       
    //handle changes from the server-side
    this.onStateChange = function() 
    {    
             
     var cursor = cmTextArea.getCursor();
      
      var newMode = {
        name: 'aql',
        nodeMappings : connector.getState().nodeMappings
      };
      
      cmTextArea.setOption('mode', newMode);
      cmTextArea.setOption("placeholder", connector.getState().inputPrompt);
      
      // set the text from the server defined state if a new request was made
      if(lastServerRequestCounter < connector.getState().serverRequestCounter)
      {
        lastServerRequestCounter = connector.getState().serverRequestCounter;
        cmTextArea.setValue(connector.getState().text);

        // restore the cursor position
        cmTextArea.setCursor(cursor);
      }
      
      // apply parent code class
      if(connector.getState().textareaClass && connector.getState().textareaClass !== "") {
        var c = connector.getState().textareaClass;
        if(!$(cmTextArea.getWrapperElement()).find("pre").hasClass(c)) {
          $(cmTextArea.getWrapperElement()).find("pre").addClass(c);
        }
      } 
      
      
      // copy all error messages
      errorList = [];
      for(var i=0; i < connector.getState().errors.length; i++)
      {
        var err = connector.getState().errors[i];
        var endColumn = err.endColumn+1;
        errorList.push({
          from: CodeMirror.Pos(err.startLine-1, err.startColumn),
          to: CodeMirror.Pos(err.endLine-1, endColumn),
          message: err.message
        });
      }
      // hack: re-initialize the lint by re-setting the option
      cmTextArea.setOption("lint", false);
      cmTextArea.setOption("lint", true);
    };
    
    this.setChangeDelayTime = function(newDelayTime) {
      changeDelayTime = newDelayTime;
    };
    
   
    //pass user interaction to the server-side
    cmTextArea.on("change", function(instance, changeObj)
    {  
 
    	
     var LRM = '\u200e';
	 var quotationMark = "\"";
	 var regexMark = "/";
     // TODO test behavior, cursor position, display of token, especially with u0627 at the and
  	 var lineCount = cmTextArea.lineCount();   	  
  	  for (var i = 0; i < lineCount; i++) 
  	  {
  		  var lineValue = cmTextArea.getLine(i);
  		  var j = 0;
  		  var pos = 0;
  		  var textMarker;	
  		 
  		  
  		  //find all occurrences of \"
  		  while ((pos = lineValue.indexOf(quotationMark, j)) != -1)
  		  {
  			  j = pos + 1;
  			  // if LRM already inserted, bind LRM and \" together
  			  if (lineValue.charAt(pos - 1) == LRM)
  			  {	  textMarker = cmTextArea.findMarksAt({line: i, ch: pos -1});
  			  
  				  if (textMarker.length == 0)
  				  {
  					  cmTextArea.markText({line: i, ch: (pos - 1)}, {line: i, ch: (pos + 1)}, {atomic: true});  
  				  }
  				 
  		    	 
  			 }
  			  //else insert LRM and bind together
  			  else if (lineValue.charAt(pos - 1) != LRM || lineValue.charAt(pos - 1) === undefined)
  			  {				 
  			              cmTextArea.replaceRange(LRM, {line: i, ch: pos}); 
                          cmTextArea.markText({line: i, ch: pos}, {line: i, ch: (pos + 2)}, {atomic: true});
                      
  			         
  			         j += 1;
  			         
  			  }
  			  lineValue = cmTextArea.getLine(i);
  			
  	      }
  		       
  		  
  	  } 	  
  	  	
    	if(changeDelayTimerID)
      {
        window.clearTimeout(changeDelayTimerID);
      }
      changeDelayTimerID = window.setTimeout(connector.sendTextIfNecessary, changeDelayTime);
    });
    
    // While the text changing event has a timeout before it is send
    // to the server we must be sure it has always the current text
    // whenever the user leaves the textfield, otherwise the query might be old.
    cmTextArea.on("blur", function(instance)
    {
      connector.sendTextIfNecessary();
    });
    
    
};

