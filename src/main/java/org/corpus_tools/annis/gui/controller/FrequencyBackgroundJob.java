/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.tuple.Pair;
import org.corpus_tools.annis.api.model.FrequencyQuery;
import org.corpus_tools.annis.api.model.FrequencyQueryDefinitionInner;
import org.corpus_tools.annis.api.model.FrequencyTableRow;
import org.corpus_tools.annis.gui.CommonUI;
import org.corpus_tools.annis.gui.frequency.FrequencyQueryPanel;
import org.corpus_tools.annis.gui.objects.FrequencyTableEntry;
import org.corpus_tools.salt.util.SaltUtil;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class FrequencyBackgroundJob implements Callable<List<FrequencyTableRow>> {

  private final CommonUI ui;
  private final QueryController queryController;
  private final org.corpus_tools.annis.gui.objects.FrequencyQuery query;

  private final FrequencyQueryPanel panel;

  public FrequencyBackgroundJob(CommonUI ui, QueryController queryController,
      org.corpus_tools.annis.gui.objects.FrequencyQuery query,
      FrequencyQueryPanel panel) {
    this.ui = ui;
    this.queryController = queryController;
    this.query = query;
    this.panel = panel;
  }

  @Override
  public List<FrequencyTableRow> call() throws Exception {
    final List<FrequencyTableRow> t = loadBeans();
    ui.access(() -> panel.showResult(t, query));
    return t;
  }

  private List<FrequencyTableRow> loadBeans() {
    List<FrequencyTableRow> result = new ArrayList<>();

    try {
      FrequencyQuery frequencyQuery = new FrequencyQuery();
      frequencyQuery.setQuery(query.getQuery());
      frequencyQuery.setCorpora(new LinkedList<>(query.getCorpora()));
      frequencyQuery.setQueryLanguage(query.getApiQueryLanguage());
      List<FrequencyQueryDefinitionInner> freqDef = new LinkedList<FrequencyQueryDefinitionInner>();
      for (FrequencyTableEntry e : query.getFrequencyDefinition()) {
        FrequencyQueryDefinitionInner d = new FrequencyQueryDefinitionInner();
        d.setNodeRef(e.getReferencedNode());
        switch (e.getType()) {
          case span:
            d.setNs("annis");
            d.setName("tok");
            break;
          case annotation:
            Pair<String, String> annoKey = SaltUtil.splitQName(e.getKey());
            if (annoKey.getLeft() != null) {
              d.setNs(annoKey.getLeft());
            }
            d.setNs(annoKey.getLeft());
            d.setName(annoKey.getRight());
            break;
        }
        
        freqDef.add(d);
      }
      frequencyQuery.setDefinition(freqDef);
      result = ui.getWebClient().post().uri("/search/frequency").bodyValue(frequencyQuery)
          .retrieve().bodyToFlux(FrequencyTableRow.class).collectList().block();
    } catch (final WebClientResponseException ex) {
      ui.access(() -> queryController.reportServiceException(ex, true));
    }
    return result;
  }

}
