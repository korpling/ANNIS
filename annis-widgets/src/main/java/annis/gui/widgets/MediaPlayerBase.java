/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.widgets;

import annis.libgui.media.MediaPlayer;
import annis.libgui.media.MimeTypeErrorListener;
import annis.gui.widgets.gwt.client.ui.VMediaPlayerBase;
import annis.visualizers.LoadableVisualizer;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public abstract class MediaPlayerBase extends AbstractComponent 
  implements MediaPlayer, LoadableVisualizer, LegacyComponent
{

  public enum PlayerAction
  {
    idle, play, pause, stop
  }
  private PlayerAction action;
  private Double startTime;
  private Double endTime;
  private boolean sourcesAdded;
  private String resourceURL;
  private String mimeType;
  private boolean wasLoaded;
  
  private Set<Callback> callbacks;
  
  public MediaPlayerBase(String resourceURL, String mimeType)
  {
    this.resourceURL = resourceURL;
    this.mimeType = mimeType;
    this.callbacks = new HashSet<Callback>();
    this.wasLoaded = false;
  }
  
  
  
  @Override
  public void play(double start)
  {
    action = PlayerAction.play;
    startTime = start;
    endTime = null;

    requestRepaint();
  }

  @Override
  public void play(double start, double end)
  {
    action = PlayerAction.play;
    startTime = start;
    endTime = end;

    requestRepaint();
  }

  @Override
  public void pause()
  {
    action = PlayerAction.pause;
    requestRepaint();
  }

  public void stop()
  {
    action = PlayerAction.stop;
    
    requestRepaint();
  }
  
  

  @Override
  public void changeVariables(Object source, Map<String, Object> variables)
  {    
    if((Boolean) variables.get(VMediaPlayerBase.CANNOT_PLAY) == Boolean.TRUE)
    {     
      
      if(getUI() instanceof MimeTypeErrorListener)
      {
        ((MimeTypeErrorListener) getUI()).notifyCannotPlayMimeType(mimeType);
      }
    }
    if((Boolean) variables.get(VMediaPlayerBase.MIGHT_NOT_PLAY) == Boolean.TRUE)
    {     
      
      if(getUI() instanceof MimeTypeErrorListener)
      {
        ((MimeTypeErrorListener) getUI()).notifyMightNotPlayMimeType(mimeType);
      }
    }
    
    if((Boolean) variables.get(VMediaPlayerBase.PLAYER_LOADED) == Boolean.TRUE)
    {
      wasLoaded = true;
      for(Callback c : callbacks)
      {
        c.visualizerLoaded(this);
      }
    }
  }

  @Override
  public void detach()
  {
    super.detach();
    
    wasLoaded = false;
    sourcesAdded = false;
    
    startTime = null;
    endTime = null;
  }
  
  

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    if(target.isFullRepaint())
    {
      sourcesAdded = false;
      wasLoaded = false;
    }
    
    boolean sourcesNeeded = true;
    
    if(action == PlayerAction.play)
    {
      String[] args;
      if(endTime == null)
      {
        args = new String[] {"" + startTime};
      }
      else
      {
        args = new String[] {"" + startTime, "" + endTime};
      }
      target.addAttribute(VMediaPlayerBase.PLAY, args);
      action = PlayerAction.idle;
    }
    else if(action == PlayerAction.pause)
    {
      target.addAttribute(VMediaPlayerBase.PAUSE, true);
      action = PlayerAction.idle;
    }
    else if(action == PlayerAction.stop)
    {
      target.addAttribute(VMediaPlayerBase.STOP, true);
      action = PlayerAction.idle;   
      // re-add source the next time someone wants to play something
      sourcesAdded = false;
      sourcesNeeded = false;
    }
    
    if(sourcesNeeded && !sourcesAdded)
    {
      target.addAttribute(VMediaPlayerBase.SOURCE_URL, resourceURL);
      target.addAttribute(VMediaPlayerBase.MIME_TYPE, mimeType);
      sourcesAdded = true;
    }
    
    
  }

  @Override
  public void addOnLoadCallBack(Callback callback)
  {
    this.callbacks.add(callback);
  }

  @Override
  public void clearCallbacks()
  {
    this.callbacks.clear();
  }

  @Override
  public boolean isLoaded()
  {
    return wasLoaded;
  }

  
}
