/*
 * PlayerStage.fx
 *
 * Created on 15.06.2009, 21:32:07
 */

package de.hu_berlin.german.korpling.annis;


import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.media.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import de.hu_berlin.german.korpling.annis.AnnisPlayerUI;

var myMedia = Media {
  source:"http://upload.wikimedia.org/wikipedia/commons/7/7a/One_sparrow_then_another.ogg"
}

var player = MediaPlayer {
  media: myMedia
  autoPlay:false
}

var playerUI = AnnisPlayerUI{}

Stage {
    title: "Annis Media Player"
    width: 320
    height: 290
    scene: Scene {
        content: [
          VBox{
            content: [
              MediaView{
                mediaPlayer:player
                visible:true
                preserveRatio:true
                fitWidth:320
                fitHeight:240
              },
              HBox{
                content: [
                  playerUI,
                  Button {
                    text: "Play"
                    action: function() {
                      player.play();
                    }
                  },
                  Button {
                    text: "Pause"
                    action: function() {
                      player.pause();
                    }
                  },
                  Button {
                    text: "Stop"
                    action: function()
                    {
                      player.stop();
                    }

                  }
                ]
              }

            ]
          }
        ]
    }
}