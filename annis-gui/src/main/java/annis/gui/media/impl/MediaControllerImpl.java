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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * Default {@link MediaController} implementation
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class MediaControllerImpl implements MediaController
{

  /**
   * Map of all mediaplayers ordered by their result id.
   */
  private Map<MediaPlayerID, List<MediaPlayer>> mediaPlayers;
  /**
   * Player that was last used by the user orderd by the result id.
   */
  private Map<MediaPlayerID, MediaPlayer> lastUsedPlayer;
  
  private Map<MediaPlayer, VisualizationToggle> visToggle;

  public MediaControllerImpl()
  {
    mediaPlayers = new TreeMap<MediaPlayerID, List<MediaPlayer>>();
    lastUsedPlayer = new TreeMap<MediaPlayerID, MediaPlayer>();
    visToggle = new HashMap<MediaPlayer, VisualizationToggle>();
  }

  private MediaPlayer getPlayerForResult(String sessionID, String resultID)
  {
    MediaPlayerID id = new MediaPlayerID(sessionID, resultID);
    
    List<MediaPlayer> allPlayers = mediaPlayers.get(id);
    if (allPlayers != null && allPlayers.size() > 0)
    {
      MediaPlayer lastPlayer = lastUsedPlayer.get(id);
      MediaPlayer player = null;
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
  public void play(String sessionID,String resultID, double startTime)
  {
    MediaPlayer player = getPlayerForResult(sessionID, resultID);
    
    if (player != null)
    {
      VisualizationToggle t = visToggle.get(player);
      if(t != null)
      {
        t.toggleVisualizer(false);
      }
    
      player.play(startTime);
    }
  }

  @Override
  public void play(String sessionID, String resultID, double startTime, double endTime)
  {
    MediaPlayer player = getPlayerForResult(sessionID, resultID);
    
    if (player != null)
    {
      VisualizationToggle t = visToggle.get(player);
      if(t != null)
      {
        t.toggleVisualizer(false);
      }
      player.play(startTime, endTime);
    }
  }

  @Override
  public void pauseAll(String sessionID)
  {
    for (List<MediaPlayer> playerList : mediaPlayers.values())
    {
      for (MediaPlayer player : playerList)
      {
        player.pause();
      }
    }
  }

  @Override
  public void addMediaPlayer(MediaPlayer player, String sessionID, String resultID, 
    VisualizationToggle toggle)
  {
    // some sanity checks
    if (resultID == null || sessionID == null)
    {
      return;
    }
    
    MediaPlayerID id = new MediaPlayerID(sessionID, resultID);

    // add new list if no player with this number yet
    if (mediaPlayers.get(id) == null)
    {
      mediaPlayers.put(id, new LinkedList<MediaPlayer>());
    }

    // actually adding (we do not check if the player is already in the list)
    List<MediaPlayer> playerList = mediaPlayers.get(id);
    playerList.add(player);
    
    visToggle.put(player, toggle);
    
  }

  @Override
  public void clearMediaPlayers(String sessionID)
  {
    mediaPlayers.clear();
  }
  
}
