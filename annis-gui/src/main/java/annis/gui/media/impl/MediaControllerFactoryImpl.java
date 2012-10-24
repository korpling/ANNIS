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

import annis.gui.media.MediaController;
import annis.gui.media.MediaControllerFactory;
import annis.gui.media.MediaControllerHolder;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * Default {@link MediaControllerFactory} implementation
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class MediaControllerFactoryImpl implements MediaControllerFactory
{

  @Override
  public MediaController getOrCreate(MediaControllerHolder holder)
  {
    if(holder == null)
    {
      // create a complete new instance
      return new MediaControllerImpl();
    }
    else if(holder.getMediaController() == null)
    {
      holder.setMediaController(new MediaControllerImpl());
    }
    
    return holder.getMediaController();
  }
  
}
