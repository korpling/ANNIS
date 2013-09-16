/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui;

import annis.libgui.Helper;
import annis.gui.beans.HistoryEntry;
import annis.gui.components.ExceptionDialog;
import annis.libgui.media.MediaController;
import annis.gui.model.PagedResultQuery;
import annis.gui.model.Query;
import annis.gui.paging.PagingCallback;
import annis.gui.resultview.ResultViewPanel;
import annis.libgui.visualizers.IFrameResourceMap;
import annis.service.objects.MatchAndDocumentCount;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all the query related actions.
 *
 * <strong>This class is not reentrant.</strong>
 * It is expected that you call the functions from the Vaadin session lock
 * context, either implicitly (e.g. from a component constructor or a handler
 * callback) or explicitly with {@link VaadinSession#lock() }.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class QueryController implements PagingCallback
{

  private static final Logger log = LoggerFactory.getLogger(
    ResultViewPanel.class);

  private SearchUI ui;

  private ResultFetchThread lastMatchThread;
  private ListOrderedSet<HistoryEntry> history;

  private MatchAndDocumentCount lastCount;

  private Future<MatchAndDocumentCount> futureCount;
  
  private UUID lastQueryUUID;
  
  private PagedResultQuery preparedQuery;
  
  private transient BiMap<UUID, PagedResultQuery> queries;
  private transient BiMap<UUID, ResultViewPanel> queryPanels;
  
  public QueryController(SearchUI ui)
  {
    this.ui = ui;
    this.history = new ListOrderedSet<HistoryEntry>();
  }

  public void updateCorpusSetList()
  {
    ui.getControlPanel().getCorpusList().updateCorpusSetList();
  }

  public void setQuery(String query)
  {
    setQuery(new Query(query, ui.getControlPanel().getCorpusList().
      getSelectedCorpora()));
  }

  public void setQuery(Query query)
  {
    // Check if a corpus is selected.
    if (ui.getControlPanel().getCorpusList().getSelectedCorpora().isEmpty()
      && query.getCorpora() != null)
    {
      ui.getControlPanel().getCorpusList().selectCorpora(query.getCorpora());
    }

    PagedResultQuery paged = new PagedResultQuery(
      ui.getControlPanel().getSearchOptions().getLeftContext(),
      ui.getControlPanel().getSearchOptions().getRightContext(),
      0,
      ui.getControlPanel().getSearchOptions().getResultsPerPage(),
      ui.getControlPanel().getSearchOptions().getSegmentationLayer(),
      query.getQuery(), query.getCorpora());
    
    setQuery(paged);
  }

  public void setQuery(PagedResultQuery query)
  {
    // only allow offset at multiples of the limit size
    query.setOffset(query.getOffset() - (query.getOffset() % query.getLimit()));

    preparedQuery = query;


    ui.getControlPanel().getQueryPanel().setQuery(query.getQuery());
    ui.getControlPanel().getSearchOptions().setLeftContext(query.
      getContextLeft());
    ui.getControlPanel().getSearchOptions().setRightContext(query.
      getContextRight());
    ui.getControlPanel().getSearchOptions().setSegmentationLayer(query.
      getSegmentation());

    if (query.getCorpora() != null)
    {
      ui.getControlPanel().getCorpusList().selectCorpora(query.getCorpora());
    }
    
  }

  /**
   * Cancel queries from the client side.
   *
   * Important: This does not magically cancel the query on the server side, so
   * don't use this to implement a "real" query cancelation.
   */
  private void cancelQueries()
  {
    // don't spin forever when canceled
    ui.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);

    // abort last tasks if running
    if (futureCount != null && !futureCount.isDone())
    {
      futureCount.cancel(true);
    }
    if (lastMatchThread != null && lastMatchThread.isAlive())
    {
      lastMatchThread.abort();
    }

    futureCount = null;

  }

  /**
   * Adds a history entry to the history panel.
   *
   * @param e the entry, which is added.
   *
   * @see HistoryPanel
   */
  public void addHistoryEntry(HistoryEntry e)
  {
    // remove it first in order to let it appear on the beginning of the list
    history.remove(e);
    history.add(0, e);
    ui.getControlPanel().getQueryPanel().updateShortHistory(history.asList());
  }
  
  public UUID executeQuery()
  {
    return executeQuery(true, true);
  }
  
  public UUID executeQuery(boolean executeCount, boolean executeResult)
  {

    Validate.notNull(preparedQuery,
      "You have to set a query before you can execute it.");

    cancelQueries();

    // cleanup resources
    VaadinSession session = VaadinSession.getCurrent();
    session.setAttribute(IFrameResourceMap.class, new IFrameResourceMap());
    if (session.getAttribute(MediaController.class) != null)
    {
      session.getAttribute(MediaController.class).clearMediaPlayers();
    }

    ui.updateFragment(preparedQuery);

    HistoryEntry e = new HistoryEntry();
    e.setCorpora(preparedQuery.getCorpora());
    e.setQuery(preparedQuery.getQuery());

    addHistoryEntry(e);

    if (preparedQuery.getCorpora() == null || preparedQuery.getCorpora().isEmpty())
    {
      Notification.show("Please select a corpus",
        Notification.Type.WARNING_MESSAGE);
      return null;
    }
    if ("".equals(preparedQuery.getQuery()))
    {
      Notification.show("Empty query", Notification.Type.WARNING_MESSAGE);
      return null;
    }
    
    UUID oldQueryUUID = lastQueryUUID;
    lastQueryUUID = UUID.randomUUID();
    
    // remember the query object for later re-usage
    if(!getQueries().containsValue(preparedQuery) )
    {
      getQueries().put(lastQueryUUID, preparedQuery);
    }
    
    AsyncWebResource res = Helper.getAnnisAsyncWebResource();

    if (executeResult)
    {
      // remove old result from view
      if (oldQueryUUID != null && getQueryPanels().get(oldQueryUUID) != null)
      {
        ui.getMainTab().removeComponent(queryPanels.get(oldQueryUUID));
      }
      ResultViewPanel newResultView = new ResultViewPanel(this, ui, ui.getInstanceConfig());
      Tab newTab = ui.getMainTab().addTab(newResultView, "Query Result");
      ui.getMainTab().setSelectedTab(newResultView);
      newTab.setClosable(true);


      getQueryPanels().put(lastQueryUUID, newResultView);
      
     
      ResultFetchThread thread = new ResultFetchThread(preparedQuery, newResultView, ui);
      thread.start();

    }

    if (executeCount)
    {
      // start count query
      ui.getControlPanel().getQueryPanel().setCountIndicatorEnabled(true);

      futureCount = res.path("query").path("search").path("count").
        queryParam(
        "q", preparedQuery.getQuery()).queryParam("corpora",
        StringUtils.join(preparedQuery.getCorpora(), ",")).get(
        MatchAndDocumentCount.class);

      new CountCallback().start();
    }
    
    return lastQueryUUID;
  }

  public void corpusSelectionChangedInBackground()
  {
    ui.getControlPanel().getSearchOptions()
      .updateSearchPanelConfigurationInBackground(ui.getControlPanel().
      getCorpusList().
      getSelectedCorpora());

    ui.updateFragementWithSelectedCorpus(getSelectedCorpora());
  }

  public Set<String> getSelectedCorpora()
  {
    return ui.getControlPanel().getCorpusList().getSelectedCorpora();
  }

  public PagedResultQuery getQuery()
  {
    return preparedQuery;
  }

  @Override
  public void switchPage(int offset, int limit)
  {
    if (preparedQuery != null)
    {
      preparedQuery.setOffset(offset);
      preparedQuery.setLimit(limit);

      // execute the result query again
      executeQuery(false, true);
      if (lastQueryUUID != null && lastCount != null 
        && getQueryPanels().get(lastQueryUUID) != null)
      {
        getQueryPanels().get(lastQueryUUID).setCount(lastCount.getMatchCount());
      }
    }
  }
  
  public void notifiyTabClose(ResultViewPanel panel)
  {
    if(panel != null)
    {
      UUID queryUUID = getQueryPanels().inverse().get(panel);
      if(queryUUID != null)
      {
        getQueries().remove(queryUUID);
        getQueryPanels().remove(queryUUID);
      }
    }
  }
  
  public String getQueryDraft()
  {
    return ui.getControlPanel().getQueryPanel().getQuery();
  }

  private Map<UUID, PagedResultQuery> getQueries()
  {
    if(queries == null)
    {
      queries = HashBiMap.create();
    }
    return queries;
  }


  private BiMap<UUID, ResultViewPanel> getQueryPanels()
  {
    if(queryPanels == null)
    {
      queryPanels = HashBiMap.create();
    }
    return queryPanels;
  }

  private class CountCallback extends Thread
  {

    @Override
    public void run()
    {

      if (futureCount != null)
      {
        UniformInterfaceException cause = null;
        try
        {
          lastCount = futureCount.get();
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

        futureCount = null;

        final UniformInterfaceException causeFinal = cause;
        ui.accessSynchronously(new Runnable()
        {
          @Override
          public void run()
          {
            if (causeFinal == null)
            {
              if (lastCount != null)
              {
                String documentString = lastCount.getDocumentCount() > 1 ? "documents" : "document";
                String matchesString = lastCount.getMatchCount() > 1 ? "matches" : "match";

                ui.getControlPanel().getQueryPanel().setStatus("" + lastCount.
                  getMatchCount() + " " + matchesString
                  + " <br/>in " + lastCount.getDocumentCount() + " " + documentString);
                if (lastQueryUUID != null && lastCount.getMatchCount() > 0
                  && getQueryPanels().get(lastQueryUUID) != null)
                {
                  getQueryPanels().get(lastQueryUUID).setCount(lastCount.getMatchCount());
                }
              }
            }
            else
            {
              if (causeFinal.getResponse().getStatus() == 400)
              {
                Notification.show(
                  "parsing error",
                  causeFinal.getResponse().getEntity(String.class),
                  Notification.Type.WARNING_MESSAGE);
              }
              else if (causeFinal.getResponse().getStatus() == 504) // gateway timeout
              {
                Notification.show(
                  "Timeout: query execution took too long.",
                  "Try to simplyfiy your query e.g. by replacing \"node\" with an annotation name or adding more constraints between the nodes.",
                  Notification.Type.WARNING_MESSAGE);
              }
              else
              {
                log.error("Unexpected exception:  " + causeFinal.
                  getLocalizedMessage(), causeFinal);
                ExceptionDialog.show(causeFinal);
              }
            } // end if cause != null

            ui.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);
          }
        });
      }
    }
  }

  
  
  


}
