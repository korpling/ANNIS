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
window.annis_gui_components_codemirror_AqlCodeEditor = function() {
  
    var connector = this;
    var rootDiv = this.getElement(this.getConnectorId());
    
    var changeDelayTimerID = null;
    var lastSentText = "";
    
    var changeDelayTime = 500;

    var cmTextArea = CodeMirror(rootDiv,
    {
      mode: 'aql'
    });
    
    
    function changeDelayCallback () 
    {
      var current = cmTextArea.getValue();
      if(lastSentText !== current)
      {
        var cursor = cmTextArea.getCursor();
        // calculate the absolute cursor position
        var absPos = 0;
        for(i=0; i < cursor.line; i++)
        {
          absPos += cmTextArea.getLine(i).length;
          absPos++; // add one for the newline
        }
        absPos += cursor.ch;
        
        connector.textChanged(cmTextArea.getValue(), absPos);
        lastSentText = current;
      }
    };

    
    this.onStateChange = function() 
    {
      cmTextArea.setValue(connector.getState().text);
    };
    
    this.setChangeDelayTime = function(newDelayTime) {
      changeDelayTime = newDelayTime;
    };
    
    this.updateText = function(newText) {
      
    };
    
    cmTextArea.on("change", function(instance, changeObj)
    {
      if(changeDelayTimerID)
      {
        window.clearTimeout(changeDelayTimerID);
      }
      changeDelayTimerID = window.setTimeout(changeDelayCallback, changeDelayTime);
    });
    
};

