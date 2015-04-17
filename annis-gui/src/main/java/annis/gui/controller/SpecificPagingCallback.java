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

import annis.gui.SearchUI;
import annis.gui.objects.PagedResultQuery;
import annis.gui.objects.QueryUIState;
import annis.gui.paging.PagingCallback;
import annis.gui.resultfetch.ResultFetchJob;
import annis.gui.resultview.ResultViewPanel;
import annis.libgui.PollControl;
import java.util.concurrent.Future;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SpecificPagingCallback implements PagingCallback
{
  private final PagedResultQuery query;

  private final ResultViewPanel panel;

  private final SearchUI ui;

  public SpecificPagingCallback(PagedResultQuery query, SearchUI ui,
    ResultViewPanel panel)
  {
    this.query = query.clone();
    this.panel = panel;
    this.ui = ui;
  }

  @Override
  public void switchPage(int offset, int limit)
  {
    if (query != null)
    {
      query.setOffset(offset);
      query.setLimit(limit);
      // execute the result query again
      updateMatches(query, panel);
    }
  }

  private void updateMatches(PagedResultQuery newQuery, ResultViewPanel panel)
  {
    if (panel != null)
    {
      ui.updateFragment(newQuery);
      ui.getControlPanel().getQueryPanel().getPiCount().setVisible(true);
      ui.getControlPanel().getQueryPanel().getPiCount().setEnabled(true);
      Future<?> future = PollControl.runInBackground(500, ui,
        new ResultFetchJob(newQuery, panel, ui));
      ui.getQueryState().getExecutedTasks().
        put(QueryUIState.QueryType.FIND, future);
    }
  }
  
}
