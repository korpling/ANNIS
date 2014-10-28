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

window.annis_gui_components_NavigateableSinglePage_IFrameComponent = function() {

  var connector = this;
  var rootDiv = $(this.getElement(this.getConnectorId()));

  var ignoreScroll = false;

  function addScrollListener(iframe) {
    
    iframe.on("load", function() {
      
      var iframeContent = iframe.contents();
      // re-select last scrolled position
      if(connector.getState().lastScrollPos)
      {
        ignoreScroll = true;
        iframeContent.scrollTop(connector.getState().lastScrollPos);
      }

      iframeContent.on('scroll', function() {
    
        var top = iframeContent.scrollTop();
        var lastScrolledPos = top; // remember the last scroll position
        
        var lastInvisibleID = null;
        
        if (!ignoreScroll)
        {
          // find ID of the first header which is inside the visible range
          var headersWithID = iframeContent.find("h1[id], h2[id], h3[id], h4[id], h5[id], h6[id]");

          if (headersWithID.length > 0)
          {
            var windowHeight = iframe.height();
            var visibleBorder = top + (windowHeight / 4);

            lastInvisibleID = headersWithID.attr('id')
            // find the last header which is (even slightly) invisible
            $.each(headersWithID, function(key) {

              var offset = $(this).offset().top;

              // is invisible?
              if (offset < visibleBorder) {
                lastInvisibleID = $(this).attr('id');
              } else {
                return false;
              }
            });
          } // end if any header found
        } // end if scroll should be ignored once
        ignoreScroll = false;
        
        connector.scrolled(lastInvisibleID, lastScrolledPos);
        
      });
    });
  }

  function initElement() {
    var iframeElement = $(document.createElement("iframe"));
    rootDiv.append(iframeElement);
    iframeElement.attr("frameborder", 0);
    iframeElement.attr("width", "100%");
    iframeElement.attr("height", "100%");
    iframeElement.attr("allowtransparency", "true");
    iframeElement.attr("src", connector.getState().source);
    addScrollListener(iframeElement);
  }

  this.scrollToElement = function(id) {
    
    var iframeContent = rootDiv.find("iframe").contents();
    var element = iframeContent.find("#" + id);
    ignoreScroll = true;    
    iframeContent.scrollTop(element.offset().top);
  };

  this.onStateChange = function() {
    var iframe = rootDiv.find("iframe");
    if (iframe.length === 0)
    {
      initElement();
    }
  };
  
  this.printFrame = function() {
    var iframe = rootDiv.find("iframe");
    if(iframe.length > 0)
    {
      var iframewindow= iframe[0].contentWindow? iframe[0].contentWindow : iframe[0].contentDocument.defaultView;
      iframewindow.focus();
      iframewindow.print();
    }
  };

};
