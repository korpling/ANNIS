/*
 * Copyright 2014 SFB 632.
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
package annis.libgui.visualizers;

import java.util.List;
import java.util.Properties;

/**
 * A visualizer that defines a filtering for the annotations. This filtering
 * is currently only used when fetching complete documents (if {@link VisualizerPlugin#isUsingText() is true.).
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface FilteringVisualizerPlugin
{
  /**
   * Return the node annotation names or null if no filtering should be applied.
   * @param toplevelCorpusName
   * @param documentName
   * @param mappings
   * @return 
   */
  public List<String> getFilteredNodeAnnotationNames(String toplevelCorpusName, 
    String documentName, Properties mappings);
}
