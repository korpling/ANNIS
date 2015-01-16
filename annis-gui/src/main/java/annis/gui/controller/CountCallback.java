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
import annis.gui.components.ExceptionDialog;
import annis.gui.objects.QueryUIState;
import annis.gui.resultview.ResultViewPanel;
import annis.service.objects.MatchAndDocumentCount;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.ui.Notification;
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

  private final SearchUI ui;

  public CountCallback(ResultViewPanel panel, int pageSize, SearchUI ui)
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
              ui.getControlPanel().getQueryPanel().
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
            if (causeFinal.getResponse().getStatus() == 400)
            {
              String errMsg = causeFinal.getResponse().getEntity(String.class);
              Notification.show("parsing error", errMsg,
                Notification.Type.WARNING_MESSAGE);
              ui.getControlPanel().getQueryPanel().setStatus(errMsg);
            }
            else if (causeFinal.getResponse().getStatus() == 504)
            {
              String errMsg = "Timeout: query execution took too long.";
              Notification.show(errMsg,
                "Try to simplyfiy your query e.g. by replacing \"node\" with an annotation name or adding more constraints between the nodes.",
                Notification.Type.WARNING_MESSAGE);
              ui.getControlPanel().getQueryPanel().setStatus(errMsg);
            }
            else if (causeFinal.getResponse().getStatus() == 403)
            {
              String errMsg = "You don't have the access rights to query this corpus. " + "You might want to login to access more corpora.";
              Notification.show(errMsg,
                Notification.Type.WARNING_MESSAGE);
              ui.getControlPanel().getQueryPanel().setStatus(errMsg);
            }
            else
            {
              log.error("Unexpected exception:  " + causeFinal.getLocalizedMessage(),
                causeFinal);
              ExceptionDialog.show(causeFinal);
              ui.getControlPanel().getQueryPanel().
                setStatus("Unexpected exception:  " + causeFinal.getMessage());
            }
          } // end if cause != null
          ui.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);
        }
      });
    }
  }
  
}
