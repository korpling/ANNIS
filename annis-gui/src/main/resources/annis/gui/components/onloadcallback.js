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

window.annis_gui_components_OnLoadCallbackExtension = function() {
  var component = this;
  var elem = this.getElement(this.getParentId());

  var timeoutID;
  
  this.recallCallback = function() {
    if(timeoutID) {
      //console.debug("clearing timeout " + timeoutID);
      window.clearTimeout(timeoutID);
      timeoutID = null;
    }
    //console.debug("calling component loading callback");
    component.loaded();
    
  };
  
  var alreadyLoaded = false;

  $(document).ready(function() {
    if(!alreadyLoaded)
    {
      component.loaded();
    }
    alreadyLoaded = true;
   });

  this.requestRecall = function(delay) {
    if(!timeoutID) {
      timeoutID = window.setTimeout(this.recallCallback, delay);
      //console.debug("Setted timeout " + timeoutID);
    }
  };
};
