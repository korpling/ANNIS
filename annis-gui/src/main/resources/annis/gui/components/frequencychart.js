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


window.annis_gui_components_FrequencyWhiteboard = function() {
  var div = this.getElement();
  var theThis = this;
  
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
  
    var d = [];
    for(var i=0; i < values.length; i++)
    {
      d[i] = [i, values[i]];
    }
    
    var t = [];
    for(var i=0; i < labels.length; i++)
    {
      t[i] = [i];
    }
    
    var graph = Flotr.draw(
      div,
      [d],
      {
        bars : {
          show: true
        },
        xaxis : {
          ticks: t,
          labelsAngle: 45,
          tickFormatter: function(i){

            console.log("tickformatting for " + i);
            var l = labels[i];
            console.log("tick length: " + l.length);
            if(l.length > 25) {
              l = l.substring(0,24)+"...";
              console.log("tick resized: " + l);
            }

            return l;
          } 
        },
        mouse : {
          track : true,
          relative : true,
          trackFormatter: function(val) {
            return labels[parseInt(val.x)];
          }
        },
        HtmlText : false,
        fontSize : 10.0
      }
    );
    
    // bind click event
    graph.observe(div, 'flotr:click', function (position) {
      theThis.selectRow(position.hit.index);
    }); 
    
    lastLabels = labels;
    lastValues = values;
    
  };
};