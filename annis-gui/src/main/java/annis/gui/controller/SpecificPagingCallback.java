/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.controller;

import annis.gui.AnnisUI;
import annis.gui.SearchView;
import annis.gui.objects.DisplayedResultQuery;
import annis.gui.objects.QueryUIState;
import annis.gui.paging.PagingCallback;
import annis.gui.resultfetch.ResultFetchJob;
import annis.gui.resultview.ResultViewPanel;
import annis.libgui.Background;
import java.util.concurrent.Future;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SpecificPagingCallback implements PagingCallback
{
  private final ResultViewPanel panel;

  private final SearchView searchView;
  private final AnnisUI ui;
  private final DisplayedResultQuery query;
  
  public SpecificPagingCallback(AnnisUI ui, SearchView searchView,
    ResultViewPanel panel, DisplayedResultQuery initialQuery)
  {
    this.panel = panel;
    this.ui = ui;
    this.searchView = searchView;
    this.query = initialQuery;
  }

  @Override
  public void switchPage(long offset, int limit)
  {
    query.setOffset(offset);
    query.setLimit(limit);
    ui.getQueryController().setQuery(query);
    // execute the result query again
    updateMatches(ui.getQueryController().getSearchQuery(), panel);

  }

  private void updateMatches(DisplayedResultQuery newQuery, ResultViewPanel panel)
  {
    if (panel != null)
    {
      searchView.updateFragment(newQuery);
      searchView.getControlPanel().getQueryPanel().getPiCount().setVisible(true);
      searchView.getControlPanel().getQueryPanel().getPiCount().setEnabled(true);
      Future<?> future = Background.run(new ResultFetchJob(newQuery, panel, ui));
      ui.getQueryState().getExecutedTasks().
        put(QueryUIState.QueryType.FIND, future);
    }
  }
  
}
