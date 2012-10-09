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
package annis.gui.media.impl;

import annis.gui.VisualizationToggle;
import annis.gui.media.MediaController;
import annis.gui.media.MediaPlayer;
import annis.visualizers.LoadableVisualizer;
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
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class MediaControllerImpl implements MediaController
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
  private ReadWriteLock lock = new ReentrantReadWriteLock();
  
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
  public void play(String resultID, final double startTime)
  {
    pauseAll();

    lock.readLock().lock();
    try
    {
      final MediaPlayer player = getPlayerForResult( resultID);

      if (player != null)
      {
        VisualizationToggle t = visToggle.get(player);
        if (t != null)
        {
          t.toggleVisualizer(false, new LoadableVisualizer.Callback() {

            @Override
            public void visualizerLoaded(LoadableVisualizer origin)
            {
              player.play(startTime);
            }
          });
        }

      }
    }
    finally
    {
      lock.readLock().unlock();
    }
  }

  @Override
  public void play(String resultID, final double startTime, final double endTime)
  {
    pauseAll();

    lock.readLock().lock();
    try
    {
      final MediaPlayer player = getPlayerForResult(resultID);

      if (player != null)
      {
        VisualizationToggle t = visToggle.get(player);
        if (t != null)
        {
          t.toggleVisualizer(false, new LoadableVisualizer.Callback() 
          {
            @Override
            public void visualizerLoaded(LoadableVisualizer origin)
            {
              player.play(startTime, endTime);
            }
          });
        }
      }
    }
    finally
    {
      lock.readLock().unlock();
    }
  }

  @Override
  public void pauseAll()
  {
    lock.readLock().lock();
    try
    {
      for (List<MediaPlayer> playersForID : mediaPlayers.values())
      {
        for(MediaPlayer player : playersForID)
        {
          player.pause();
        }
      }
    }
    finally
    {
      lock.readLock().unlock();
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
}
