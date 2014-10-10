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

window.annis_gui_components_NavigateableSinglePage_IFrameComponent = function () {

  var connector = this;
  var rootDiv = $(this.getElement(this.getConnectorId()));

  function initElement() {
    var iframeElement = $(document.createElement("iframe"));
    rootDiv.append(iframeElement);
    iframeElement.attr("frameborder", 0);
    iframeElement.attr("width", "100%");
    iframeElement.attr("height", "100%");
    iframeElement.attr("allowtransparency", "true");
    iframeElement.attr("src", connector.getState().source);

    var iframeWindow = $(iframeElement.get(0).contentWindow);
    iframeWindow.on('scroll', function () {

      // find ID of the first header which is inside the visible range
      var headersWithID = $(iframeWindow.get(0).document).find("h1[id], h2[id], h3[id], h4[id], h5[id], h6[id]");

      if (headersWithID.length > 0)
      {
        var top = iframeWindow.scrollTop();
        var windowHeight = iframeWindow.height();
        var visibleBorder = top + (windowHeight / 3);


        var lastInvisibleID = headersWithID.attr('id')
        // find the last header which is (even slightly) invisible
        $.each(headersWithID, function (key) {

          var offset = $(this).offset().top;

          // is invisible?
          if (offset < visibleBorder) {
            lastInvisibleID = $(this).attr('id');
          } else {
            return false;
          }
        });
        connector.scrolled(lastInvisibleID);
      }


    });
  }

  this.onStateChange = function () {
    var iframe = rootDiv.find("iframe");
    if (iframe.length === 0)
    {
      initElement();
    }
    else
    {
      iframe.attr("src", connector.getState().source);
    }
  };

};
