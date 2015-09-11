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

import annis.gui.ExampleQueriesPanel;
import annis.gui.CorpusBrowserPanel;
import annis.gui.MetaDataPanel;
import annis.libgui.Helper;
import annis.security.UserConfig;
import annis.libgui.CorpusSet;
import annis.libgui.InstanceConfig;
import annis.gui.QueryController;
import annis.gui.SearchUI;
import annis.libgui.Background;
import annis.service.objects.AnnisCorpus;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.Action;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import static com.vaadin.server.Sizeable.UNITS_EM;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.ws.rs.core.Response;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class CorpusListPanel extends VerticalLayout implements
  AbstractSelect.NewItemHandler, Action.Handler
{

  private static final org.slf4j.Logger log = LoggerFactory.
    getLogger(CorpusListPanel.class);

  private static final Resource INFO_ICON = FontAwesome.INFO_CIRCLE;

  private static final Resource DOC_ICON = FontAwesome.FILE_TEXT_O;

  public static final String ALL_CORPORA = "All";

  // holds the panels of auto generated queries
  private final ExampleQueriesPanel autoGenQueries;

  private final SearchUI ui;

  public enum ActionType
  {

    Add, Remove

  };
  private BeanContainer<String, AnnisCorpus> corpusContainer;

  private final ProgressBar pbLoadCorpora;

  private Table tblCorpora;

  private final QueryController controller;

  private final HorizontalLayout selectionLayout;
  private final ComboBox cbSelection;

  private final TextField txtFilter;

  private transient UserConfig userConfig;

  private List<AnnisCorpus> allCorpora = new LinkedList<>();

  private final InstanceConfig instanceConfig;

  public CorpusListPanel(final QueryController controller,
    InstanceConfig instanceConfig, ExampleQueriesPanel autoGenQueries,
    final SearchUI ui)
  {
    this.controller = controller;
    this.instanceConfig = instanceConfig;
    this.autoGenQueries = autoGenQueries;
    this.ui = ui;

    final CorpusListPanel finalThis = this;

    setSizeFull();

    selectionLayout = new HorizontalLayout();
    selectionLayout.setWidth("100%");
    selectionLayout.setHeight("-1px");
    selectionLayout.setVisible(false);

    Label lblVisible = new Label("Visible: ");
    lblVisible.setSizeUndefined();
    selectionLayout.addComponent(lblVisible);

    cbSelection = new ComboBox();
    cbSelection.setDescription("Choose corpus selection set");
    cbSelection.setWidth("100%");
    cbSelection.setHeight("-1px");
    cbSelection.addStyleName(ValoTheme.COMBOBOX_SMALL);
    cbSelection.setInputPrompt("Add new corpus selection set");
    cbSelection.setNullSelectionAllowed(false);
    cbSelection.setNewItemsAllowed(true);
    cbSelection.setNewItemHandler((AbstractSelect.NewItemHandler) this);
    cbSelection.setImmediate(true);
    cbSelection.addValueChangeListener(new ValueChangeListener()
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      {
        
        
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
    txtFilter.setInputPrompt("Filter");
    txtFilter.setImmediate(true);
    txtFilter.setTextChangeTimeout(500);
    txtFilter.addTextChangeListener(new FieldEvents.TextChangeListener()
    {
      @Override
      public void textChange(FieldEvents.TextChangeEvent event)
      {
        corpusContainer.removeAllContainerFilters();
        if (event.getText() != null && !event.getText().isEmpty())
        {
          Set<String> selectedIDs = getSelectedCorpora();

          corpusContainer.addContainerFilter(
            new SimpleStringFilter("name", event.getText(), true, false));
          // select the first item
          List<String> filteredIDs = corpusContainer.getItemIds();

          Set<String> selectedAndFiltered = new HashSet<>(selectedIDs);
          selectedAndFiltered.retainAll(filteredIDs);

          Set<String> selectedAndOutsideFilter = new HashSet<>(selectedIDs);
          selectedAndOutsideFilter.removeAll(filteredIDs);

          for (String id : selectedAndOutsideFilter)
          {
            tblCorpora.unselect(id);
          }

          if (selectedAndFiltered.isEmpty() && !filteredIDs.isEmpty())
          {
            for (String id : selectedIDs)
            {
              tblCorpora.unselect(id);
            }
            tblCorpora.select(filteredIDs.get(0));
          }
        }
      }
    });
    txtFilter.setWidth("100%");
    txtFilter.setHeight("-1px");
    txtFilter.addStyleName(ValoTheme.TEXTFIELD_SMALL);
    addComponent(txtFilter);

    pbLoadCorpora = new ProgressBar();
    pbLoadCorpora.setCaption("Loading corpus list...");
    pbLoadCorpora.setIndeterminate(true);
    addComponent(pbLoadCorpora);

    tblCorpora = new Table();

    addComponent(tblCorpora);

    corpusContainer = new BeanContainer<>(AnnisCorpus.class);
    corpusContainer.setBeanIdProperty("name");
    corpusContainer.setItemSorter(new CorpusSorter());

    tblCorpora.setVisible(false); // don't show list before it was not loaded
    tblCorpora.setContainerDataSource(corpusContainer);
    tblCorpora.setMultiSelect(true);
    tblCorpora.setPropertyDataSource(ui.getQueryState().getSelectedCorpora());

    tblCorpora.addGeneratedColumn("info", new InfoGenerator());
    tblCorpora.addGeneratedColumn("docs", new DocLinkGenerator());

    tblCorpora.setVisibleColumns("name", "textCount", "tokenCount", "info",
      "docs");
    tblCorpora.setColumnHeaders("Name", "Texts", "Tokens", "", "");
    tblCorpora.setHeight("100%");
    tblCorpora.setWidth("100%");
    tblCorpora.setSelectable(true);
    tblCorpora.setNullSelectionAllowed(false);
    tblCorpora.setColumnExpandRatio("name", 0.6f);
    tblCorpora.setColumnExpandRatio("textCount", 0.15f);
    tblCorpora.setColumnExpandRatio("tokenCount", 0.25f);
    tblCorpora.addStyleName(ValoTheme.TABLE_SMALL);

    tblCorpora.addActionHandler((Action.Handler) this);
    tblCorpora.setImmediate(true);
    tblCorpora.addItemClickListener(new ItemClickEvent.ItemClickListener()
    {
      @Override
      public void itemClick(ItemClickEvent event)
      {
        Set selections = (Set) tblCorpora.getValue();
        if (selections.size() == 1
          && event.isCtrlKey() && tblCorpora.isSelected(event.getItemId()))
        {
          tblCorpora.setValue(null);
        }
      }
    });
    tblCorpora.setItemDescriptionGenerator(new TooltipGenerator());
    tblCorpora.addValueChangeListener(new CorpusTableChangedListener(finalThis));

    Button btReload = new Button();
    btReload.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        updateCorpusSetList(false);
        Notification.show("Reloaded corpus list",
          Notification.Type.HUMANIZED_MESSAGE);
      }
    });
    btReload.setIcon(FontAwesome.REFRESH);
    btReload.setDescription("Reload corpus list");
    btReload.addStyleName(ValoTheme.BUTTON_ICON_ONLY);

    selectionLayout.addComponent(btReload);
    selectionLayout.setComponentAlignment(btReload, Alignment.MIDDLE_RIGHT);

    tblCorpora.setSortContainerPropertyId("name");
    
    setExpandRatio(tblCorpora, 1.0f);
    
    updateCorpusSetList(true);
  }

  public void updateCorpusSetList()
  {
    updateCorpusSetList(false);
  }

  private void updateCorpusSetList(boolean showLoginMessage)
  {
    if (ui != null)
    {
      ui.clearCorpusConfigCache();
    }

    CorpusListUpdater updater = new CorpusListUpdater(showLoginMessage);
    Background.run(updater);
  }

  private void updateCorpusTable()
  {
    corpusContainer.removeAllItems();
    String selectedCorpusSetName = (String) cbSelection.getValue();

    if (selectedCorpusSetName == null || ALL_CORPORA.equals(
      selectedCorpusSetName))
    {
      // add all corpora
      corpusContainer.addAll(allCorpora);
    }
    else if (userConfig != null)
    {
      CorpusSet selectedCS = null;

      // TODO: use map
      List<CorpusSet> corpusSets = new LinkedList<>();
      if (instanceConfig != null && instanceConfig.getCorpusSets() != null)
      {
        corpusSets.addAll(instanceConfig.getCorpusSets());
      }

      if (userConfig.getCorpusSets() != null)
      {
        corpusSets.addAll(userConfig.getCorpusSets());
      }

      for (CorpusSet cs : corpusSets)
      {
        if (cs.getName().equals(selectedCorpusSetName))
        {
          selectedCS = cs;
        }
      }
      if (selectedCS != null)
      {
        LinkedList<AnnisCorpus> shownCorpora = new LinkedList<>();
        for (AnnisCorpus c : allCorpora)
        {
          if (selectedCS.getCorpora().contains(c.getName()))
          {
            shownCorpora.add(c);
          }
        }
        corpusContainer.addAll(shownCorpora);
      }
    }
    tblCorpora.sort();
  }

  /**
   * Updates or initializes the panel, which holds the automatic generated
   * queries.
   */
  private void updateAutoGeneratedQueriesPanel()
  {
    Set<String> corpora = getSelectedCorpora();

    if (corpora.isEmpty())
    {
      corpora.addAll(corpusContainer.getItemIds());
    }
    autoGenQueries.setSelectedCorpusInBackground(corpora);
  }

  private List<AnnisCorpus> getCorpusListFromServer()
  {
    List<AnnisCorpus> result = new LinkedList<>();
    try
    {
      WebResource rootRes = Helper.getAnnisWebResource();
      result = rootRes.path("query").path("corpora")
        .get(new AnnisCorpusListType());
      return result;
    }
    catch (ClientHandlerException ex)
    {
      log.error(null, ex);
      Notification.show("Service not available: " + ex.getLocalizedMessage(),
        Notification.Type.TRAY_NOTIFICATION);
    }
    catch (UniformInterfaceException ex)
    {
      if (ex.getResponse().getStatus() == Response.Status.UNAUTHORIZED.
        getStatusCode())
      {
        Notification.show("You are not authorized to get the corpus list.", ex.
          getMessage(), Notification.Type.WARNING_MESSAGE);
      }
      else if (ex.getResponse().getStatus() == Response.Status.FORBIDDEN.
        getStatusCode())
      {
        Notification.show("Your account has expired.", ex.
          getMessage(), Notification.Type.WARNING_MESSAGE);
        return result;
      }
      else
      {
        log.error(null, ex);
        Notification.show("Remote exception: " + ex.getLocalizedMessage(),
          Notification.Type.TRAY_NOTIFICATION);
      }
    }
    return null;
  }

  private UserConfig getUserConfigFromRemote()
  {
    WebResource rootRes = Helper.getAnnisWebResource();
    // get the current corpus configuration
    return rootRes.path("admin").path("userconfig").
      get(UserConfig.class);
  }

  private void storeChangesRemote()
  {
    WebResource rootRes = Helper.getAnnisWebResource();
    // store the config on the server
    rootRes.path("admin").path("userconfig").post(this.userConfig);
  }

  @Override
  public void addNewItem(String newItemCaption)
  {
    if (!cbSelection.containsId(newItemCaption) && this.userConfig != null)
    {
      cbSelection.addItem(newItemCaption);
      cbSelection.setValue(newItemCaption);

      try
      {
        UserConfig newUserConfig = getUserConfigFromRemote();
        this.userConfig = newUserConfig;
        // add new corpus set to the config
        CorpusSet newSet = new CorpusSet();
        newSet.setName(newItemCaption);
        this.userConfig.getCorpusSets().add(newSet);
        // store the config on the server
        storeChangesRemote();

        // update everything else
        updateCorpusTable();
      }
      catch (ClientHandlerException ex)
      {
        log.error(
          "could not store new corpus set", ex);
        Notification.show("Could not store new corpus set: "
          + ex.getLocalizedMessage(), Type.WARNING_MESSAGE);
      }
      catch (UniformInterfaceException ex)
      {

        if (ex.getResponse().getStatus() == Response.Status.UNAUTHORIZED.
          getStatusCode())
        {
          log.error(ex.getLocalizedMessage());
          Notification.show("Not allowed",
            "you have not the permission to add a new corpus group",
            Type.WARNING_MESSAGE);
        }
        else
        {
          log.error("error occures while storing new corpus set", ex);
          Notification.show("error occures while storing new corpus set",
            "Maybe you will have to log in", Type.WARNING_MESSAGE);
        }
      }
    } // end if new item
  }

  @Override
  public Action[] getActions(Object target, Object sender)
  {
    String corpusName = (String) target;
    LinkedList<Action> result = new LinkedList<>();

    if (target == null)
    {
      // no action for empty space
      return new Action[0];
    }

    if (Helper.getUser() == null)
    {
      // we can't change anything if we are not logged in so don't even try
      return new Action[0];
    }

    if (userConfig != null)
    {
      for (CorpusSet entry : userConfig.getCorpusSets())
      {
        if (entry.getCorpora().contains(corpusName))
        {
          AddRemoveAction action = new AddRemoveAction(ActionType.Remove, entry,
            corpusName, "Remove from " + entry.getName());
          // add possibility to remove
          result.add(action);
        }
        else
        {
          AddRemoveAction action = new AddRemoveAction(ActionType.Add, entry,
            corpusName, "Add to " + entry.getName());
          // add possibility to add
          result.add(action);
        }
      }
    }

    return result.toArray(new Action[result.size()]);
  }

  @Override
  public void handleAction(Action action, Object sender, Object target)
  {
    if (action instanceof AddRemoveAction && this.userConfig != null)
    {
      AddRemoveAction a = (AddRemoveAction) action;

      int idx = this.userConfig.getCorpusSets().indexOf(a.getCorpusSet());
      if (idx > -1)
      {
        CorpusSet set = this.userConfig.getCorpusSets().get(idx);

        if (a.type == ActionType.Remove)
        {
          set.getCorpora().remove(a.getCorpusId());
          if (set.getCorpora().isEmpty())
          {
            // remove the set itself when it gets empty
            this.userConfig.getCorpusSets().remove(set);

            cbSelection.removeItem(a.getCorpusSet().getName());
            cbSelection.select(ALL_CORPORA);
          }
        }
        else if (a.type == ActionType.Add)
        {
          set.getCorpora().add(a.getCorpusId());
        }

        storeChangesRemote();

        // update view
        updateCorpusTable();
      }
    }
  }

  /**
   * Select the corpora
   *
   * @param corpora Corpora to select
   * @param delayScroll
   */
  public void selectCorpora(Set<String> corpora)
  {
    // if the corpus to select is not contained in the corpus set, 
    // reset to the "All corpora" corpus set
    Set<String> visibleCorpora = getVisibleCorpora();
    if (!visibleCorpora.containsAll(corpora))
    {
      setCorpusSet(CorpusListPanel.ALL_CORPORA);
    }

    if (tblCorpora != null)
    {
      tblCorpora.setValue(corpora);
      if (!corpora.isEmpty())
      {
        String firstCorpusName = corpora.iterator().next();
        int idx = corpusContainer.indexOfId(firstCorpusName);
        tblCorpora.setCurrentPageFirstItemIndex(idx);
      }
    }
  }

  /**
   * Get the names of the corpora that are currently selected.
   *
   * @return
   */
  public Set<String> getSelectedCorpora()
  {
    // make a copy
    return new LinkedHashSet<>((Set<String>) tblCorpora.getValue());
  }

  /**
   * Get the names of the corpora that are currently visible and can be choosen
   * by the user.
   *
   * @return
   */
  public Set<String> getVisibleCorpora()
  {
    return new HashSet<>(corpusContainer.getItemIds());
  }

  /**
   * Set the currently displayed corpus set.
   *
   * @param corpusSet
   */
  public void setCorpusSet(String corpusSet)
  {
    cbSelection.select(corpusSet);
  }

  public class DocLinkGenerator implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      final String id = (String) itemId;

      if (ui.getDocBrowserController().docsAvailable(id))
      {
        Button l = new Button();
        l.setStyleName(ValoTheme.BUTTON_BORDERLESS);
        l.setIcon(DOC_ICON);

        l.setDescription("opens the document browser for " + id);
        l.addClickListener(new Button.ClickListener()
        {
          @Override
          public void buttonClick(ClickEvent event)
          {
            ui.getDocBrowserController().openDocBrowser(id);
          }
        });
        return l;
      }

      return "";
    }
  }

  public class InfoGenerator implements Table.ColumnGenerator
  {

    @Override
    public Component generateCell(Table source, Object itemId, Object columnId)
    {
      final String id = (String) itemId;
      final Button l = new Button();
      l.setStyleName(ValoTheme.BUTTON_BORDERLESS);
      l.setIcon(INFO_ICON);
      l.setDescription("show metadata and annotations for " + id);
      l.addClickListener(new Button.ClickListener()
      {
        @Override
        public void buttonClick(ClickEvent event)
        {
          if (controller != null)
          {
            l.setEnabled(false);
            initCorpusBrowser(id, l);
          }
        }
      });

      return l;
    }
  }

  public static class AddRemoveAction extends Action
  {

    private ActionType type;

    private CorpusSet corpusSet;

    private String corpusId;

    public AddRemoveAction(ActionType type, CorpusSet corpusSet, String corpusId,
      String caption)
    {
      super(caption);
      this.type = type;
      this.corpusSet = corpusSet;
      this.corpusId = corpusId;
    }

    public ActionType getType()
    {
      return type;
    }

    public String getCorpusId()
    {
      return corpusId;
    }

    public CorpusSet getCorpusSet()
    {
      return corpusSet;
    }
  }

  private class CorpusTableChangedListener implements ValueChangeListener
  {

    private final CorpusListPanel finalThis;

    public CorpusTableChangedListener(CorpusListPanel finalThis)
    {
      this.finalThis = finalThis;
    }

    @Override
    public void valueChange(ValueChangeEvent event)
    {
      updateAutoGeneratedQueriesPanel();
      controller.corpusSelectionChangedInBackground();
      String query = ui.getQueryState().getAql().getValue();
        if (!query.isEmpty()){
          controller.validateQuery(query);
        }
    }
  }

  private static class AnnisCorpusListType extends GenericType<List<AnnisCorpus>>
  {

    public AnnisCorpusListType()
    {
    }
  }

  public Table getTblCorpora()
  {
    return tblCorpora;
  }

  public void initCorpusBrowser(String topLevelCorpusName, final Button l)
  {

    AnnisCorpus c = corpusContainer.getItem(topLevelCorpusName).getBean();
    MetaDataPanel meta = new MetaDataPanel(c.getName());

    CorpusBrowserPanel browse = new CorpusBrowserPanel(c, controller);
    GridLayout infoLayout = new GridLayout(2, 2);
    infoLayout.setSizeFull();

    String corpusURL = Helper.generateCorpusLink(Sets.newHashSet(
      topLevelCorpusName));
    Label lblLink = new Label("Link to corpus: <a href=\"" + corpusURL + "\">"
      + corpusURL + "</a>", ContentMode.HTML);
    lblLink.setHeight("-1px");
    lblLink.setWidth("-1px");

    infoLayout.addComponent(meta, 0, 0);
    infoLayout.addComponent(browse, 1, 0);
    infoLayout.addComponent(lblLink, 0, 1, 1, 1);

    infoLayout.setRowExpandRatio(0, 1.0f);
    infoLayout.setColumnExpandRatio(0, 0.5f);
    infoLayout.setColumnExpandRatio(1, 0.5f);
    infoLayout.setComponentAlignment(lblLink, Alignment.MIDDLE_CENTER);

    Window window = new Window("Corpus information for " + c.getName()
      + " (ID: " + c.getId() + ")", infoLayout);
    window.setWidth(70, UNITS_EM);
    window.setHeight(45, UNITS_EM);
    window.setResizable(true);
    window.setModal(false);
    window.setResizeLazy(true);

    window.addCloseListener(new Window.CloseListener()
    {

      @Override
      public void windowClose(Window.CloseEvent e)
      {
        l.setEnabled(true);
      }
    });

    UI.getCurrent().addWindow(window);
    window.center();
  }

  public static class TooltipGenerator implements
    AbstractSelect.ItemDescriptionGenerator
  {

    @Override
    public String generateDescription(Component source, Object itemId,
      Object propertyId)
    {
      if ("name".equals(propertyId))
      {
        return (String) itemId;
      }
      return null;
    }

  }

  private class CorpusListUpdater implements Runnable
  {

    private final boolean showLoginMessage;

    public CorpusListUpdater(boolean showLoginMessage)
    {
      this.showLoginMessage = showLoginMessage;
    }

    @Override
    public void run()
    {
      // query in background

      try
      {
        final UserConfig newUserConfig = getUserConfigFromRemote();
        final List<AnnisCorpus> newCorpusList = getCorpusListFromServer();
        
        // update the GUI
        ui.access(new Runnable()
        {
          @Override
          public void run()
          {
            if (newUserConfig != null && newCorpusList != null)
            {
              allCorpora = newCorpusList;
              userConfig = newUserConfig;

              if (VaadinSession.getCurrent().getAttribute(AnnisCorpus.class) == null)
              {
                if (showLoginMessage)
                {
                  if (allCorpora.isEmpty())
                  {
                    Notification.show("No corpora found. Please login "
                      + "(use button at upper right corner) to see more corpora.",
                      Notification.Type.HUMANIZED_MESSAGE);
                  }
                }
              }

              Object oldSelection = cbSelection.getValue();
              cbSelection.removeAllItems();
              cbSelection.addItem(ALL_CORPORA);

              List<CorpusSet> corpusSets = new LinkedList<>();
              if (instanceConfig != null && instanceConfig.getCorpusSets() != null)
              {
                corpusSets.addAll(instanceConfig.getCorpusSets());
              }

              if (userConfig.getCorpusSets() != null)
              {
                corpusSets.addAll(userConfig.getCorpusSets());
              }

              // add the corpus set names in sorted order
              TreeSet<String> corpusSetNames = new TreeSet<>();
              for (CorpusSet cs : corpusSets)
              {
                corpusSetNames.add(cs.getName());
              }
              for (String s : corpusSetNames)
              {
                cbSelection.addItem(s);
              }

              // restore old selection or select the ALL corpus selection
              if (oldSelection != null && cbSelection.containsId(oldSelection))
              {
                cbSelection.select(oldSelection);
              }
              else
              {

                if (instanceConfig != null && instanceConfig.
                  getDefaultCorpusSet() != null
                  && instanceConfig.getDefaultCorpusSet().length() > 0)
                {
                  cbSelection.select(instanceConfig.getDefaultCorpusSet());
                }
                else
                {
                  cbSelection.select(ALL_CORPORA);
                }
              }

              updateCorpusTable();
              updateAutoGeneratedQueriesPanel();
            } // end if querying the server for corpus list was successful
          }
        });
      }
      finally
      {
        ui.access(new Runnable()
        {
          @Override
          public void run()
          {
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
