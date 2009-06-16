/*
 * Play.fx
 *
 * Created on 15.06.2009, 22:05:18
 */

package de.hu_berlin.german.korpling.annis;

import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * @author thomas
 */

public class PlayerUI extends CustomNode{
    public override function create(): Node {
      return Group {
        content: [
          Rectangle {
            x: 0, y: 0
            width: 140, height: 16,
            fill: Color{red: 0.13, green: 0.13, blue: 0.13}
          }
          ]
      }
    }
}
