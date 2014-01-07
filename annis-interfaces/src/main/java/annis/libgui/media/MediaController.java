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
import net.xeoh.plugins.base.Plugin;

/**
 * System wide access to multimedia player functions.
 * 
 * The real playback is done by implementations of {@link MediaPlayer} which
 * are registered to this controller.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface MediaController extends Plugin
{
  /**
   * Play media file from a specific starting point.
   * 
   * @param resultID The ID of the result where the media player will be started
   * @param startTime Where to start the playback in seconds.
   */
  public void play(String resultID, double startTime);
  
  /**
   * Play media file from a specific starting point to and endpoint.
   * @param resultID The ID of the result where the media player will be started
   * @param startTime Where to start the playback in seconds.
   * @param endTime Where to end the playback in seconds.
   */
  public void play(String resultID, double startTime, double endTime);
  
  /**
   * Register a {@link MediaPlayer} instance. 
   * @param player The instance.
   * @param resultID To which result this player belongs.
   */
  public void addMediaPlayer(MediaPlayer player, String resultID, 
    VisualizationToggle toggle);
  
  /**
   * Unregister all associated {@link MediaPlayer} instances.
   */
  public void clearMediaPlayers();

  void closeOtherPlayers(MediaPlayer doNotCloseThisOne);
}
