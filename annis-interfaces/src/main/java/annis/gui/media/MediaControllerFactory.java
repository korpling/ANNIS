/*
 * Copyright 2012 SFB 632.
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
package annis.gui.media;

import net.xeoh.plugins.base.Plugin;

/**
 * Factory for {@link MediaController} instances.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public interface MediaControllerFactory extends Plugin
{
  /**
   * Gets a {@link MediaController}. Will always return a non-null value.
   * If the {@link MediaControllerHolder} does not contain an instance, a new 
   * instance will be created and added to the {@link MediaControllerHolder}.
   * 
   * @param holder The {@link MediaControllerHolder} to store the {@link MediaController} 
   * @return Either the instance holded by the {@link MediaControllerHolder} or a newly created one.
   */
  public MediaController getOrCreate(MediaControllerHolder holder);
}
