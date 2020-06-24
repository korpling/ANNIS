/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.controlpanel;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.core.Response;

import org.corpus_tools.ApiClient;
import org.corpus_tools.ApiException;
import org.corpus_tools.Configuration;
import org.corpus_tools.annis.AdministrationApi;
import org.corpus_tools.annis.CorporaApi;
import org.corpus_tools.annis.CorpusConfiguration;
import org.corpus_tools.annis.CorpusList;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.Action;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.util.BeanContainer;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.FieldEvents;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.ui.AbstractSelect;

import annis.gui.AnnisUI;
import annis.gui.CorpusBrowserPanel;
import annis.gui.ExampleQueriesPanel;
import annis.gui.MetaDataPanel;
import annis.gui.components.ExceptionDialog;
import annis.gui.filter.SetFilter;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Background;
import annis.libgui.CorpusSet;
import annis.libgui.Helper;
import annis.libgui.IDGenerator;
import annis.libgui.InstanceConfig;
import annis.security.UserConfig;
import annis.service.objects.AnnisCorpus;

/**
 *
 * @author thomas
 */
public class CorpusListPanel extends VerticalLayout {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CorpusListPanel.class);

  private static final Resource INFO_ICON = FontAwesome.INFO_CIRCLE;

  private static final Resource DOC_ICON = FontAwesome.FILE_TEXT_O;

  public static final String ALL_CORPORA = "All";

  // holds the panels of auto generated queries
  private final ExampleQueriesPanel autoGenQueries;

  private final AnnisUI ui;

  private final ProgressBar pbLoadCorpora;

  private Grid<String> tblCorpora;

  private final HorizontalLayout selectionLayout;

  private final ComboBox<String> cbSelection;

  private final TextField txtFilter;

  private final InstanceConfig instanceConfig;

  public CorpusListPanel(InstanceConfig instanceConfig, ExampleQueriesPanel autoGenQueries, final AnnisUI ui) {
    this.instanceConfig = instanceConfig;
    this.autoGenQueries = autoGenQueries;
    this.ui = ui;

    setSizeFull();

    selectionLayout = new HorizontalLayout();
    selectionLayout.setWidth("100%");
    selectionLayout.setHeight("-1px");
    selectionLayout.setVisible(false);

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
    cbSelection.addValueChangeListener(new ValueChangeListener<String>() {
      @Override
      public void valueChange(ValueChangeEvent<String> event) {
        updateCorpusTable();
        updateAutoGeneratedQueriesPanel();
      }
    });

    selectionLayout.addComponent(cbSelection);
    selectionLayout.setExpandRatio(cbSelection, 1.0f);
    selectionLayout.setSpacing(true);
    selectionLayout.setComponentAlignment(cbSelection, Alignment.MIDDLE_RIGHT);
    selectionLayout.setComponentAlignment(lblVisible, Alignment.MIDDLE_LEFT);

    addComponent(selectionLayout);

    txtFilter = new TextField();
    txtFilter.setVisible(false);
    txtFilter.setPlaceholder("Filter");
    txtFilter.setWidth("100%");
    txtFilter.setHeight("-1px");
    txtFilter.addStyleName(ValoTheme.TEXTFIELD_SMALL);
    addComponent(txtFilter);

    pbLoadCorpora = new ProgressBar();
    pbLoadCorpora.setCaption("Loading corpus list...");
    pbLoadCorpora.setIndeterminate(true);
    addComponent(pbLoadCorpora);

    tblCorpora = new Grid<>();

    addComponent(tblCorpora);

    tblCorpora.setDataProvider(ui.getQueryState().getAvailableCorpora());
    tblCorpora.setSelectionMode(SelectionMode.MULTI);
    Column<String, String> nameColumn = tblCorpora.addColumn(corpus -> corpus);
    nameColumn.setCaption("Corpus");

    Button btReload = new Button();
    btReload.addClickListener(new Button.ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        updateCorpusSetList(false, false);
        Notification.show("Reloaded corpus list", Notification.Type.HUMANIZED_MESSAGE);
      }
    });
    btReload.setIcon(FontAwesome.REFRESH);
    btReload.setDescription("Reload corpus list");
    btReload.addStyleName(ValoTheme.BUTTON_ICON_ONLY);

    selectionLayout.addComponent(btReload);
    selectionLayout.setComponentAlignment(btReload, Alignment.MIDDLE_RIGHT);

    setExpandRatio(tblCorpora, 1.0f);

  }

  @Override
  public void attach() {
    super.attach();

    updateCorpusSetList(true, true);
    IDGenerator.assignIDForFields(CorpusListPanel.this, tblCorpora, txtFilter);
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

  private void updateCorpusTable() {
    String selectedCorpusSetName = (String) cbSelection.getValue();

    if (!ALL_CORPORA.equals(selectedCorpusSetName)) {
      CorpusSet selectedCS = null;

      // TODO: use map
      List<CorpusSet> corpusSets = new LinkedList<>();
      if (instanceConfig != null && instanceConfig.getCorpusSets() != null) {
        corpusSets.addAll(instanceConfig.getCorpusSets());
      }

      for (CorpusSet cs : corpusSets) {
        if (cs.getName().equals(selectedCorpusSetName)) {
          selectedCS = cs;
        }
      }

      if (selectedCS != null) {

        Set<String> corpusNamesByCorpusSet = new HashSet<>(selectedCS.getCorpora());

        Collection<String> allCorpusNames = ui.getQueryState().getAvailableCorpora().getItems();
        corpusNamesByCorpusSet.retainAll(allCorpusNames);

      }
    }
    //tblCorpora.sort(tblCorpora.getColumns().get(0));
  }

  /**
   * Updates or initializes the panel, which holds the automatic generated
   * queries.
   */
  private void updateAutoGeneratedQueriesPanel() {
    // make sure to make a copy since we are adding items to this set
    Set<String> corpora = new HashSet<>(ui.getQueryState().getSelectedCorpora().getItems());

    if (corpora.isEmpty()) {
      corpora.addAll(ui.getQueryState().getAvailableCorpora().getItems());
    }
    autoGenQueries.setSelectedCorpusInBackground(corpora);
  }

  private UserConfig getUserConfigFromRemote() {
    WebResource rootRes = Helper.getAnnisWebResource(ui);
    // get the current corpus configuration
    return rootRes.path("admin").path("userconfig").get(UserConfig.class);
  }

  public void scrollToSelectedCorpus() {
    Collection<String> corpora = ui.getQueryState().getSelectedCorpora().getItems();

    if (tblCorpora != null) {
      if (!corpora.isEmpty() && !ui.getQueryState().getAvailableCorpora().getItems().isEmpty()) {
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

  public Grid<String> getTblCorpora() {
    return tblCorpora;
  }

  public void initCorpusBrowser(String topLevelCorpusName, final Button l) {

    CorporaApi api = new CorporaApi();
    CorpusConfiguration c;
    try {
      c = api.corpusConfiguration(topLevelCorpusName);
    } catch (ApiException ex) {
      ExceptionDialog.show(ex, "Fetching corpus details failed", ui);
      return;
    }

    MetaDataPanel meta = new MetaDataPanel(topLevelCorpusName);

    CorpusBrowserPanel browse = new CorpusBrowserPanel(topLevelCorpusName, c, ui.getQueryController());
    GridLayout infoLayout = new GridLayout(2, 2);
    infoLayout.setSizeFull();

    String corpusURL = Helper.generateCorpusLink(Sets.newHashSet(topLevelCorpusName));
    Label lblLink = new Label("Link to corpus: <a href=\"" + corpusURL + "\">" + corpusURL + "</a>", ContentMode.HTML);
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

    window.addCloseListener(new Window.CloseListener() {

      @Override
      public void windowClose(Window.CloseEvent e) {
        l.setEnabled(true);
      }
    });

    UI.getCurrent().addWindow(window);
    window.center();
  }

  public static class TooltipGenerator implements AbstractSelect.ItemDescriptionGenerator {

    @Override
    public String generateDescription(Component source, Object itemId, Object propertyId) {
      if ("name".equals(propertyId)) {
        return (String) itemId;
      }
      return null;
    }

  }

  private class CorpusListUpdater implements Runnable {

    private final boolean showLoginMessage;

    private final boolean scrollToSelected;

    public CorpusListUpdater(boolean showLoginMessage, boolean scrollToSelected) {
      this.showLoginMessage = showLoginMessage;
      this.scrollToSelected = scrollToSelected;
    }

    @Override
    public void run() {
      // query in background

      try {
        CorporaApi api = new CorporaApi();

        // List all corpora and get additional information about each of them
        final CorpusList newCorpusList = api.listCorpora();

        // update the GUI
        ui.access(new Runnable() {
          @Override
          public void run() {
            if (newCorpusList != null) {
              ui.getQueryState().getAvailableCorpora().getItems().clear();
              ui.getQueryState().getAvailableCorpora().getItems().addAll(newCorpusList);

              if (VaadinSession.getCurrent().getAttribute(AnnisCorpus.class) == null) {
                if (showLoginMessage) {
                  if (newCorpusList.isEmpty()) {
                    Notification.show(
                        "No corpora found. Please login " + "(use button at upper right corner) to see more corpora.",
                        Notification.Type.HUMANIZED_MESSAGE);
                  }
                }
              }

              List<CorpusSet> corpusSets = new LinkedList<>();
              if (instanceConfig != null && instanceConfig.getCorpusSets() != null) {
                corpusSets.addAll(instanceConfig.getCorpusSets());
              }

              updateCorpusTable();
              updateAutoGeneratedQueriesPanel();
              if (scrollToSelected) {
                scrollToSelectedCorpus();
              }
            } // end if querying the server for corpus list was successful
          }
        });
      } catch (Throwable ex) {
        log.warn("Could not get corpus list", ex);
      } finally {
        ui.access(new Runnable() {
          @Override
          public void run() {
            tblCorpora.setVisible(true);
            txtFilter.setVisible(true);
            selectionLayout.setVisible(true);
            pbLoadCorpora.setVisible(false);

          }
        });
      }

    }
  }
}
