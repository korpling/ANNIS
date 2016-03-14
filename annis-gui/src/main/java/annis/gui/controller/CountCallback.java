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
import annis.gui.objects.QueryUIState;
import annis.gui.resultview.ResultViewPanel;
import annis.service.objects.MatchAndDocumentCount;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CountCallback implements Runnable
{
  private static final Logger log = LoggerFactory.getLogger(
    CountCallback.class);
  
  private final ResultViewPanel panel;

  private final int pageSize;

  private final AnnisUI ui;

  public CountCallback(ResultViewPanel panel, int pageSize, AnnisUI ui)
  {
    this.panel = panel;
    this.pageSize = pageSize;
    this.ui = ui;
  }

  @Override
  public void run()
  {
    Future futureCount = ui.getQueryState().getExecutedTasks().
      get(QueryUIState.QueryType.COUNT);
    final MatchAndDocumentCount countResult;
    MatchAndDocumentCount tmpCountResult = null;
    if (futureCount != null)
    {
      UniformInterfaceException cause = null;
      try
      {
        tmpCountResult = (MatchAndDocumentCount) futureCount.get();
      }
      catch (InterruptedException ex)
      {
        log.warn(null, ex);
      }
      catch (ExecutionException root)
      {
        if (root.getCause() instanceof UniformInterfaceException)
        {
          cause = (UniformInterfaceException) root.getCause();
        }
        else
        {
          log.error("Unexcepted ExecutionException cause", root);
        }
      }
      finally
      {
        countResult = tmpCountResult;
      }
      ui.getQueryState().getExecutedTasks().
        remove(QueryUIState.QueryType.COUNT);
      final UniformInterfaceException causeFinal = cause;
      ui.accessSynchronously(new Runnable()
      {
        @Override
        public void run()
        {
          if (causeFinal == null)
          {
            if (countResult != null)
            {
              String documentString = countResult.getDocumentCount() > 1 ? "documents" : "document";
              String matchesString = countResult.getMatchCount() > 1 ? "matches" : "match";
              ui.getSearchView().getControlPanel().getQueryPanel().
                setStatus("" + countResult.getMatchCount() + " " + matchesString + "\nin " + countResult.getDocumentCount() + " " + documentString);
              if (countResult.getMatchCount() > 0 && panel != null)
              {
                panel.getPaging().setPageSize(pageSize, false);
                panel.setCount(countResult.getMatchCount());
              }
            }
          }
          else
          {
            ui.getQueryController().reportServiceException(causeFinal, true);
          } // end if cause != null
          ui.getSearchView().getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);
        }
      });
    }
  }
  
}
