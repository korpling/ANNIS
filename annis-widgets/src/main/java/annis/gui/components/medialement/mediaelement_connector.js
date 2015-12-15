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
  var rootDiv = $(this.getElement(this.getConnectorId()));


  function player() {
    return rootDiv.find(connector.getState().elementType);
  }
  
  function initElement() {
      // if the sources are still set, Chrome might not close the connections for
      // streaming and will block in the end
      for (playerIndex in mejs.players) 
      {
        var p = mejs.players[playerIndex];
        if(p.setSrc && p.media.setSrc)
        {
          p.setSrc("");
        }
        // also clean up the global indexes of players, since it can get quite polluted
        // after some clicks
        if(p.remove && p.media.remove) // there is a bug in IE, check if removing works
        {
          p.remove();
        }
        else
        {
          // just delete from the list
          delete mejs.players[playerIndex];
        }
      }
      var mediaElement = $(document.createElement(connector.getState().elementType));
      rootDiv.append(mediaElement);

      mediaElement.attr("controls", "controls");
      mediaElement.attr("preload", "auto");

      var mediaElementSrc = $(document.createElement("source"));
      mediaElement.append(mediaElementSrc);

      if(connector.getState().mimeType)
      {
        // check if the media player can play the mime type
        if(mediaElement[0].canPlayType(connector.getState().mimeType) === "")
        {
          connector.cannotPlay(connector.getState().mimeType);
        }
      }
      mediaElementSrc.attr("type", connector.getState().mimeType);
      mediaElementSrc.attr("src", connector.getState().resourceURL);

      var loadedCallback = function(e) {
        connector.wasLoaded();
        player().off('loadedmetadata');
      };

      var options = {};
      options.alwaysShowControls = false;
      options.pauseOtherPlayers = true;
      options.success = function(media, domObject) {
        player().on('loadedmetadata', loadedCallback);
      };

      mediaElement.mediaelementplayer(options);
  }
  
  // Handle changes from the server-side
  this.onStateChange = function() {
    
    if (player().length === 0)
    {
      initElement();
    }
  };

  this.play = function(start) {
    if (player().length) {
      player()[0].pause();
      player()[0].setCurrentTime(start);
      player()[0].play();
    }
  };

  this.playRange = function(start, end) {
    if (player().length) {
      player()[0].pause();
      player()[0].setCurrentTime(start);

      var timeHandler = function()
      {
        if (end !== null && player()[0].currentTime >= end)
        {
          player()[0].pause();
        }
      };
      player().on("timeupdate", timeHandler);
      player().on("pause", function()
      {
        player().off("timeupdate", timeHandler);
      });

      player()[0].play();
    }
  };

  this.pause = function() {
    if (player().length) {
      player()[0].pause();
    }
  };
};
