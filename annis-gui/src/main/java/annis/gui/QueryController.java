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
package annis.gui;

import annis.gui.components.ExceptionDialog;
import annis.gui.controller.CountCallback;
import annis.gui.controller.ExportBackgroundJob;
import annis.gui.controller.FrequencyBackgroundJob;
import annis.gui.controller.SpecificPagingCallback;
import annis.gui.controlpanel.QueryPanel;
import annis.gui.exporter.Exporter;
import annis.gui.frequency.FrequencyQueryPanel;
import annis.gui.frequency.UserGeneratedFrequencyEntry;
import annis.gui.objects.ContextualizedQuery;
import annis.gui.objects.ExportQuery;
import annis.gui.objects.FrequencyQuery;
import annis.gui.objects.PagedResultQuery;
import annis.gui.objects.Query;
import annis.gui.objects.QueryGenerator;
import annis.gui.objects.QueryUIState;
import annis.gui.resultfetch.ResultFetchJob;
import annis.gui.resultfetch.SingleResultFetchJob;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.resultview.VisualizerContextChanger;
import annis.libgui.Background;
import annis.libgui.Helper;
import annis.libgui.media.MediaController;
import annis.libgui.visualizers.IFrameResourceMap;
import annis.model.AqlParseError;
import annis.model.QueryNode;
import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import annis.service.objects.FrequencyTableQuery;
import annis.service.objects.Match;
import annis.service.objects.MatchAndDocumentCount;
import com.google.common.base.Joiner;
import com.google.common.eventbus.EventBus;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A controller to modifiy the query UI state.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class QueryController implements Serializable
{

  private static final Logger log = LoggerFactory.getLogger(
    QueryController.class);

  private final SearchView searchView;
  private final AnnisUI ui;

  private final QueryUIState state;

  private final Map<String, Exporter> exporterMap = new HashMap<>();

  public QueryController(SearchView searchView, AnnisUI ui)
  {
    this.searchView = searchView;
    this.ui = ui;
    this.state = ui.getQueryState();

    this.state.getAql().addValueChangeListener(
      new Property.ValueChangeListener()
      {

        @Override
        public void valueChange(Property.ValueChangeEvent event)
        {
          validateQuery();
        }
      });

    for (Exporter e : SearchView.EXPORTER)
    {
      String name = e.getClass().getSimpleName();
      exporterMap.put(name, e);
    }
  }

  public void validateQuery()
  {
    QueryPanel qp = searchView.getControlPanel().getQueryPanel();

    // reset status
    qp.setErrors(null);
    qp.setNodes(null);

    String query = state.getAql().getValue();
    if (query == null || query.isEmpty())
    {
      qp.setStatus("Empty query");

    }
    else
    {
      // validate query
      try
      {
        AsyncWebResource annisResource = Helper.getAnnisAsyncWebResource();
        Future<List<QueryNode>> future = annisResource.path("query").path(
          "parse/nodes")
          .queryParam("q", Helper.encodeJersey(query))
          .get(new GenericType<List<QueryNode>>()
          {
          });

        // wait for maximal one seconds
        try
        {
          List<QueryNode> nodes = future.get(1, TimeUnit.SECONDS);

          qp.setNodes(nodes);

          if (state.getSelectedCorpora().getValue() == null
            || state.getSelectedCorpora().getValue().isEmpty())
          {
            qp.setStatus(
              "Please select a corpus from the list below, then click on \"Search\".");
          }
          else
          {
            qp.setStatus(
              "Valid query, click on \"Search\" to start searching.");
          }

        }
        catch (InterruptedException ex)
        {
          log.warn(null, ex);
        }
        catch (ExecutionException ex)
        {
          if (ex.getCause() instanceof UniformInterfaceException)
          {
            reportServiceException((UniformInterfaceException) ex.getCause(),
              false);
          }
          else
          {
            // something unknown, report
            ExceptionDialog.show(ex);
          }
        }
        catch (TimeoutException ex)
        {
          qp.setStatus("Validation of query took too long.");
        }

      }
      catch (ClientHandlerException ex)
      {
        log.error(
          "Could not connect to web service", ex);
        ExceptionDialog.show(ex, "Could not connect to web service");
      }
    }
  }

  /**
   * Show errors that occured during the exeuction of a query to the user.
   *
   * @param ex The exception to report in the user interface
   * @param showNotification If true a notification is shown instead of only
   * displaying the error in the status label.
   */
  public void reportServiceException(UniformInterfaceException ex,
    boolean showNotification)
  {
    QueryPanel qp = searchView.getControlPanel().getQueryPanel();

    String caption = null;
    String description = null;

    if (ex.getResponse().getStatus() == 400)
    {
      List<AqlParseError> errors
        = ex.getResponse().getEntity(
          new GenericType<List<AqlParseError>>()
          {
          });
      caption = "Parsing error";
      description = Joiner.on("\n").join(errors);
      qp.setStatus(description);
      qp.setErrors(errors);
    }
    else if (ex.getResponse().getStatus() == 504)
    {
      caption = "Timeout";
      description = "Query execution took too long.";
      qp.setStatus(caption + ": " + description);
    }
    else if (ex.getResponse().getStatus() == 403)
    {
      qp.setStatus("You don't have the access rights to query this corpus. " 
        + "You might want to login to access more corpora.");
      searchView.getMainToolbar().showLoginWindow(true);
    }
    else
    {
      log.error(
        "Exception when communicating with service", ex);
      qp.setStatus("Unexpected exception:  " + ex.getMessage());
      ExceptionDialog.show(ex,
        "Exception when communicating with service.");
    }

    if (showNotification && caption != null)
    {
      Notification.show(caption, description, Notification.Type.WARNING_MESSAGE);
    }

  }

  public void setQuery(Query q)
  {
    state.getAql().setValue(q.getQuery());
    state.getSelectedCorpora().setValue(q.getCorpora());
    if (q instanceof ContextualizedQuery)
    {
      state.getLeftContext().
        setValue(((ContextualizedQuery) q).getLeftContext());
      state.getRightContext().setValue(((ContextualizedQuery) q).
        getRightContext());
      state.getBaseText().setValue(((ContextualizedQuery) q).getSegmentation());
    }
    if (q instanceof PagedResultQuery)
    {
      state.getOffset().setValue(((PagedResultQuery) q).getOffset());
      state.getLimit().setValue(((PagedResultQuery) q).getLimit());
      state.getOrder().setValue(((PagedResultQuery) q).getOrder());
    }
    if (q instanceof ExportQuery)
    {
      state.getExporterName().setValue(((ExportQuery) q).getExporterName());
      state.getExportAnnotationKeys().setValue(((ExportQuery) q).
        getAnnotationKeys());
      state.getExportParameters().setValue(((ExportQuery) q).getParameters());
    }
  }

  /**
   * Get the current query as it is defined by the UI controls.
   *
   * @return
   */
  public PagedResultQuery getSearchQuery()
  {
    return QueryGenerator.paged()
      .query(state.getAql().getValue())
      .corpora(state.getSelectedCorpora().getValue())
      .left(state.getLeftContext().getValue())
      .right(state.getRightContext().getValue())
      .segmentation(state.getBaseText().getValue())
      .limit(state.getLimit().getValue())
      .offset(state.getOffset().getValue())
      .order(state.getOrder().getValue())
      .build();
  }

  /**
   * Get the current query as it is defined by the UI controls.
   *
   * @return
   */
  public ExportQuery getExportQuery()
  {
    return QueryGenerator.export()
      .query(state.getAql().getValue())
      .corpora(state.getSelectedCorpora().getValue())
      .left(state.getLeftContext().getValue())
      .right(state.getRightContext().getValue())
      .segmentation(state.getBaseText().getValue())
      .exporter(state.getExporterName().getValue())
      .annotations(state.getExportAnnotationKeys().getValue())
      .param(state.getExportParameters().getValue())
      .build();
  }

  public void executeSearch(boolean replaceOldTab, boolean startFromFirstPage)
  {
    if (startFromFirstPage)
    {
      getState().getOffset().setValue(0);
    }
    // construct a query from the current properties
    PagedResultQuery pagedQuery = getSearchQuery();

    searchView.getControlPanel().getQueryPanel().setStatus("Searching...");

    cancelSearch();

    // cleanup resources
    VaadinSession session = VaadinSession.getCurrent();
    session.setAttribute(IFrameResourceMap.class, new IFrameResourceMap());
    if (session.getAttribute(MediaController.class) != null)
    {
      session.getAttribute(MediaController.class).clearMediaPlayers();
    }

    searchView.updateFragment(pagedQuery);

    addHistoryEntry(pagedQuery);

    if (pagedQuery.getCorpora() == null || pagedQuery.getCorpora().
      isEmpty())
    {
      Notification.show("Please select a corpus",
        Notification.Type.WARNING_MESSAGE);
      return;
    }
    if ("".equals(pagedQuery.getQuery()))
    {
      Notification.show("Empty query", Notification.Type.WARNING_MESSAGE);
      return;
    }

    AsyncWebResource res = Helper.getAnnisAsyncWebResource();

    //
    // begin execute match fetching
    //
    ResultViewPanel oldPanel = searchView.getLastSelectedResultView();
    if (replaceOldTab)
    {
      // remove old panel from view
      searchView.closeTab(oldPanel);
    }

    ResultViewPanel newResultView = new ResultViewPanel(ui, ui,
      ui.getInstanceConfig(), pagedQuery);
    newResultView.getPaging().addCallback(new SpecificPagingCallback(pagedQuery,
      ui, searchView, newResultView));

    TabSheet.Tab newTab;

    List<ResultViewPanel> existingResultPanels = getResultPanels();
    String caption = existingResultPanels.isEmpty()
      ? "Query Result" : "Query Result #" + (existingResultPanels.size() + 1);

    newTab = searchView.getMainTab().addTab(newResultView, caption);
    newTab.setClosable(true);
    newTab.setIcon(FontAwesome.SEARCH);

    searchView.getMainTab().setSelectedTab(newResultView);
    searchView.notifiyQueryStarted();

    Background.run(new ResultFetchJob(pagedQuery,
      newResultView, ui));

    //
    // end execute match fetching
    //
    // 
    // begin execute count
    //
    // start count query
    searchView.getControlPanel().getQueryPanel().setCountIndicatorEnabled(true);

    Future<MatchAndDocumentCount> futureCount = res.path("query").path("search").
      path("count").
      queryParam("q", Helper.encodeJersey(pagedQuery.getQuery()))
      .queryParam("corpora", StringUtils.join(pagedQuery.getCorpora(), ",")).
      get(
        MatchAndDocumentCount.class);
    state.getExecutedTasks().put(QueryUIState.QueryType.COUNT, futureCount);

    Background.run(new CountCallback(newResultView, pagedQuery.getLimit(), ui));

    //
    // end execute count
    //
  }

  public void executeExport(ExportPanel panel, EventBus eventBus)
  {

    Future exportFuture = state.getExecutedTasks().get(
      QueryUIState.QueryType.EXPORT);
    if (exportFuture != null && !exportFuture.isDone())
    {
      exportFuture.cancel(true);
    }

    ExportQuery query = getExportQuery();

    addHistoryEntry(query);

    exportFuture = Background.call(new ExportBackgroundJob(query,
      getExporterByName(query.getExporterName()), ui, eventBus, panel));
    state.getExecutedTasks().put(QueryUIState.QueryType.EXPORT, exportFuture);
  }

  public void cancelExport()
  {
    Future exportFuture = state.getExecutedTasks().get(
      QueryUIState.QueryType.EXPORT);
    if (exportFuture != null && !exportFuture.isDone())
    {
      if (!exportFuture.cancel(true))
      {
        log.warn("Could not cancel export");
      }
    }
  }

  public void executeFrequency(FrequencyQueryPanel panel)
  {
    // kill old request
    Future freqFuture = state.getExecutedTasks().get(
      QueryUIState.QueryType.FREQUENCY);
    if (freqFuture != null && !freqFuture.isDone())
    {
      freqFuture.cancel(true);
    }

    if ("".equals(state.getAql().getValue()))
    {
      Notification.show("Empty query", Notification.Type.WARNING_MESSAGE);
      panel.showQueryDefinitionPanel();
      return;
    }
    else if (state.getSelectedCorpora().getValue().isEmpty())
    {
      Notification.show("Please select a corpus",
        Notification.Type.WARNING_MESSAGE);
      panel.showQueryDefinitionPanel();
      return;
    }

    BeanContainer<Integer, UserGeneratedFrequencyEntry> container
      = state.getFrequencyTableDefinition();

    FrequencyTableQuery freqDefinition = new FrequencyTableQuery();
    for (Integer id : container.getItemIds())
    {
      UserGeneratedFrequencyEntry userGen
        = container.getItem(id).getBean();
      freqDefinition.add(userGen.toFrequencyTableEntry());
    }
    // additionally add meta data columns
    for (String m : state.getFrequencyMetaData().getValue())
    {
      FrequencyTableEntry entry = new FrequencyTableEntry();
      entry.setType(FrequencyTableEntryType.meta);
      entry.setKey(m);
      freqDefinition.add(entry);
    }

    FrequencyQuery query = QueryGenerator
      .frequency().query(state.getAql().getValue())
      .corpora(state.getSelectedCorpora().getValue())
      .def(freqDefinition).build();

    addHistoryEntry(query);

    FrequencyBackgroundJob job = new FrequencyBackgroundJob(ui, query, panel);

    freqFuture = Background.call(job);
    state.getExecutedTasks().put(QueryUIState.QueryType.FREQUENCY, freqFuture);
  }

  public Exporter getExporterByName(String name)
  {
    return exporterMap.get(name);
  }

  private List<ResultViewPanel> getResultPanels()
  {
    ArrayList<ResultViewPanel> result = new ArrayList<>();
    for (int i = 0; i < searchView.getMainTab().getComponentCount(); i++)
    {
      Component c = searchView.getMainTab().getTab(i).getComponent();
      if (c instanceof ResultViewPanel)
      {
        result.add((ResultViewPanel) c);
      }
    }
    return result;
  }

  /**
   * Cancel queries from the client side.
   *
   * Important: This does not magically cancel the query on the server side, so
   * don't use this to implement a "real" query cancelation.
   */
  private void cancelSearch()
  {
    // don't spin forever when canceled
    searchView.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);

    Map<QueryUIState.QueryType, Future<?>> exec = state.getExecutedTasks();
    // abort last tasks if running
    if (exec.containsKey(QueryUIState.QueryType.COUNT)
      && !exec.get(QueryUIState.QueryType.COUNT).isDone())
    {
      exec.get(QueryUIState.QueryType.COUNT).cancel(true);
    }
    if (exec.containsKey(QueryUIState.QueryType.FIND)
      && !exec.get(QueryUIState.QueryType.FIND).isDone())
    {
      exec.get(QueryUIState.QueryType.FIND).cancel(true);
    }

    exec.remove(QueryUIState.QueryType.COUNT);
    exec.remove(QueryUIState.QueryType.FIND);

  }

  /**
   * Adds a history entry to the history panel.
   *
   * @param q the entry, which is added.
   *
   * @see HistoryPanel
   */
  public void addHistoryEntry(Query q)
  {
    // remove it first in order to let it appear on the beginning of the list
    state.getHistory().removeItem(q);
    state.getHistory().addItemAt(0, q);
    searchView.getControlPanel().getQueryPanel().updateShortHistory();
  }

  public void changeContext(PagedResultQuery originalQuery,
    Match match,
    int offset, int newContext,
    VisualizerContextChanger visCtxChange, boolean left)
  {

    PagedResultQuery newQuery = originalQuery.clone();
    if (left)
    {
      newQuery.setLeftContext(newContext);
    }
    else
    {
      newQuery.setRightContext(newContext);
    }

    newQuery.setOffset(offset);

    Background.run(new SingleResultFetchJob(match, newQuery,
      visCtxChange));

  }

  public void corpusSelectionChangedInBackground()
  {
    searchView.getControlPanel().getSearchOptions()
      .updateSearchPanelConfigurationInBackground(getState().
        getSelectedCorpora().getValue(), ui);
  }

  public QueryUIState getState()
  {
    return ui.getQueryState();
  }

}
