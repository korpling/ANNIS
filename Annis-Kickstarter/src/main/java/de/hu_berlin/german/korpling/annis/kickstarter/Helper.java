/*
 *  Copyright 2009 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package de.hu_berlin.german.korpling.annis.kickstarter;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;

/**
 *
 * @author thomas
 */
public class Helper
{

  public static void centerWindow(Window window)
  {
    Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
    window.setLocation(new Point((dScreen.width/2)-(window.getBounds().width/2),
      (dScreen.height/2) - (window.getBounds().height/2)));
  }
}
