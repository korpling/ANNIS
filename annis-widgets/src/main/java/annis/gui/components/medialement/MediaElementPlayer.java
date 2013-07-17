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
package annis.gui.components.medialement;

import annis.libgui.media.MediaPlayer;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.ClassResource;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * An video/audio player based on the medialement.js library ({@link http://mediaelementjs.com/})
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@JavaScript({"vaadin://mediaelement/jquery.js", "vaadin://mediaelement/mediaelement-and-player.min.js", "mediaelement_connector.js"})
@StyleSheet({"vaadin://mediaelement/mediaelementplayer.min.css"})
public class MediaElementPlayer extends AbstractJavaScriptComponent 
  implements MediaPlayer
{
  
  public MediaElementPlayer(MediaElement elementType, String resourceURL, String mimeType)
  { 
    getState().setElementType(elementType);
    getState().setResourceURL(resourceURL);
    getState().setMimeType(mimeType);
  }
  
  @Override
  public final MediaState getState() 
  {
    return (MediaState) super.getState();
  }
  
  @Override
  public void play(double start)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void play(double start, double end)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void pause()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void stop()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
