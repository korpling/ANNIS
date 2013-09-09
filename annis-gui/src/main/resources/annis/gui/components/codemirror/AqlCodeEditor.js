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
    var component = this;
    
    var origTextArea = this.getElement(this.getParentId());
    var cmTextArea = CodeMirror.fromTextArea(origTextArea,
    {
      mode: 'aql'
    });
    
    cmTextArea.on("change", function(instance, changeObj)
    {
      instance.save();
      // simulate onchange event
      var element = instance.getTextArea();
      if ("createEvent" in document) {
        var evt = document.createEvent("HTMLEvents");
        evt.initEvent("change", false, true);
        element.dispatchEvent(evt);
      }
      else {
        element.fireEvent("onchange");
      }
    });

//    var componentDiv = this.getElement(this.getConnectorId());
//    
//    var codemirrorTextArea = CodeMirror(componentDiv, 
//    {
//      value: "test=\"Hello World\"",
//      mode: "properties"
//    });
//    
    this.onStateChange = function() {
    };
    
    this.updateText = function(newText) {
      cmTextArea.setValue(newText);
    }
    
    
};

