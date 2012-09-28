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
package annis.gui.visualizers.component;

import annis.gui.media.MediaController;
import annis.gui.visualizers.AbstractVisualizer;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.widgets.AudioPlayer;
import com.vaadin.Application;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class AudioVisualizer extends AbstractVisualizer<AudioPlayer>
{

  @InjectPlugin
  public MediaController mediaController;
  
  @Override
  public String getShortName()
  {
    return "audio";
  }

  @Override
  public AudioPlayer createComponent(VisualizerInput visInput, Application application)
  {
     AudioPlayer player = new AudioPlayer();
     
     if(mediaController != null)
     {
       mediaController.addMediaPlayer(player, visInput.getId());
     }
     
     return player;
  }
  
}
