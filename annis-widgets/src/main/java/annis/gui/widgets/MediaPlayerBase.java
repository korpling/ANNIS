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

import annis.gui.media.MediaPlayer;
import annis.gui.media.MimeTypeErrorListener;
import annis.gui.widgets.gwt.client.VMediaPlayerBase;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import java.util.Map;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public abstract class MediaPlayerBase extends AbstractComponent implements MediaPlayer
{

  public enum PlayerAction
  {
    idle, play, pause
  }
  private PlayerAction action;
  private Double startTime;
  private Double endTime;
  private boolean sourcesAdded;
  private String resourceURL;
  private String mimeType;
  
  public MediaPlayerBase(String resourceURL, String mimeType)
  {
    this.resourceURL = resourceURL;
    this.mimeType = mimeType;
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

  @Override
  public void changeVariables(Object source, Map<String, Object> variables)
  {
    super.changeVariables(source, variables);
    
    if(variables.containsKey("cannot_play") && (Boolean) variables.get("cannot_play") == true)
    {     
      
      if(getWindow() instanceof MimeTypeErrorListener)
      {
        ((MimeTypeErrorListener) getWindow()).notifyCannotPlayMimeType(mimeType);
      }
    }
  }
  
  

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    super.paintContent(target);
    
    if(!sourcesAdded)
    {
      target.addAttribute("url", resourceURL);
      target.addAttribute("mime_type", mimeType);
      sourcesAdded = true;
    }
    
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
  }
  
  
}
