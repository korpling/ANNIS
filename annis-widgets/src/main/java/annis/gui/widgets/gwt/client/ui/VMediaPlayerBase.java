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
package annis.gui.widgets.gwt.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.VConsole;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class VMediaPlayerBase extends Widget implements Paintable
{
  public static final String PLAY = "play";
  public static final String PAUSE = "pause";
  public static final String STOP = "stop";
  public static final String SOURCE_URL = "url";
  public static final String MIME_TYPE = "mime_type";
  public static final String CANNOT_PLAY = "cannot_play";
  public static final String MIGHT_NOT_PLAY = "might_not_play";
  public static final String PLAYER_LOADED = "player_loaded";
  
  private MediaElement media;
  
  /** The client side widget identifier */
  protected String paintableId;
  /** Reference to the server connection object. */
  ApplicationConnection gClient;
  
  public VMediaPlayerBase(MediaElement media)
  {
    this.media = media;
    setElement(this.media);
    
    media.setControls(true);
    media.setAutoplay(false);
    media.setPreload(MediaElement.PRELOAD_METADATA);
    media.setLoop(false);
   
  }

  @Override
  protected void onUnload()
  {
    // stop playing and remove source so that the browser is really not holding
    // any HTTP request open (hopefully)
    media.pause();
    media.setSrc("");
  }
  
  
  
  @Override
  public void updateFromUIDL(UIDL uidl, ApplicationConnection client)
  {
    if (client.updateComponent(this, uidl, true))
    {
      return;
    }
    
    // Save reference to server connection object to be able to send
    // user interaction later
    this.gClient = client;

    // Save the client side identifier (paintable id) for the widget
    paintableId = uidl.getId();
    
    if(media == null)
    {
      VConsole.error("media not set!!!");
      return;
    }
    
    if(uidl.hasAttribute(SOURCE_URL))
    {
      registerMetadataLoadedEvent(media);
      
      if(uidl.hasAttribute(MIME_TYPE))
      {
        String mimeType = uidl.getStringAttribute(MIME_TYPE);
        VConsole.log("canPlayType for \"" + mimeType + "\"value is \"" + media.canPlayType(mimeType) + "\"");
        // check for correct mime type
        if(media.canPlayType(uidl.getStringAttribute(MIME_TYPE)).equals(MediaElement.CANNOT_PLAY))
        {
          VConsole.log("CANNOT PLAY!!!");
          
          gClient.updateVariable(paintableId, CANNOT_PLAY, true, true); 
        }
        else if(media.canPlayType(uidl.getStringAttribute(MIME_TYPE)).equals(MediaElement.CAN_PLAY_MAYBE))
        {          
          gClient.updateVariable(paintableId, MIGHT_NOT_PLAY, true, true); 
        }
      }
      media.setSrc(uidl.getStringAttribute(SOURCE_URL));
    }
    
    
    
    if(uidl.hasAttribute(PLAY))
    {
      String[] time = uidl.getStringArrayAttribute(PLAY);
      if(time != null && time.length > 0)
      {
        if(time.length == 1)
        {
          try
          {
            media.setCurrentTime(Double.parseDouble(time[0]));
          }
          catch(NumberFormatException ex)
          {
            VConsole.error(ex);
          }
        }
        else if(time.length == 2)
        {
          try
          {
            media.setCurrentTime(Double.parseDouble(time[0]));
            setEndTimeOnce(media, Double.parseDouble(time[1]));
          }
          catch(NumberFormatException ex)
          {
            VConsole.error(ex);
          }
        }
        media.play();
      }
    }
    else if(uidl.hasAttribute(PAUSE))
    {
      media.pause();
    }
    else if(uidl.hasAttribute(STOP))
    {
      media.pause();
      media.setSrc("");
    }
  }
  
  private void metaDataWasLoaded()
  {
    if (gClient != null && paintableId != null)
    {
      gClient.updateVariable(paintableId, PLAYER_LOADED, true, true);
    }
  }

  
  
  private native void setEndTimeOnce(Element elem, double endTime) 
  /*-{
    var media =  $wnd.$(elem); // wrap element with jquery
    var timeHandler = function()
    {
      if (endTime !== null && media[0].currentTime >= endTime)
      {       
        media[0].pause();  
      }    
    };
    media.on("timeupdate", timeHandler);
    media.on("pause", function()
    {
      media.off();
    }); 
  }-*/;
  
  private native void registerMetadataLoadedEvent(Element el)
  /*-{
      var media = $wnd.$(el);
      var self = this;
      
      media.on('loadedmetadata', $entry(function(e) 
      {
        self.@annis.gui.widgets.gwt.client.ui.VMediaPlayerBase::metaDataWasLoaded()();
      }));
  }-*/;

  public MediaElement getMedia()
  {
    return media;
  }
  
  
  
}
