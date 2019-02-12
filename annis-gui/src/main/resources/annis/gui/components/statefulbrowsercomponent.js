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

window.annis_gui_components_StatefulBrowserComponent_IFrameComponent = function () {

  var connector = this;
  var rootDiv = $(this.getElement(this.getConnectorId()));

  function addScrollListener(iframe) {

    iframe.on("load", function () {
      var iframeContent = iframe.contents();

      // remember the currently loaded URL
      try {
        var newUrl = iframe[0].contentWindow.location.href;
        if (newUrl == connector.getState().source) {
          // re-select last scrolled position
          if (connector.getState().lastScrollPos) {
            iframeContent.scrollTop(connector.getState().lastScrollPos);
          }
        } else {
          connector.urlChanged(newUrl);
        }
      } catch(e) {
        // ignore security exception
      }

      iframeContent.on('scroll', function () {

        var top = iframeContent.scrollTop();
        var lastScrolledPos = top; // remember the last scroll position

        connector.scrolled(lastScrolledPos);

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


  this.onStateChange = function () {
    var iframe = rootDiv.find("iframe");
    if (iframe.length === 0) {
      initElement();
    }
  };

};
