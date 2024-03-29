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
package org.corpus_tools.annis.gui.resultview;

import com.vaadin.ui.UI;
import java.io.Serializable;
import java.util.List;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.salt.common.SDocument;

/**
 *
 * @author thomas
 */
public interface ResolverProvider extends Serializable {
  public List<VisualizerRule> getResolverEntries(String corpusName, SDocument result, UI ui);

}
