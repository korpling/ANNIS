/*
 * Copyright 2013 SFB 632.
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
package annis.visualizers.iframe;

import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * A {@link CorefVisualizer} that is only using the match (not the document).
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class MatchCorefVisualizer extends CorefVisualizer
{

  @Override
  public String getShortName()
  {
    return "coref";
  }

  @Override
  public boolean isUsingText()
  {
    return false;
  }
  
  
}
