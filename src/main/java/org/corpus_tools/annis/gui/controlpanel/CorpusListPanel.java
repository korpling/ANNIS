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
package org.corpus_tools.annis.gui.controlpanel;

import com.google.common.collect.Sets;
import com.vaadin.data.Binder;
import com.vaadin.data.provider.GridSortOrderBuilder;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Background;
import org.corpus_tools.annis.gui.CorpusBrowserPanel;
import org.corpus_tools.annis.gui.CorpusSet;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.IDGenerator;
import org.corpus_tools.annis.gui.MetaDataPanel;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.objects.QueryUIState;
import org.slf4j.LoggerFactory;
import org.vaadin.extension.gridscroll.GridScrollExtension;

/**
 * Displays a filterable list of corpora.
 * 
 * @author Thomas Krause
 */
public class CorpusListPanel extends VerticalLayout {

  private static final String COMPACT_COLUMN_CLASS = "compact-column";

  private class CorpusListUpdater implements Runnable {

    private final boolean showLoginMessage;

    public CorpusListUpdater(boolean showLoginMessage) {
      this.showLoginMessage = showLoginMessage;
    }

    @Override
    public void run() {

      try {
        // query in background
        CorporaApi api = new CorporaApi(Helper.getClient(ui));

        List<String> corpora = api.listCorpora();

        // update the GUI
        ui.access(() -> {
          availableCorpora = new ListDataProvider<>(corpora);
          availableCorpora.setFilter(filter);
          HashSet<String> oldSelectedItems = new HashSet<>(tblCorpora.getSelectedItems());
          tblCorpora.setDataProvider(availableCorpora);
          // reset the selected items
          tblCorpora.asMultiSelect().setValue(oldSelectedItems);

          if (showLoginMessage) {
            if (corpora.isEmpty()) {
              Notification.show(
                  "No corpora found. Please login "
                      + "(use button at upper right corner) to see more corpora.",
                  Notification.Type.HUMANIZED_MESSAGE);
            }
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


  private final Grid<String> tblCorpora = new Grid<>();
  private final GridScrollExtension<String> tblCorporaScrollExt =
      new GridScrollExtension<>(tblCorpora);

  private final HorizontalLayout selectionLayout;

  private final ComboBox<String> cbSelection;

  private final TextField txtFilter;

  private final AnnisUI ui;

  private final SerializablePredicate<String> filter;

  private ListDataProvider<String> availableCorpora;

  private final Column<String, String> nameColumn;

  private final Column<String, Boolean> selectedColumn;


  public CorpusListPanel(AnnisUI ui) {
    this.ui = ui;

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
    cbSelection.setEmptySelectionAllowed(true);
    cbSelection.setEmptySelectionCaption(ALL_CORPORA);
    cbSelection.addValueChangeListener(cs -> updateCorpusSetList(true));

    selectionLayout.addComponent(cbSelection);
    selectionLayout.setExpandRatio(cbSelection, 1.0f);
    selectionLayout.setSpacing(true);
    selectionLayout.setComponentAlignment(cbSelection, Alignment.MIDDLE_RIGHT);
    selectionLayout.setComponentAlignment(lblVisible, Alignment.MIDDLE_LEFT);

    Button btReload = new Button();
    btReload.addClickListener(event -> {
      updateCorpusSetList(false);
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
      // Always show selected corpora
      if (tblCorpora.getSelectedItems().contains(corpus)) {
        return true;
      }

      // Check if the corpus is included in the corpus set or filtered by the name
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

    tblCorpora.setWidthFull();
    tblCorpora.setHeightFull();

    tblCorpora.setSelectionMode(SelectionMode.MULTI);
    tblCorpora.addStyleName("corpus-list");


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
      l.addStyleNames(ValoTheme.BUTTON_BORDERLESS, ValoTheme.BUTTON_ICON_ONLY,
          ValoTheme.BUTTON_SMALL);
      l.setWidthUndefined();
      return l;
    });
    infoColumn.setExpandRatio(0);
    infoColumn.setStyleGenerator(item -> COMPACT_COLUMN_CLASS);
    infoColumn.setResizable(false);

    Column<String, Button> docBrowserColumn = tblCorpora.addComponentColumn(corpus -> {
      final Button l = new Button();
      l.setIcon(DOC_ICON);
      l.setDescription("opens the document browser for " + corpus);
      l.addClickListener(event -> {
        ui.getSearchView().getDocBrowserController().openDocBrowser(corpus);
      });
      l.addStyleNames(ValoTheme.BUTTON_BORDERLESS, ValoTheme.BUTTON_ICON_ONLY,
          ValoTheme.BUTTON_SMALL);
      return l;
    });
    docBrowserColumn.setExpandRatio(0);
    docBrowserColumn.setStyleGenerator(item -> COMPACT_COLUMN_CLASS);
    docBrowserColumn.setResizable(false);

    nameColumn = tblCorpora.addColumn(corpus -> corpus);
    nameColumn.setCaption("Corpus");
    nameColumn.setId("corpus");
    nameColumn.setExpandRatio(10);
    nameColumn.setStyleGenerator(item -> COMPACT_COLUMN_CLASS);
    nameColumn.setResizable(false);
    
    selectedColumn =
        tblCorpora.addColumn(corpus -> ui.getQueryState().getSelectedCorpora().contains(corpus));
    selectedColumn.setHidden(true);

    tblCorpora.setSortOrder(
        new GridSortOrderBuilder<String>().thenDesc(selectedColumn).thenAsc(nameColumn).build());
    addComponent(tblCorpora);

    tblCorporaScrollExt.addGridScrolledListener(event -> tblCorpora.recalculateColumnWidths());

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
      availableCorpora = new ListDataProvider<>(corpora);
      availableCorpora.setFilter(filter);
      tblCorpora.setDataProvider(availableCorpora);
      if (ui.getInstanceConfig() != null && ui.getInstanceConfig().getCorpusSets() != null) {
        TreeSet<String> corpusSetNames = new TreeSet<>(ui.getInstanceConfig().getCorpusSets()
            .stream().map(CorpusSet::getName).collect(Collectors.toList()));
        cbSelection.setItems(corpusSetNames);
        if (ui.getInstanceConfig().getDefaultCorpusSet() != null
            && !ui.getInstanceConfig().getDefaultCorpusSet().isEmpty()) {
          cbSelection.setSelectedItem(ui.getInstanceConfig().getDefaultCorpusSet());
        }
      }

      
      if (corpora.isEmpty() && Helper.getUser(ui.getSecurityContext()).isPresent()) {
        Notification.show(
            "No corpora found. Please login "
                + "(use button at upper right corner) to see more corpora.",
            Notification.Type.HUMANIZED_MESSAGE);
      }


    } catch (ApiException e) {
      ExceptionDialog.show(e, "Could not get corpus list", getUI());
    }

    Binder<QueryUIState> binder = ui.getQueryController().getBinder();
    MultiSelect<String> corpusSelection = tblCorpora.asMultiSelect();
    binder.forField(corpusSelection).bind(QueryUIState::getSelectedCorpora,
        (state, selectedCorpora) -> ui.getQueryController().setSelectedCorpora(selectedCorpora));


    IDGenerator.assignIDForFields(CorpusListPanel.this, tblCorpora, txtFilter);
    corpusSelectionChanged();
  }

  public void corpusSelectionChanged() {
    // trigger a resort
    tblCorpora.clearSortOrder();
    tblCorpora.setSortOrder(
        new GridSortOrderBuilder<String>().thenDesc(selectedColumn).thenAsc(nameColumn).build());
    if (availableCorpora != null && !availableCorpora.getItems().isEmpty()) {
      tblCorpora.scrollTo(0);
    }
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

  public void updateCorpusSetList(boolean showLoginMessage) {
    if (ui != null) {
      ui.clearCorpusConfigCache();
    }
    CorpusListUpdater updater = new CorpusListUpdater(showLoginMessage);
    Background.run(updater);
  }
}
