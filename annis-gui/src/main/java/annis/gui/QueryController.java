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

import annis.gui.resultfetch.SingleResultFetchJob;
import annis.libgui.Helper;
import annis.gui.beans.HistoryEntry;
import annis.gui.components.ExceptionDialog;
import annis.libgui.media.MediaController;
import annis.gui.objects.PagedResultQuery;
import annis.gui.objects.Query;
import annis.gui.paging.PagingCallback;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.resultview.VisualizerContextChanger;
import annis.libgui.PollControl;
import annis.gui.resultfetch.ResultFetchJob;
import annis.libgui.visualizers.IFrameResourceMap;
import annis.service.objects.Match;
import annis.service.objects.MatchAndDocumentCount;
import annis.service.objects.MatchGroup;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class QueryController implements TabSheet.SelectedTabChangeListener,
  Serializable
{

  private static final Logger log = LoggerFactory.getLogger(
    ResultViewPanel.class);

  private final SearchUI ui;

  private transient Future<?> lastMatchFuture;

  private final ListOrderedSet<HistoryEntry> history;

  private Future<MatchAndDocumentCount> futureCount;

  private UUID lastQueryUUID;

  private PagedResultQuery preparedQuery;

  private Map<UUID, PagedResultQuery> queries;

  private BiMap<UUID, ResultViewPanel> resultPanels;

  private Map<UUID, MatchAndDocumentCount> counts;

  /**
   * Stores updated queries. They are created when single results are queried
   * again with a different context.
   */
  private Map<UUID, Map<Integer, PagedResultQuery>> updatedQueries;

  /**
   * Holds the matches from the last query. Useful for repeated queries in order
   * to change the context.
   */
  private MatchGroup matches;

  private int maxShortID;

  private List<CorpusSelectionChangeListener> corpusSelChangeListeners
    = new LinkedList<>();

  public QueryController(SearchUI ui)
  {
    this.ui = ui;
    this.history = new ListOrderedSet<>();
  }
  

  public void updateCorpusSetList()
  {
    ui.getControlPanel().getCorpusList().updateCorpusSetList();
  }

  public void setQueryFromUI()
  {
    setQuery(ui.getControlPanel().getQueryPanel().getQuery());
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
    ui.getControlPanel().getSearchOptions().setResultsPerPage(query.getLimit());

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
    if (lastMatchFuture != null && !lastMatchFuture.isDone())
    {
      lastMatchFuture.cancel(true);
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

  public void addCorpusSelectionChangeListener(
    CorpusSelectionChangeListener listener)
  {
    if (corpusSelChangeListeners == null)
    {
      corpusSelChangeListeners = new LinkedList<>();
    }
    corpusSelChangeListeners.add(listener);
  }

  public void removeCorpusSelectionChangeListener(
    CorpusSelectionChangeListener listener)
  {
    if (corpusSelChangeListeners == null)
    {
      corpusSelChangeListeners = new LinkedList<>();
    }
    else
    {
      corpusSelChangeListeners.remove(listener);
    }
  }

  public UUID executeQuery()
  {
    return executeQuery(true);
  }

  /**
   * Common actions for preparing the executions of a query.
   */
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

    ui.getControlPanel().getQueryPanel().setStatus("Searching...");
    
    prepareExecuteQuery();

    if (preparedQuery.getCorpora() == null || preparedQuery.getCorpora().
      isEmpty())
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
    ResultViewPanel oldPanel = getResultPanels().get(oldQueryUUID);

    if (replaceOldTab && oldQueryUUID != null && oldPanel != null)
    {
      removeQuery(oldQueryUUID);
    }

    // create a short ID for display
    maxShortID++;

    ResultViewPanel newResultView = new ResultViewPanel(this, ui, lastQueryUUID,
      ui.getInstanceConfig());

    Tab newTab;

    String caption = getResultPanels().isEmpty()
      ? "Query Result" : "Query Result #" + maxShortID;

    if (replaceOldTab && oldPanel != null)
    {
      ui.getMainTab().replaceComponent(oldPanel, newResultView);
      newTab = ui.getMainTab().getTab(newResultView);

      newTab.setCaption(caption);
    }
    else
    {
      newTab = ui.getMainTab().addTab(newResultView, caption);
      newTab.setClosable(true);
      newTab.setIcon(FontAwesome.SEARCH);
    }
    ui.getMainTab().setSelectedTab(newResultView);
    ui.notifiyQueryStarted();

    newResultView.getPaging().addCallback(new SpecificPagingCallback(
      lastQueryUUID));

    getResultPanels().put(lastQueryUUID, newResultView);
    PollControl.runInBackground(500, ui, new ResultFetchJob(preparedQuery,
      newResultView, ui, this));

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

    PollControl.runInBackground(500, ui, new CountCallback(lastQueryUUID));

    //
    // end execute count
    //
    // remember the query object for later re-usage
    getQueries().put(lastQueryUUID, preparedQuery);

    return lastQueryUUID;
  }

  private void updateMatches(UUID uuid, PagedResultQuery newQuery)
  {
    ResultViewPanel panel = getResultPanels().get(uuid);
    if (panel != null && preparedQuery != null)
    {
      prepareExecuteQuery();
      
      getQueries().put(uuid, newQuery);
      
      ui.getControlPanel().getQueryPanel().getPiCount().setVisible(true);
      ui.getControlPanel().getQueryPanel().getPiCount().setEnabled(true);
      
      lastMatchFuture = PollControl.runInBackground(500, ui,
        new ResultFetchJob(newQuery, panel, ui, this));
    }
  }

  public void corpusSelectionChangedInBackground()
  {
    ui.getControlPanel().getSearchOptions()
      .updateSearchPanelConfigurationInBackground(ui.getControlPanel().
        getCorpusList().
        getSelectedCorpora(), ui);

    // Since there is a serious lag when selecting the corpus don't update
    // the corpus fragment any longer.
    // The user can manually get the corpus link with the corpus explorer.
    //ui.updateFragementWithSelectedCorpus(getSelectedCorpora());

    if (corpusSelChangeListeners != null)
    {

      Set<String> selected = getSelectedCorpora();
      for (CorpusSelectionChangeListener listener : corpusSelChangeListeners)
      {
        listener.onCorpusSelectionChanged(selected);
      }
    }
  }

  @Override
  public void selectedTabChange(TabSheet.SelectedTabChangeEvent event)
  {
    if (event.getTabSheet().getSelectedTab() instanceof ResultViewPanel)
    {
      ResultViewPanel panel = (ResultViewPanel) event.getTabSheet().
        getSelectedTab();
      UUID uuid = getResultPanels().inverse().get(panel);
      if (uuid != null)
      {
        lastQueryUUID = uuid;
        PagedResultQuery query = getQueries().get(uuid);
        if (query != null)
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

  /**
   * Get the query that is currently prepared for execution, but not executed
   * yet.
   *
   * @return
   */
  public PagedResultQuery getPreparedQuery()
  {
    return preparedQuery;
  }

  /**
   * Clear the collected informations about a certain query. Also remove any
   * attached {@link ResultViewPanel} for that query.
   *
   * @param uuid The UUID of the query to remove.
   */
  private void removeQuery(UUID uuid)
  {
    if (uuid != null)
    {
      getQueries().remove(uuid);
      getResultPanels().remove(uuid);
      getCounts().remove(uuid);
    }
  }

  public void notifyTabClose(ResultViewPanel panel)
  {
    if (panel != null)
    {
      removeQuery(getResultPanels().inverse().get(panel));
    }
  }

  public String getQueryDraft()
  {
    return ui.getControlPanel().getQueryPanel().getQuery();
  }

  private Map<UUID, PagedResultQuery> getQueries()
  {
    if (queries == null)
    {
      queries = new HashMap<>();
    }
    return queries;
  }

  private BiMap<UUID, ResultViewPanel> getResultPanels()
  {
    if (resultPanels == null)
    {
      resultPanels = HashBiMap.create();
    }
    return resultPanels;
  }

  private Map<UUID, MatchAndDocumentCount> getCounts()
  {
    if (counts == null)
    {
      counts = new HashMap<>();
    }
    return counts;
  }

  private class SpecificPagingCallback implements PagingCallback
  {

    private final UUID uuid;

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

  private class CountCallback implements Runnable
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
                  && getResultPanels().get(lastQueryUUID) != null)
                {
                  getResultPanels().get(lastQueryUUID).getPaging().setPageSize(
                    getQueries().get(uuid).getLimit(), false);
                  getResultPanels().get(lastQueryUUID).setCount(countResult.
                    getMatchCount());
                }
              }
            }
            else
            {
              if (causeFinal.getResponse().getStatus() == 400)
              {
                String errMsg = causeFinal.getResponse().getEntity(String.class);
                Notification.show("parsing error", 
                  errMsg, Notification.Type.WARNING_MESSAGE);
                ui.getControlPanel().getQueryPanel().setStatus(errMsg);
              }
              else if (causeFinal.getResponse().getStatus() == 504) // gateway timeout
              {
                String errMsg =  "Timeout: query execution took too long.";
                Notification.show(
                  errMsg,
                  "Try to simplyfiy your query e.g. by replacing \"node\" with an annotation name or adding more constraints between the nodes.",
                  Notification.Type.WARNING_MESSAGE);
                ui.getControlPanel().getQueryPanel().setStatus(errMsg);
              }
              else
              {
                log.error("Unexpected exception:  " + causeFinal.
                  getLocalizedMessage(), causeFinal);
                ExceptionDialog.show(causeFinal);
                
                ui.getControlPanel().getQueryPanel().setStatus(
                  "Unexpected exception:  " + causeFinal.getMessage());
              }
            } // end if cause != null

            ui.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);
          }
        });
      }
    }
  }

  public void changeCtx(UUID queryID, int offset, int context,
    VisualizerContextChanger visCtxChange, boolean left)
  {

    PagedResultQuery query;

    if (queries.containsKey(queryID) && resultPanels.containsKey(queryID))
    {
      if (updatedQueries == null)
      {
        updatedQueries = new HashMap<>();
      }

      if (!updatedQueries.containsKey(queryID))
      {
        updatedQueries.put(queryID, new HashMap<Integer, PagedResultQuery>());
      }

      if (!updatedQueries.get(queryID).containsKey(offset))
      {
        query = (PagedResultQuery) queries.get(queryID).clone();
        updatedQueries.get(queryID).put(offset, query);
      }
      else
      {
        query = updatedQueries.get(queryID).get(offset);
      }

      if (left)
      {
        query.setContextLeft(context);
      }
      else
      {
        query.setContextRight(context);
      }

      query.setOffset(offset);
      query.setLimit(1);

      if (matches != null && matches.getMatches() != null
        && !matches.getMatches().isEmpty())
      {
        // The size is the match list corresponds to the page size of the 
        // result view, thus we can make an index shift to the right position of 
        // match in the match list via modulo of size of the match list.
        List<Match> extractMatches = matches.getMatches();
        Match m = extractMatches.get(offset % extractMatches.size());

        PollControl.runInBackground(500, ui,
          new SingleResultFetchJob(m, query,
            visCtxChange));
      }
    }
    else
    {
      log.warn("no query with {} found");
      return;
    }
  }

  public void setMatches(MatchGroup matches)
  {
    this.matches = matches;
  }
}
