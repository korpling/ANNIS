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

import java.io.Serializable;

/**
 * Classes that implement this interface are representing media players in 
 * the GUI.
 * Only functions relevant for ANNIS are implemented, this is not a general media
 * player interface.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface MediaPlayer extends Serializable
{
  /**
   * Play media file from a specific starting point.
   * @param start Where to start the playback in seconds.
   */
  public void play(double start);
  
  /**
   * Play media file from a specific starting point to and endpoint.
   * @param start Where to start the playback in seconds.
   * @param end Where to end the playback in seconds.
   */
  public void play(double start, double end);
  
  /**
   * Pause playback of the media file.
   */
  public void pause();
  
}
