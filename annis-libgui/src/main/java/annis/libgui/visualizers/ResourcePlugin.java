/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.libgui.visualizers;

import net.xeoh.plugins.base.Plugin;

/**
 * Base interface for all plugins that have resources in their package
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface ResourcePlugin extends Plugin
{
  /**
   * Get the shorted name of the linguistic type of this plugin ("partitur", 
   * "tree", etc.)
   * @return 
   */
  public String getShortName();

}
