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
  var lastFontFamily = "sans-serif";
  var lastScale = null;
  var lastFontSize = 10.0;

  
  this.onStateChange = function() { 
    showData(lastLabels, lastValues, lastScale, lastFontFamily, lastFontSize);
  };
  
  
  this.showData = function(labels, values, scale, fontFamily, fontSize) {    
    if(!labels || !values || !scale)
    {
      alert("invalid call to showData");
      return;
    }
    
    var predefinedYTicks = null;
    // the list is ordered
    var maxValue = values[0];
    if(scale === "logarithmic") {
      // adjust ticks manually since the flotr2 library does not handle this well
      predefinedYTicks = [];
      predefinedYTicks.push(0);
      predefinedYTicks.push(1);
      
      var lastTick = 1;
      do
      {
        lastTick = lastTick * 10;
        predefinedYTicks.push(lastTick);
      }
      while (lastTick < maxValue);
    }
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
    
    $(div).remove("canvas");
    
    var graph = Flotr.draw(
      div,
      [d],
      {
        bars : {
          show: true
        },
        yaxis : {
          scaling: scale,
          
          ticks: predefinedYTicks,
          min: 0
        },
        xaxis : {
          ticks: t,
          labelsAngle: 45,
          tickFormatter: function(i){

            var l = labels[i];
            if(l.length > 20) {
              l = l.substring(0,19)+"...";
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
        fontSize : fontSize,
        fontFamily: fontFamily
      }
    );
        
    // bind click event
    graph.observe(div, 'flotr:click', function (position) {
      theThis.selectRow(position.hit.index);
    }); 
    
    lastLabels = labels;
    lastValues = values;
    lastScale = scale;
    lastFontFamily = fontFamily;
    lastFontSize = fontSize;
    
  };
  
  // always resize the canvas to the size of the parent div
  $(window).resize(function() {
    theThis.showData(lastLabels, lastValues, lastScale, lastFontFamily, lastFontSize);
  });
};