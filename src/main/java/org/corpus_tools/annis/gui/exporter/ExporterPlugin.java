/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.exporter;

import com.google.common.eventbus.EventBus;
import com.vaadin.ui.UI;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.QueryLanguage;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public interface ExporterPlugin {
  public Exception convertText(String query, QueryLanguage queryLanguage, int contextLeft,
      int contextRight, Set<String> corpora, List<String> keys, String args, boolean alignmc,
      Writer out, EventBus eventBus,
      Map<String, CorpusConfiguration> corpusConfigs, UI ui);

  public String getFileEnding();

  public String getHelpMessage();

  public boolean isAlignable();

  public boolean isCancelable();

  public boolean needsContext();

}
