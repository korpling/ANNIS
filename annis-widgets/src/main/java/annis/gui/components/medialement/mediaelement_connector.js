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

window.annis_gui_components_medialement_MediaElementPlayer = function() {
  var connector = this;
  var rootDiv = $(this.getElement(this.getParentId()));

  var mediaElement = null;
  var globalPlayer = null;

  // Handle changes from the server-side
  this.onStateChange = function() {
    if (!mediaElement)
    {
      mediaElement = $(document.createElement(this.getState().elementType));
      rootDiv.append(mediaElement);

      mediaElement.attr("controls", "controls");
      mediaElement.attr("preload", "metadata");

      var mediaElementSrc = $(document.createElement("source"));
      mediaElement.append(mediaElementSrc);

      mediaElementSrc.attr("type", this.getState().mimeType);
      mediaElementSrc.attr("src", this.getState().resourceURL);

      var loadedCallback = function(e) {
        connector.wasLoaded();
      };

      var options = {};
      options.alwaysShowControls = false;
      options.success = function(media, domObject, internalPlayer) {
        globalPlayer = $(media);
        globalPlayer.on('loadedmetadata', loadedCallback);
      };

      mediaElement.mediaelementplayer(options);

    }
  };

  this.play = function(start) {
    if (globalPlayer) {
      globalPlayer[0].pause();
      globalPlayer[0].setCurrentTime(start);
      globalPlayer[0].play();
    }
  };

  this.playRange = function(start, end) {
    if (globalPlayer) {
      globalPlayer[0].pause();
      globalPlayer[0].setCurrentTime(start);

      var timeHandler = function()
      {
        if (end !== null && globalPlayer[0].currentTime >= end)
        {
          globalPlayer[0].pause();
        }
      };
      globalPlayer.on("timeupdate", timeHandler);
      globalPlayer.on("pause", function()
      {
        globalPlayer.off();
      });

      globalPlayer[0].play();
    }
  };

  this.pause = function() {
    if (globalPlayer !== null) {
      globalPlayer[0].play();
    }
  };
};
