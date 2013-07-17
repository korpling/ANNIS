/* 
 * Copyright 2013 SFB 632.
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

window.annis_gui_components_medialement_MediaElementPlayer =
function() {
    var connector = this;
    var rootDiv = $(this.getElement(this.getParentId()));
  
    var mediaElement = null;
    var player = null;
    
    //TODO: Create the component
   
    // Handle changes from the server-side
    this.onStateChange = function() {
      if(mediaElement === null)
        {
          mediaElement = $(document.createElement(this.getState().elementType));
          rootDiv.append(mediaElement);
          
          mediaElement.attr("controls", "controls");
          
          var mediaElementSrc = $(document.createElement("source"));
          mediaElement.append(mediaElementSrc);
          
          mediaElementSrc.attr("type", this.getState().mimeType);
          mediaElementSrc.attr("src", this.getState().resourceURL);
          
          var options = {};
          options.alwaysShowControls=true;
          player = new MediaElementPlayer(mediaElement.get(), options);
          player.play();
        }
      // TODO
    };

    // Pass user interaction to the server-side
    //mycomponent.click = function() {
      // TODO
      //  connector.onClick(mycomponent.getValue());
    //};
};
