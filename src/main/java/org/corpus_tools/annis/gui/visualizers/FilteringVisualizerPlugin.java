/*
 * Copyright 2014 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.visualizers;

import com.vaadin.ui.UI;
import java.util.List;
import java.util.Map;
import org.corpus_tools.salt.common.SCorpus;

/**
 * A visualizer that defines a filtering for the annotations. This filtering is currently only used
 * when fetching complete documents (if {@link VisualizerPlugin#isUsingText() is true.).
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public interface FilteringVisualizerPlugin {
  /**
   * Return the node annotation names or null if no filtering should be applied.
   * 
   * @param toplevelCorpusName The name of the toplevel corpus.
   * @param toplevelCorpusId The Salt node ID for the toplevel {@link SCorpus}
   * @param mappings
   * @return
   * 
   * @see SCorpus#getId()
   */
  public List<String> getFilteredNodeAnnotationNames(String toplevelCorpusName,
      String toplevelCorpusId, Map<String, String> mappings, UI ui);
}
