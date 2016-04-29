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
package annis.libgui.media;

import annis.libgui.VisualizationToggle;
import annis.libgui.media.MediaController;
import annis.libgui.media.MediaPlayer;
import annis.visualizers.LoadableVisualizer;
import com.vaadin.ui.Notification;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Default {@link MediaController} implementation
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class MediaControllerImpl implements MediaController, Serializable
{

  /**
   * Map of all mediaplayers ordered by their result id.
   */
  private Map<String, List<MediaPlayer>> mediaPlayers;
  /**
   * Player that was last used by the user orderd by the result id.
   */
  private Map<String, MediaPlayer> lastUsedPlayer;
  private Map<MediaPlayer, VisualizationToggle> visToggle;

  /** Since everone can call us asynchronously we need a locking mechanism */
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  
  public MediaControllerImpl()
  {
    lock.writeLock().lock();
    try
    {
      mediaPlayers = new TreeMap<String, List<MediaPlayer>>();
      lastUsedPlayer = new TreeMap<String, MediaPlayer>();
      visToggle = new HashMap<MediaPlayer, VisualizationToggle>();
    }
    finally
    {
      lock.writeLock().unlock();
    }
  }

  private MediaPlayer getPlayerForResult(String resultID)
  {
    List<MediaPlayer> allPlayers = mediaPlayers.get(resultID);
    if (allPlayers != null && allPlayers.size() > 0)
    {
      MediaPlayer lastPlayer = lastUsedPlayer.get(resultID);
      MediaPlayer player;
      if (lastPlayer == null)
      {
        player = allPlayers.get(0);
      }
      else
      {
        player = lastPlayer;
      }
      return player;
    }

    return null;
  }

  @Override
  public void play(String resultID, double startTime)
  {
    boolean foundPlayer = false;
    
    lock.readLock().lock();
    try
    {
      MediaPlayer player = getPlayerForResult( resultID);

      if (player != null)
      {
        closeOtherPlayers(player);

        VisualizationToggle t = visToggle.get(player);
        if (t != null)
        {
          foundPlayer = true;
          t.toggleVisualizer(true, new CallbackImpl(player, startTime, null));
        }

      }
    }
    finally
    {
      lock.readLock().unlock();
    }
    if(!foundPlayer)
    {
      Notification.show("Could not play media.", "If this is a match reference open the actual search interface by following the \"Show in ANNIS search interface\" link.", Notification.Type.WARNING_MESSAGE);
    }
  }

  @Override
  public void play(String resultID, double startTime, double endTime)
  {

    boolean foundPlayer = false;
    lock.readLock().lock();
    try
    {
      MediaPlayer player = getPlayerForResult(resultID);
      
      if (player != null)
      {
        closeOtherPlayers(player);
        
        VisualizationToggle t = visToggle.get(player);
        if (t != null)
        {
          foundPlayer = true;
          t.toggleVisualizer(true, new CallbackImpl(player, startTime, endTime));
        }
      }
    }
    finally
    {
      lock.readLock().unlock();
    }
    if(!foundPlayer)
    {
      Notification.show("Could not play media.", "If this is a match reference open the actual search interface by following the \"Show in ANNIS search interface\" link.", Notification.Type.WARNING_MESSAGE);
    }
  }

  @Override
  public void closeOtherPlayers(MediaPlayer doNotCloseThisOne)
  {
    for (List<MediaPlayer> playersForID : mediaPlayers.values())
    {
      for (MediaPlayer player : playersForID)
      {
        if (player != doNotCloseThisOne)
        {
          VisualizationToggle t = visToggle.get(player);
          if (t != null)
          {
            t.toggleVisualizer(false, null);
          }
        }
      }
    }
  }
  

  @Override
  public void addMediaPlayer(MediaPlayer player, String resultID,
    VisualizationToggle toggle)
  {
    // some sanity checks
    if (resultID == null)
    {
      return;
    }
    
    lock.writeLock().lock();
    try
    {
      // add new list if no player with this number yet
      if (mediaPlayers.get(resultID) == null)
      {
        mediaPlayers.put(resultID, new LinkedList<MediaPlayer>());
      }

      // actually adding (we do not check if the player is already in the list)
      List<MediaPlayer> playerList = mediaPlayers.get(resultID);
      playerList.add(player);

      visToggle.put(player, toggle);
    }
    finally
    {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void clearMediaPlayers()
  {
    lock.writeLock().lock();
    try
    {
      mediaPlayers.clear();
      visToggle.clear();
      lastUsedPlayer.clear();
    }
    finally
    {
      lock.writeLock().unlock();
    }
  }
  
  public static class CallbackImpl implements LoadableVisualizer.Callback
  {
    
    private MediaPlayer player;
    private Double startTime;
    private Double endTime;

    public CallbackImpl(MediaPlayer player, Double startTime, Double endTime)
    {
      this.player = player;
      this.startTime = startTime;
      this.endTime = endTime;
    }

    @Override
    public void visualizerLoaded(LoadableVisualizer origin)
    {
      if(player != null && startTime != null)
      {
        if(endTime == null)
        {
          player.play(startTime);
        }
        else
        {
          player.play(startTime, endTime);
        }
      }
    }
    
  }
}
