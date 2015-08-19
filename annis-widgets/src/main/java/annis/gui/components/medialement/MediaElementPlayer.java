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
import annis.libgui.media.MimeTypeErrorListener;
import annis.visualizers.LoadableVisualizer;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.UI;
import elemental.json.JsonArray;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;

/**
 * An video/audio player based on the medialement.js library
 * ({@link http://mediaelementjs.com/})
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@JavaScript(
{
  "vaadin://jquery.js", "vaadin://mediaelement/mediaelement-and-player.js", "mediaelement_connector.js"
})
@StyleSheet(
{
  "vaadin://mediaelement/mediaelementplayer.min.css"
})
public class MediaElementPlayer extends AbstractJavaScriptComponent
  implements MediaPlayer, LoadableVisualizer
{

  private Set<Callback> callbacks;

  private boolean wasLoaded;

  public MediaElementPlayer(MediaElement elementType, String resourceURL,
    String mimeType)
  {
    this.callbacks = new HashSet<>();
    this.wasLoaded = false;

    getState().setElementType(elementType);
    getState().setResourceURL(resourceURL);
    getState().setMimeType(mimeType);

    final MediaElementPlayer finalThis = this;
    addFunction("wasLoaded", new JavaScriptFunction()
    {
      @Override
      public void call(JsonArray arguments) throws JSONException
      {
        wasLoaded = true;
        for (Callback c : callbacks)
        {
          c.visualizerLoaded(finalThis);
        }
      }
    });
    addFunction("cannotPlay", new CannotPlayFunction());

  }
  
  private static class CannotPlayFunction implements JavaScriptFunction
  {

    @Override
    public void call(JsonArray arguments) throws JSONException
    {
      if(UI.getCurrent() instanceof MimeTypeErrorListener)
      {
        ((MimeTypeErrorListener) UI.getCurrent()).notifyCannotPlayMimeType(arguments.getString(0));
      }
    }
    
  }

  @Override
  public final MediaState getState()
  {
    return (MediaState) super.getState();
  }

  @Override
  public void play(double start)
  {
    // we get the time in seconds with fractions but the HTML5 players
    // only have a resolution of seconds
    start = Math.floor(start);
    callFunction("play", start);
  }

  @Override
  public void play(double start, double end)
  {
    // we get the time in seconds with fractions but the HTML5 players
    // only have a resolution of seconds
    start = Math.floor(start);
    end = Math.ceil(end);
    callFunction("playRange", start, end);
  }

  @Override
  public void pause()
  {
    callFunction("pause");
  }

  @Override
  public boolean isLoaded()
  {
    return wasLoaded;
  }

  @Override
  public void clearCallbacks()
  {
    callbacks.clear();
  }

  @Override
  public void addOnLoadCallBack(Callback callback)
  {
    callbacks.add(callback);
  }
}
