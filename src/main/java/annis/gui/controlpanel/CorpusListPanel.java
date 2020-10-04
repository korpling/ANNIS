/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.controlpanel;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.vaadin.data.Binder;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MultiSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.slf4j.LoggerFactory;

import annis.gui.AnnisUI;
import annis.gui.CorpusBrowserPanel;
import annis.gui.ExampleQueriesPanel;
import annis.gui.MetaDataPanel;
import annis.gui.components.ExceptionDialog;
import annis.gui.objects.QueryUIState;
import annis.libgui.Background;
import annis.libgui.CorpusSet;
import annis.libgui.Helper;
import annis.libgui.IDGenerator;

/**
 *
 * @author thomas
 */
public class CorpusListPanel extends VerticalLayout {

  private class CorpusListUpdater implements Runnable {

    private final boolean showLoginMessage;

    private final boolean scrollToSelected;

    public CorpusListUpdater(boolean showLoginMessage, boolean scrollToSelected) {
      this.showLoginMessage = showLoginMessage;
      this.scrollToSelected = scrollToSelected;
    }

    @Override
    public void run() {

      try {
        // query in background
        CorporaApi api = new CorporaApi(Helper.getClient(ui));

        List<String> corpora = api.listCorpora();

        // update the GUI
        ui.access(() -> {
          ListDataProvider<String> availableCorpora = new ListDataProvider<>(corpora);
          availableCorpora.setFilter(filter);
          tblCorpora.setDataProvider(availableCorpora);
          List<CorpusSet> corpusSets = new LinkedList<>();
          if (ui.getInstanceConfig() != null && ui.getInstanceConfig().getCorpusSets() != null) {
            corpusSets.addAll(ui.getInstanceConfig().getCorpusSets());
          }

          if (showLoginMessage) {
            if (corpora.isEmpty()) {
              Notification.show(
                  "No corpora found. Please login "
                      + "(use button at upper right corner) to see more corpora.",
                  Notification.Type.HUMANIZED_MESSAGE);
            }
          }

          if (scrollToSelected) {
            scrollToSelectedCorpus();
          }

        });
      } catch (Throwable ex) {
        log.warn("Could not get corpus list", ex);
      }

    }
  }

  private static final long serialVersionUID = -6395601812288089382L;

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CorpusListPanel.class);

  private static final Resource INFO_ICON = VaadinIcons.INFO_CIRCLE;

  private static final Resource DOC_ICON = VaadinIcons.FILE_TEXT_O;

  public static final String ALL_CORPORA = "All";

  // holds the panels of auto generated queries
  private final ExampleQueriesPanel autoGenQueries;


  private Grid<String> tblCorpora;

  private final HorizontalLayout selectionLayout;

  private final ComboBox<String> cbSelection;

  private final TextField txtFilter;

  private final AnnisUI ui;

  private final SerializablePredicate<String> filter;

  public CorpusListPanel(AnnisUI ui, ExampleQueriesPanel autoGenQueries) {
    this.ui = ui;
    this.autoGenQueries = autoGenQueries;

    setWidthFull();
    setHeightFull();
    setMargin(true);

    selectionLayout = new HorizontalLayout();
    selectionLayout.setWidthFull();
    selectionLayout.setHeightUndefined();

    Label lblVisible = new Label("Visible: ");
    lblVisible.setSizeUndefined();
    selectionLayout.addComponent(lblVisible);

    cbSelection = new ComboBox<>();
    cbSelection.setDescription("Choose corpus selection set");
    cbSelection.setWidth("100%");
    cbSelection.setHeight("-1px");
    cbSelection.addStyleName(ValoTheme.COMBOBOX_SMALL);
    cbSelection.setPlaceholder("Select corpus selection set");
    cbSelection.setEmptySelectionAllowed(false);


    selectionLayout.addComponent(cbSelection);
    selectionLayout.setExpandRatio(cbSelection, 1.0f);
    selectionLayout.setSpacing(true);
    selectionLayout.setComponentAlignment(cbSelection, Alignment.MIDDLE_RIGHT);
    selectionLayout.setComponentAlignment(lblVisible, Alignment.MIDDLE_LEFT);

    Button btReload = new Button();
    btReload.addClickListener(event -> {
      updateCorpusSetList(false, false);
      Notification.show("Reloaded corpus list", Notification.Type.HUMANIZED_MESSAGE);
    });
    btReload.setIcon(VaadinIcons.REFRESH);
    btReload.setDescription("Reload corpus list");
    btReload.addStyleName(ValoTheme.BUTTON_ICON_ONLY);

    selectionLayout.addComponent(btReload);
    selectionLayout.setComponentAlignment(btReload, Alignment.MIDDLE_RIGHT);

    addComponent(selectionLayout);

    txtFilter = new TextField();
    txtFilter.setPlaceholder("Filter");
    txtFilter.setWidth("100%");
    txtFilter.setHeight("-1px");
    txtFilter.addStyleName(ValoTheme.TEXTFIELD_SMALL);
    txtFilter.addValueChangeListener(event -> {
      tblCorpora.getDataProvider().refreshAll();
    });
    addComponent(txtFilter);

    this.filter = corpus -> {
      String selectedCorpusSetName = cbSelection.getValue();

      String corpusNameFilter =
          txtFilter.getValue() == null ? "" : txtFilter.getValue().trim().toLowerCase();

      if (!corpusNameFilter.isEmpty() && !corpus.toLowerCase().contains(corpusNameFilter)) {
        return false;
      } else if (selectedCorpusSetName != null && !ALL_CORPORA.equals(selectedCorpusSetName)) {
        CorpusSet selectedCS = null;

        List<CorpusSet> corpusSets = new LinkedList<>();
        if (ui.getInstanceConfig() != null && ui.getInstanceConfig().getCorpusSets() != null) {
          corpusSets.addAll(ui.getInstanceConfig().getCorpusSets());
        }

        for (CorpusSet cs : corpusSets) {
          if (cs.getName().equals(selectedCorpusSetName)) {
            selectedCS = cs;
          }
        }

        return selectedCS == null || selectedCS.getCorpora().contains(corpus);

      }
      return true;
    };

    tblCorpora = new Grid<>();
    tblCorpora.setWidthFull();
    tblCorpora.setHeightFull();

    tblCorpora.setSelectionMode(SelectionMode.MULTI);
    Column<String, String> nameColumn = tblCorpora.addColumn(corpus -> corpus);
    nameColumn.setCaption("Corpus");
    nameColumn.setId("corpus");
    nameColumn.setExpandRatio(10);

    Column<?, ?> infoColumn = tblCorpora.addComponentColumn(corpus -> {
      final Button l = new Button();
      l.setIcon(INFO_ICON);
      l.setDescription("show metadata and annotations for " + corpus);
      l.addClickListener(event -> {
        if (ui.getQueryController() != null) {
          l.setEnabled(false);
          initCorpusBrowser(corpus, l);
        }
      });
      l.addStyleNames(ValoTheme.BUTTON_BORDERLESS, ValoTheme.BUTTON_ICON_ONLY);
      return l;
    });
    infoColumn.setExpandRatio(0);

    Column<?, ?> docColumn = tblCorpora.addComponentColumn(corpus -> {
      final Button l = new Button();
      l.setIcon(DOC_ICON);
      l.setDescription("opens the document browser for " + corpus);
      l.addClickListener(event -> {
        ui.getSearchView().getDocBrowserController().openDocBrowser(corpus);
      });
      l.addStyleNames(ValoTheme.BUTTON_BORDERLESS, ValoTheme.BUTTON_ICON_ONLY);
      return l;
    });
    infoColumn.setExpandRatio(0);

    addComponent(tblCorpora);



    setExpandRatio(tblCorpora, 1.0f);

  }

  @Override
  public void attach() {
    super.attach();

    // Get the initial corpus list, this must become before the binder is set,
    // to make sure any selected value is also an item.
    CorporaApi api = new CorporaApi(Helper.getClient(ui));

    try {
      List<String> corpora = api.listCorpora();
      ListDataProvider<String> availableCorpora = new ListDataProvider<>(corpora);
      availableCorpora.setFilter(filter);
      tblCorpora.setDataProvider(availableCorpora);
      List<CorpusSet> corpusSets = new LinkedList<>();
      if (ui.getInstanceConfig() != null && ui.getInstanceConfig().getCorpusSets() != null) {
        corpusSets.addAll(ui.getInstanceConfig().getCorpusSets());
      }

      if (corpora.isEmpty() && Helper.getUser(ui.getSecurityContext()).isPresent()) {
        Notification.show(
            "No corpora found. Please login "
                + "(use button at upper right corner) to see more corpora.",
            Notification.Type.HUMANIZED_MESSAGE);
      }


    } catch (ApiException e) {
      ExceptionDialog.show(e, "Coould not get corpus list", getUI());
    }

    Binder<QueryUIState> binder = new Binder<>();
    MultiSelect<String> corpusSelection = tblCorpora.asMultiSelect();
    binder.forField(corpusSelection).bind(QueryUIState::getSelectedCorpora,
        QueryUIState::setSelectedCorpora);
    binder.setBean(ui.getQueryState());

    binder.addValueChangeListener(event -> {
      Set<String> corpora = new HashSet<>(ui.getQueryState().getSelectedCorpora());
      autoGenQueries.setSelectedCorpusInBackground(corpora);
      ui.getQueryController().corpusSelectionChangedInBackground();
    });


    IDGenerator.assignIDForFields(CorpusListPanel.this, tblCorpora, txtFilter);
  }

  public Grid<String> getTblCorpora() {
    return tblCorpora;
  }

  public void initCorpusBrowser(String topLevelCorpusName, final Button l) {

    MetaDataPanel meta = new MetaDataPanel(topLevelCorpusName);

    CorpusBrowserPanel browse = new CorpusBrowserPanel(topLevelCorpusName, ui.getQueryController());
    GridLayout infoLayout = new GridLayout(2, 2);
    infoLayout.setSizeFull();

    String corpusURL = Helper.generateCorpusLink(Sets.newHashSet(topLevelCorpusName));
    Label lblLink = new Label("Link to corpus: <a href=\"" + corpusURL + "\">" + corpusURL + "</a>",
        ContentMode.HTML);
    lblLink.setHeight("-1px");
    lblLink.setWidth("-1px");

    infoLayout.addComponent(meta, 0, 0);
    infoLayout.addComponent(browse, 1, 0);
    infoLayout.addComponent(lblLink, 0, 1, 1, 1);

    infoLayout.setRowExpandRatio(0, 1.0f);
    infoLayout.setColumnExpandRatio(0, 0.5f);
    infoLayout.setColumnExpandRatio(1, 0.5f);
    infoLayout.setComponentAlignment(lblLink, Alignment.MIDDLE_CENTER);

    Window window = new Window("Corpus information for " + topLevelCorpusName, infoLayout);
    window.setWidth(70, Unit.EM);
    window.setHeight(45, Unit.EM);
    window.setResizable(true);
    window.setModal(false);
    window.setResizeLazy(true);

    window.addCloseListener(e -> l.setEnabled(true));

    UI.getCurrent().addWindow(window);
    window.center();
  }

  public void scrollToSelectedCorpus() {
    Collection<String> corpora = ui.getQueryState().getSelectedCorpora();

    if (tblCorpora != null) {
      if (!corpora.isEmpty()) {
        String firstCorpusName = corpora.iterator().next();
        tblCorpora.select(firstCorpusName);
      }
    }
  }

  /**
   * Set the currently displayed corpus set.
   *
   * @param corpusSet
   */
  public void setCorpusSet(String corpusSet) {
    cbSelection.setValue(corpusSet);
  }

  public void selectedCorpusChanged(boolean scrollToSelected) {
    tblCorpora.asMultiSelect().setValue(ui.getQueryController().getState().getSelectedCorpora());
    if (scrollToSelected) {
      scrollToSelectedCorpus();
    }
  }

  public void updateCorpusSetList(boolean scrollToSelected) {
    updateCorpusSetList(false, scrollToSelected);
  }

  private void updateCorpusSetList(boolean showLoginMessage, boolean scrollToSelected) {
    if (ui != null) {
      ui.clearCorpusConfigCache();
    }
    CorpusListUpdater updater = new CorpusListUpdater(showLoginMessage, scrollToSelected);
    Background.run(updater);
  }
}
