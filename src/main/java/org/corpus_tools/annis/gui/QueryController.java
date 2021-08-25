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
package org.corpus_tools.annis.gui;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import com.vaadin.data.Binder;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.BeanContainer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.Future;
import okhttp3.Call;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.JSON;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.BadRequestError;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.CountQuery;
import org.corpus_tools.annis.api.model.QueryAttributeDescription;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.controller.CountCallback;
import org.corpus_tools.annis.gui.controller.ExportBackgroundJob;
import org.corpus_tools.annis.gui.controller.FrequencyBackgroundJob;
import org.corpus_tools.annis.gui.controller.SpecificPagingCallback;
import org.corpus_tools.annis.gui.controlpanel.QueryPanel;
import org.corpus_tools.annis.gui.exporter.ExporterPlugin;
import org.corpus_tools.annis.gui.frequency.FrequencyQueryPanel;
import org.corpus_tools.annis.gui.frequency.UserGeneratedFrequencyEntry;
import org.corpus_tools.annis.gui.media.MediaController;
import org.corpus_tools.annis.gui.objects.ContextualizedQuery;
import org.corpus_tools.annis.gui.objects.DisplayedResultQuery;
import org.corpus_tools.annis.gui.objects.ExportQuery;
import org.corpus_tools.annis.gui.objects.ExportQueryGenerator;
import org.corpus_tools.annis.gui.objects.FrequencyQuery;
import org.corpus_tools.annis.gui.objects.FrequencyTableQuery;
import org.corpus_tools.annis.gui.objects.Match;
import org.corpus_tools.annis.gui.objects.PagedResultQuery;
import org.corpus_tools.annis.gui.objects.Query;
import org.corpus_tools.annis.gui.objects.QueryLanguage;
import org.corpus_tools.annis.gui.objects.QueryUIState;
import org.corpus_tools.annis.gui.resultfetch.ResultFetchJob;
import org.corpus_tools.annis.gui.resultfetch.SingleResultFetchJob;
import org.corpus_tools.annis.gui.resultview.ResultViewPanel;
import org.corpus_tools.annis.gui.resultview.VisualizerContextChanger;
import org.corpus_tools.annis.gui.visualizers.IFrameResourceMap;
import org.corpus_tools.salt.common.SaltProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A controller to modifiy the query UI state. s
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class QueryController implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -5746215715915616348L;

  private static final Logger log = LoggerFactory.getLogger(QueryController.class);



  /**
   * Only changes the value of the property if it is not equals to the old one.
   * 
   * @param <T>
   * @param prop
   * @param newValue
   */
  private static <T> void setIfNew(ListDataProvider<T> prop, Collection<T> newValue) {
    if (!Objects.equals(prop.getItems(), newValue)) {
      prop.getItems().clear();
      prop.getItems().addAll(newValue);
    }
  }


  /**
   * Only changes the value of the property if it is not equals to the old one.
   * 
   * @param <T>
   * @param prop
   * @param newValue
   */
  private static <T> void setIfNew(Property<T> prop, T newValue) {
    if (!Objects.equals(prop.getValue(), newValue)) {
      prop.setValue(newValue);
    }
  }

  private final AnnisUI ui;

  private final SearchView searchView;

  private final QueryUIState state;

  private final Binder<QueryUIState> binder;


  public QueryController(AnnisUI ui, SearchView searchView, QueryUIState state) {
    this.ui = ui;
    this.searchView = searchView;
    this.state = state;

    this.binder = new Binder<>(QueryUIState.class);
    this.binder.setBean(state);

    this.state.getAql().addValueChangeListener(event -> validateQuery());

    this.binder.addValueChangeListener(event -> validateQuery());
  }

  public Binder<QueryUIState> getBinder() {
    return binder;
  }

  /**
   * Adds a history entry to the history panel.
   *
   * @param q the entry, which is added.
   *
   * @see HistoryPanel
   */
  public void addHistoryEntry(Query q) {
    Query queryCopy;
    if (q instanceof DisplayedResultQuery) {
      queryCopy = new DisplayedResultQuery((DisplayedResultQuery) q);
    } else if (q instanceof PagedResultQuery) {
      queryCopy = new PagedResultQuery((PagedResultQuery) q);
    } else if (q instanceof ContextualizedQuery) {
      queryCopy = new ContextualizedQuery((ContextualizedQuery) q);
    }
    else {
      queryCopy = new Query(q);
    }

    // remove it first in order to let it appear on the beginning of the list
    state.getHistory().removeItem(queryCopy);
    state.getHistory().addItemAt(0, queryCopy);
    searchView.getControlPanel().getQueryPanel().updateShortHistory();

  }

  public void cancelExport() {
    Future exportFuture = state.getExecutedTasks().get(QueryUIState.QueryType.EXPORT);
    if (exportFuture != null && !exportFuture.isDone()) {
      if (!exportFuture.cancel(true)) {
        log.warn("Could not cancel export");
      }
    }
  }

  /**
   * Cancel queries from the client side.
   *
   * Important: This does not magically cancel the query on the server side, so don't use this to
   * implement a "real" query cancellation.
   */
  private void cancelSearch() {
    // don't spin forever when canceled
    searchView.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);

    Map<QueryUIState.QueryType, Future<?>> exec = state.getExecutedTasks();
    // abort last tasks if running
    if (exec.containsKey(QueryUIState.QueryType.COUNT)
        && !exec.get(QueryUIState.QueryType.COUNT).isDone()) {
      exec.get(QueryUIState.QueryType.COUNT).cancel(true);
    }
    if (exec.containsKey(QueryUIState.QueryType.FIND)
        && !exec.get(QueryUIState.QueryType.FIND).isDone()) {
      exec.get(QueryUIState.QueryType.FIND).cancel(true);
    }

    exec.remove(QueryUIState.QueryType.COUNT);
    exec.remove(QueryUIState.QueryType.FIND);

  }

  public void changeContext(PagedResultQuery originalQuery, Match match, long offset,
      int newContext, final VisualizerContextChanger visCtxChange, boolean left) {

    final PagedResultQuery newQuery = new PagedResultQuery(originalQuery);
    if (left) {
      newQuery.setLeftContext(newContext);
    } else {
      newQuery.setRightContext(newContext);
    }

    newQuery.setOffset(offset);

    UI ui = UI.getCurrent();
    Background.runWithCallback(new SingleResultFetchJob(match, newQuery, ui),
        new FutureCallback<SaltProject>() {

          @Override
          public void onFailure(Throwable t) {
            ExceptionDialog.show(t, "Could not extend context.", ui);
          }

          @Override
          public void onSuccess(SaltProject result) {
            visCtxChange.updateResult(result, newQuery);
          }
        });

  }

  private void checkQuirksMode(Query query) {
    if (query.getQueryLanguage() == QueryLanguage.AQL_QUIRKS_V3) {
      Notification.show("Using query language compatibility or \"quirks\" mode.",
          "This means the semantics of the query language AQL are changed to match these of the previous ANNIS release 3. "
              + "E.g. the range of the .* operator is limited to at maximum 50 in the compatibilty mode.\n\n"
              + "You can change the query language to the most recent version of AQL in the \"Search Options\" tab of the control pannel.",
          Notification.Type.WARNING_MESSAGE);

    }
  }

  public void corpusSelectionChangedInBackground() {
    searchView.getControlPanel()
        .getCorpusList().selectedCorpusChanged(true);
    searchView.getControlPanel().getSearchOptions()
        .updateSearchPanelConfigurationInBackground(getState().getSelectedCorpora());
  }

  public void executeExport(ExportPanel panel, EventBus eventBus) {

    Future exportFuture = state.getExecutedTasks().get(QueryUIState.QueryType.EXPORT);
    if (exportFuture != null && !exportFuture.isDone()) {
      exportFuture.cancel(true);
    }

    ExportQuery query = getExportQuery();

    checkQuirksMode(query);

    addHistoryEntry(query);

    Optional<ExporterPlugin> exporterImpl =
        ui.getExporterPlugins().stream().filter((e) -> e.getClass().equals(query.getExporter()))
            .findAny();

    UI ui = UI.getCurrent();
    if (exporterImpl.isPresent() && ui instanceof AnnisUI) {
      exportFuture = Background
          .call(new ExportBackgroundJob(query, exporterImpl.get(), (AnnisUI) ui, eventBus, panel));
      state.getExecutedTasks().put(QueryUIState.QueryType.EXPORT, exportFuture);
    }
  }

  public void executeFrequency(FrequencyQueryPanel panel) {
    // kill old request
    Future freqFuture = state.getExecutedTasks().get(QueryUIState.QueryType.FREQUENCY);
    if (freqFuture != null && !freqFuture.isDone()) {
      freqFuture.cancel(true);
    }

    if ("".equals(state.getAql().getValue())) {
      Notification.show("Empty query", Notification.Type.WARNING_MESSAGE);
      panel.showQueryDefinitionPanel();
      return;
    } else if (state.getSelectedCorpora().isEmpty()) {
      Notification.show("Please select a corpus", Notification.Type.WARNING_MESSAGE);
      panel.showQueryDefinitionPanel();
      return;
    }

    BeanContainer<Integer, UserGeneratedFrequencyEntry> container =
        state.getFrequencyTableDefinition();

    FrequencyTableQuery freqDefinition = new FrequencyTableQuery();
    for (Integer id : container.getItemIds()) {
      UserGeneratedFrequencyEntry userGen = container.getItem(id).getBean();
      freqDefinition.add(userGen.toFrequencyTableEntry());
    }

    FrequencyQuery query = QueryGenerator.frequency().query(state.getAql().getValue())
        .corpora(new LinkedHashSet<>(state.getSelectedCorpora()))
        .queryLanguage(state.getQueryLanguageLegacy()).def(freqDefinition).build();

    checkQuirksMode(query);

    addHistoryEntry(query);

    UI ui = UI.getCurrent();
    FrequencyBackgroundJob job = new FrequencyBackgroundJob(ui, this, query, panel);

    freqFuture = Background.call(job);
    state.getExecutedTasks().put(QueryUIState.QueryType.FREQUENCY, freqFuture);
  }

  /**
   * Executes a query.
   * 
   * @param replaceOldTab
   * @param freshQuery If true the offset and the selected matches are reset before executing the
   *        query.
   */
  public void executeSearch(boolean replaceOldTab, boolean freshQuery) {
    UI ui = UI.getCurrent();
    if (freshQuery && ui instanceof AnnisUI) {
      getState().getOffset().setValue(0l);
      getState().getSelectedMatches().setValue(new TreeSet<Long>());
      // get the value for the visible segmentation from the configured context
      Collection<String> selectedCorpora = getState().getSelectedCorpora();
      CorpusConfiguration config = new CorpusConfiguration();
      if (selectedCorpora != null && !selectedCorpora.isEmpty()) {
        config = ((AnnisUI) ui).getCorpusConfigWithCache(selectedCorpora.iterator().next());
      }

      if (config.getView() != null) {
        String configVal = config.getView().getBaseTextSegmentation();
        if ("".equals(configVal) || "tok".equals(configVal)) {
          configVal = null;
        }
        getState().getVisibleBaseText().setValue(configVal);
      }

    }
    // construct a query from the current properties
    DisplayedResultQuery displayedQuery = getSearchQuery();

    searchView.getControlPanel().getQueryPanel().setStatus("Searching...");

    cancelSearch();

    // cleanup resources
    VaadinSession session = VaadinSession.getCurrent();
    session.setAttribute(IFrameResourceMap.class, new IFrameResourceMap());
    if (session.getAttribute(MediaController.class) != null) {
      session.getAttribute(MediaController.class).clearMediaPlayers();
    }

    searchView.updateFragment(displayedQuery);

    if (displayedQuery.getCorpora() == null || displayedQuery.getCorpora().isEmpty()) {
      searchView.getControlPanel().getQueryPanel().setStatus("No corpus selected");
      searchView.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);
      Notification.show("Please select a corpus", Notification.Type.WARNING_MESSAGE);
      return;
    }
    if ("".equals(displayedQuery.getQuery())) {
      searchView.getControlPanel().getQueryPanel().setStatus("Empty query");
      searchView.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);
      Notification.show("Empty query", Notification.Type.WARNING_MESSAGE);
      return;
    }

    checkQuirksMode(displayedQuery);

    addHistoryEntry(displayedQuery);


    //
    // begin execute match fetching
    //
    ResultViewPanel oldPanel = searchView.getLastSelectedResultView();
    if (replaceOldTab) {
      // remove old panel from view
      searchView.closeTab(oldPanel);
    }

    if (ui instanceof AnnisUI) {
      AnnisUI annisUI = (AnnisUI) ui;
      ResultViewPanel newResultView =
          new ResultViewPanel(annisUI, displayedQuery);
      newResultView.getPaging()
          .addCallback(
              new SpecificPagingCallback(annisUI, searchView, newResultView, displayedQuery));

      TabSheet.Tab newTab;

      List<ResultViewPanel> existingResultPanels = getResultPanels();
      String caption = existingResultPanels.isEmpty() ? "Query Result"
          : "Query Result #" + (existingResultPanels.size() + 1);

      newTab = searchView.getMainTab().addTab(newResultView, caption);
      newTab.setClosable(true);
      newTab.setIcon(FontAwesome.SEARCH);

      searchView.getMainTab().setSelectedTab(newResultView);
      searchView.notifiyQueryStarted();

      Background.run(new ResultFetchJob(displayedQuery, newResultView, annisUI));

      //
      // end execute match fetching
      //
      //
      // begin execute count
      //
      // start count query
      searchView.getControlPanel().getQueryPanel().setCountIndicatorEnabled(true);

      SearchApi api = new SearchApi(Helper.getClient(ui));
      CountQuery countQuery = new CountQuery();
      countQuery.setCorpora(new LinkedList<>(displayedQuery.getCorpora()));
      countQuery.setQuery(displayedQuery.getQuery());
      countQuery.setQueryLanguage(displayedQuery.getApiQueryLanguage());
      try {
        Call call = api.countAsync(countQuery,
            new CountCallback(newResultView, displayedQuery.getLimit(), annisUI));

        state.getExecutedCalls().put(QueryUIState.QueryType.COUNT, call);
      } catch (ApiException ex) {
        ExceptionDialog.show(ex, ui);
      }
    }
  }

  /**
   * Get the current query as it is defined by the UI controls.
   *
   * @return
   */
  public ExportQuery getExportQuery() {
    return new ExportQueryGenerator().query(state.getAql().getValue())
        .corpora(new LinkedHashSet<>(state.getSelectedCorpora()))
        .queryLanguage(state.getQueryLanguageLegacy()).left(state.getLeftContext())
        .right(state.getRightContext()).segmentation(state.getVisibleBaseText().getValue())
        .exporter(state.getExporter().getValue())
        .annotations(state.getExportAnnotationKeys().getValue())
        .param(state.getExportParameters().getValue()).alignmc(state.getAlignmc().getValue())
        .build();
  }

  private List<ResultViewPanel> getResultPanels() {
    ArrayList<ResultViewPanel> result = new ArrayList<>();
    for (int i = 0; i < searchView.getMainTab().getComponentCount(); i++) {
      Component c = searchView.getMainTab().getTab(i).getComponent();
      if (c instanceof ResultViewPanel) {
        result.add((ResultViewPanel) c);
      }
    }
    return result;
  }

  /**
   * Get the current query as it is defined by the current {@link QueryUIState}.
   *
   * @return
   */
  public DisplayedResultQuery getSearchQuery() {
    return QueryGenerator.displayed().query(state.getAql().getValue())
        .corpora(new LinkedHashSet<>(state.getSelectedCorpora()))
        .queryLanguage(state.getQueryLanguageLegacy()).left(state.getLeftContext())
        .right(state.getRightContext()).segmentation(state.getContextSegmentation())
        .baseText(state.getVisibleBaseText().getValue()).limit(state.getLimit())
        .offset(state.getOffset().getValue()).order(state.getOrder())
        .selectedMatches(state.getSelectedMatches().getValue()).build();
  }

  public QueryUIState getState() {
    return state;
  }

  /**
   * Show errors that occurred during the execution of a query to the user.
   *
   * @param ex The exception to report in the user interface
   * @param showNotification If true a notification is shown instead of only displaying the error in
   *        the status label.
   */
  public void reportServiceException(ApiException ex, boolean showNotification) {
    QueryPanel qp = searchView.getControlPanel().getQueryPanel();

    String caption = null;
    String description = null;

    if (!ui.handleCommonError(ex, "execute query")) {
      switch (ex.getCode()) {
        case 400:
          BadRequestError error =
              JSON.createGson().create().fromJson(ex.getResponseBody(), BadRequestError.class);

          caption = "Parsing error";
          if (error.getAqLSyntaxError() != null) {
            description = error.getAqLSyntaxError().getDesc();
          } else if (error.getAqLSemanticError() != null) {
            description = error.getAqLSemanticError().getDesc();
          } else if (error.getImpossibleSearch() != null) {
            description = error.getImpossibleSearch();
          } else {
            description = error.toString();
          }
          qp.setError(error);
          qp.setStatus(description);
          break;
        case 504:
          caption = "Timeout";
          description = "Query execution took too long.";
          qp.setStatus(caption + ": " + description);
          break;
        case 403:
          if (!Helper.getUser(ui.getSecurityContext()).isPresent()) {
            // not logged in
            qp.setStatus("You don't have the access rights to query this corpus. "
                + "You might want to login to access more corpora.");
          } else {
            // logged in but wrong user
            caption = "You don't have the access rights to query this corpus. "
                + "You might want to login as another user to access more corpora.";
            qp.setStatus(caption);
          }
          break;
        default:
          log.error("Exception when communicating with service", ex);
          qp.setStatus("Unexpected exception:  " + ex.getMessage());
          ExceptionDialog.show(ex, "Exception when communicating with service.", ui);
          break;
      }

      if (showNotification && caption != null) {
        Notification.show(caption, description, Notification.Type.WARNING_MESSAGE);
      }
    }

    searchView.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);

  }

  public void setQuery(Query q) {
    // Create a new object that can be saved using the binder
    setIfNew(state.getAql(), q.getQuery());
    if (q.getQueryLanguage() != state.getQueryLanguageLegacy()) {
      state.setQueryLanguageLegacy(q.getQueryLanguage());
    }
    if (!Objects.deepEquals(state.getSelectedCorpora(), q.getCorpora())) {
      state.setSelectedCorpora(q.getCorpora());
      corpusSelectionChangedInBackground();
    }

    if (q instanceof ContextualizedQuery) {
      if (!Objects.equals(state.getLeftContext(), ((ContextualizedQuery) q).getLeftContext())) {
        state.setLeftContext(((ContextualizedQuery) q).getLeftContext());
      }
      if (!Objects.equals(state.getRightContext(), ((ContextualizedQuery) q).getRightContext())) {
        state.setRightContext(((ContextualizedQuery) q).getRightContext());
      }
      if (!Objects.equals(state.getContextSegmentation(),
          ((ContextualizedQuery) q).getSegmentation())) {
        state.setContextSegmentation(((ContextualizedQuery) q).getSegmentation());
      }
    }
    if (q instanceof PagedResultQuery) {
      setIfNew(state.getOffset(), ((PagedResultQuery) q).getOffset());
      if (!Objects.equals(state.getLimit(), ((PagedResultQuery) q).getLimit())) {
        state.setLimit(((PagedResultQuery) q).getLimit());
      }
      if (!Objects.equals(state.getOrder(), ((PagedResultQuery) q).getOrder())) {
        state.setOrder(((PagedResultQuery) q).getOrder());
      }
    }
    if (q instanceof DisplayedResultQuery) {
      setIfNew(state.getSelectedMatches(), ((DisplayedResultQuery) q).getSelectedMatches());
      setIfNew(state.getVisibleBaseText(), ((DisplayedResultQuery) q).getBaseText());
    }
    if (q instanceof ExportQuery) {
      setIfNew(state.getExporter(), ((ExportQuery) q).getExporter());
      setIfNew(state.getExportAnnotationKeys(), ((ExportQuery) q).getAnnotationKeys());
      setIfNew(state.getExportParameters(), ((ExportQuery) q).getParameters());
      setIfNew(state.getAlignmc(), ((ExportQuery) q).getAlignmc());
    }


    // Update the binder
    binder.setBean(state);
  }

  public void validateQuery() {
    QueryPanel qp = searchView.getControlPanel().getQueryPanel();

    // reset status
    qp.setError(null);
    qp.setNodes(null);

    String query = state.getAql().getValue();
    if (query == null || query.isEmpty()) {
      qp.setStatus("Empty query");

    } else {
      // validate query
      UI ui = UI.getCurrent();
      Background.runWithCallback(() -> {
        SearchApi api = new SearchApi(Helper.getClient(ui));
        return api.nodeDescriptions(query, org.corpus_tools.annis.api.model.QueryLanguage.AQL);
      }, new FutureCallback<List<QueryAttributeDescription>>() {

        @Override
        public void onSuccess(List<QueryAttributeDescription> nodes) {
          qp.setNodes(nodes);

          if (state.getSelectedCorpora() == null || state.getSelectedCorpora().isEmpty()) {
            qp.setStatus("Please select a corpus from the list below, then click on \"Search\".");
          } else {
            qp.setStatus("Valid query, click on \"Search\" to start searching.");
          }

        }

        @Override
        public void onFailure(Throwable t) {
          if (t instanceof ApiException) {
            reportServiceException((ApiException) t, false);
          }
        }

      });


    }
  }

}
