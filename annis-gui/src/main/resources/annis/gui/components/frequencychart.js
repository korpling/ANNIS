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


window.annis_gui_components_FrequencyChart = function() {
  var div = this.getElement();
  var canvas = document.createElement('canvas');
  var theThis = this;
  div.appendChild(canvas);
  
  var lastValues = null;
  var lastLabels = null;
  
  // always resize the canvas to the size of the parent div
  $(window).resize(function() {    
    if (lastValues !== null && lastLabels !== null)
    {
      theThis.showData(lastLabels, lastValues);
    }
  });

  this.onStateChange = function() {    
  };
  
  this.showData = function(labels, values) {
  
    canvas.width = $(div).width();
    canvas.height = $(div).height();

  
    var data = {
      labels: labels,
      datasets: [
        {
          fillColor: "rgba(220,220,220,0.5)",
          strokeColor: "rgba(220,220,220,1)",
          data: values
        }
      ]
    };
    
    var ctx = canvas.getContext("2d");
    var chart = new Chart(ctx).Bar(eval(data));
    
    lastLabels = labels;
    lastValues = values;
    
  };
};