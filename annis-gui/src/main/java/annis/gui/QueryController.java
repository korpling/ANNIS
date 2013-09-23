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
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import java.util.HashMap;
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
public class QueryController implements TabSheet.SelectedTabChangeListener
{

  private static final Logger log = LoggerFactory.getLogger(
    ResultViewPanel.class);

  private SearchUI ui;

  private ResultFetchThread lastMatchThread;
  private ListOrderedSet<HistoryEntry> history;

  private Future<MatchAndDocumentCount> futureCount;
  
  private UUID lastQueryUUID;
  
  private PagedResultQuery preparedQuery;
  
  private transient Map<UUID, PagedResultQuery> queries;
  private transient BiMap<UUID, ResultViewPanel> queryPanels;
  private transient Map<UUID, MatchAndDocumentCount> counts;
  private int maxShortID;
  
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
    return executeQuery(true);
  }
  
  /** Common actions for preparing the executions of a query. */
  private void prepareExecuteQuery()
  {
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

  }
  
  public UUID executeQuery(boolean replaceOldTab)
  {

    Validate.notNull(preparedQuery,
      "You have to set a query before you can execute it.");

    prepareExecuteQuery();
    
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
    
    AsyncWebResource res = Helper.getAnnisAsyncWebResource();

    //
    // begin execute match fetching
    //
    // remove old result from view
    ResultViewPanel oldPanel = getQueryPanels().get(oldQueryUUID);
    
    if (replaceOldTab && oldQueryUUID != null && oldPanel != null)
    {
      removeQuery(oldQueryUUID);
    }


    // create a short ID for display
    maxShortID++;

    ResultViewPanel newResultView = new ResultViewPanel(this, ui, ui.getInstanceConfig());

    
    Tab newTab;
    
    String caption = getQueryPanels().isEmpty() 
      ? "Query Result" : "Query Result #" + maxShortID;
    
    if(replaceOldTab && oldPanel != null)
    {
       ui.getMainTab().replaceComponent(oldPanel, newResultView);
       newTab = ui.getMainTab().getTab(newResultView);
       
       newTab.setCaption(caption);
    }
    else
    {
      newTab = ui.getMainTab().addTab(newResultView, caption);
      newTab.setClosable(true);
      newTab.setIcon(new ThemeResource("tango-icons/16x16/system-search.png"));
    }
    ui.getMainTab().setSelectedTab(newResultView);
    
    newResultView.getPaging().addCallback(new SpecificPagingCallback(
      lastQueryUUID));

    getQueryPanels().put(lastQueryUUID, newResultView);

    ResultFetchThread thread = new ResultFetchThread(preparedQuery, newResultView, ui);
    thread.start();

    //
    // end execute match fetching
    //
    
    // 
    // begin execute count
    //
    
    // start count query
    ui.getControlPanel().getQueryPanel().setCountIndicatorEnabled(true);

    futureCount = res.path("query").path("search").path("count").
      queryParam(
      "q", preparedQuery.getQuery()).queryParam("corpora",
      StringUtils.join(preparedQuery.getCorpora(), ",")).get(
      MatchAndDocumentCount.class);

    new CountCallback(lastQueryUUID).start();
    
    //
    // end execute count
    //
    
    // remember the query object for later re-usage
    getQueries().put(lastQueryUUID, preparedQuery);
    
    return lastQueryUUID;
  }
  
  private void updateMatches(UUID uuid, PagedResultQuery newQuery)
  {
    ResultViewPanel panel = getQueryPanels().get(uuid);
    if(panel != null)
    {
      prepareExecuteQuery();
      
      getQueries().put(uuid, newQuery); 
      ResultFetchThread thread = new ResultFetchThread(newQuery,
        panel, ui);
      thread.start();
    }
  }

  public void corpusSelectionChangedInBackground()
  {
    ui.getControlPanel().getSearchOptions()
      .updateSearchPanelConfigurationInBackground(ui.getControlPanel().
      getCorpusList().
      getSelectedCorpora());

    ui.updateFragementWithSelectedCorpus(getSelectedCorpora());
  }

  @Override
  public void selectedTabChange(TabSheet.SelectedTabChangeEvent event)
  {
    if(event.getTabSheet().getSelectedTab() instanceof ResultViewPanel)
    {
      ResultViewPanel panel = (ResultViewPanel) event.getTabSheet().getSelectedTab();
      UUID uuid = getQueryPanels().inverse().get(panel);
      if(uuid != null)
      {
        lastQueryUUID = uuid;
        PagedResultQuery query = getQueries().get(uuid);
        if(query != null)
        {
          ui.updateFragment(query);
        }
      }
    }
  }
  
  

  public Set<String> getSelectedCorpora()
  {
    return ui.getControlPanel().getCorpusList().getSelectedCorpora();
  }

  public PagedResultQuery getQuery()
  {
    return preparedQuery;
  }
  
  /**
   * Clear the collected informations about a certain query. Also remove
   * any attached {@link ResultViewPanel} for that query.
   * 
   * @param uuid The UUID of the query to remove.
   */
  private void removeQuery(UUID uuid)
  {
    if(uuid != null)
    {      
      getQueries().remove(uuid);
      getQueryPanels().remove(uuid);
      getCounts().remove(uuid);
    }
  }

  public void notifyTabClose(ResultViewPanel panel)
  {
    if(panel != null)
    {
      removeQuery(getQueryPanels().inverse().get(panel));
      ui.getMainTab().removeComponent(panel);
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
      queries = new HashMap<UUID, PagedResultQuery>();
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

  private Map<UUID, MatchAndDocumentCount> getCounts()
  {
    if(counts == null)
    {
      counts = new HashMap<UUID, MatchAndDocumentCount>();
    }
    return counts;
  }
  
  private class SpecificPagingCallback implements PagingCallback
  {
    private UUID uuid;

    public SpecificPagingCallback(UUID uuid)
    {
      this.uuid = uuid;
    }
    
    
    @Override
    public void switchPage(int offset, int limit)
    {
      PagedResultQuery query = getQueries().get(uuid);
      if (query != null)
      {
        query.setOffset(offset);
        query.setLimit(limit);
        
        // execute the result query again
        
        updateMatches(uuid, query);
      }
    }
  }

  private class CountCallback extends Thread
  {
    
    private UUID uuid;

    public CountCallback(UUID uuid)
    {
      this.uuid = uuid;
    }
    
    

    @Override
    public void run()
    {

      final MatchAndDocumentCount countResult;
      MatchAndDocumentCount tmpCountResult = null;
      if (futureCount != null)
      {
        UniformInterfaceException cause = null;
        try
        {
          tmpCountResult = futureCount.get();
          getCounts().put(uuid, tmpCountResult);
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

        futureCount = null;

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

                ui.getControlPanel().getQueryPanel().setStatus("" + countResult.
                  getMatchCount() + " " + matchesString
                  + "\nin " + countResult.getDocumentCount() + " " + documentString);
                if (lastQueryUUID != null && countResult.getMatchCount() > 0
                  && getQueryPanels().get(lastQueryUUID) != null)
                {
                  getQueryPanels().get(lastQueryUUID).getPaging().setPageSize(
                    getQueries().get(uuid).getLimit(), true);
                  getQueryPanels().get(lastQueryUUID).setCount(countResult.getMatchCount());
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
