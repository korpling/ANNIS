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

import annis.gui.CorpusBrowserPanel;
import annis.gui.MetaDataPanel;
import annis.gui.Helper;
import annis.security.AnnisUserConfig;
import annis.gui.CorpusSet;
import annis.gui.InstanceConfig;
import annis.gui.QueryController;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.event.Action;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.HashSet;
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

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CorpusListPanel.class);
  
  private static final ThemeResource INFO_ICON = new ThemeResource("info.gif");
  public static final String ALL_CORPORA = "All";

  public enum ActionType
  {

    Add, Remove
  };
  BeanContainer<String, AnnisCorpus> corpusContainer;
  private Table tblCorpora;
  private QueryController controller;
  private ComboBox cbSelection;
  private transient AnnisUserConfig userConfig;
  private List<AnnisCorpus> allCorpora = new LinkedList<AnnisCorpus>();
  private InstanceConfig instanceConfig;

  public CorpusListPanel(QueryController controller, InstanceConfig instanceConfig)
  {
    this.controller = controller;
    this.instanceConfig = instanceConfig;
    
    final CorpusListPanel finalThis = this;
  
    setSizeFull();
//    setHeight("99%");
//    setWidth("99%");

    HorizontalLayout selectionLayout = new HorizontalLayout();
    selectionLayout.setWidth("100%");
    selectionLayout.setHeight("-1px");

    Label lblVisible = new Label("Visible: ");
    lblVisible.setSizeUndefined();
    selectionLayout.addComponent(lblVisible);

    cbSelection = new ComboBox();
    cbSelection.setDescription("Choose corpus selection set");
    cbSelection.setWidth("100%");
    cbSelection.setHeight("-1px");
    cbSelection.setInputPrompt("Add new corpus selection set");
    cbSelection.setNullSelectionAllowed(false);
    cbSelection.setNewItemsAllowed(true);
    cbSelection.setNewItemHandler((AbstractSelect.NewItemHandler) this);
    cbSelection.setImmediate(true);
    cbSelection.addListener(new ValueChangeListener() 
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      {
        updateCorpusTable();
      }
    });

    selectionLayout.addComponent(cbSelection);
    selectionLayout.setExpandRatio(cbSelection, 1.0f);
    selectionLayout.setSpacing(true);
    selectionLayout.setComponentAlignment(cbSelection, Alignment.MIDDLE_RIGHT);
    selectionLayout.setComponentAlignment(lblVisible, Alignment.MIDDLE_LEFT);

    addComponent(selectionLayout);

    tblCorpora = new Table();
    addComponent(tblCorpora);

    corpusContainer = new BeanContainer<String, AnnisCorpus>(AnnisCorpus.class);
    corpusContainer.setBeanIdProperty("name");
    corpusContainer.setItemSorter(new CorpusSorter());


    tblCorpora.setContainerDataSource(corpusContainer);

    tblCorpora.addGeneratedColumn("info", new InfoGenerator());

    tblCorpora.setVisibleColumns(new String[]
      {
        "name", "textCount", "tokenCount", "info"
      });
    tblCorpora.setColumnHeaders(new String[]
      {
        "Name", "Texts", "Tokens", ""
      });
    tblCorpora.setHeight(100f, UNITS_PERCENTAGE);
    tblCorpora.setWidth(100f, UNITS_PERCENTAGE);
    tblCorpora.setSelectable(true);
    tblCorpora.setMultiSelect(true);
    tblCorpora.setNullSelectionAllowed(false);
    tblCorpora.setColumnExpandRatio("name", 0.6f);
    tblCorpora.setColumnExpandRatio("textCount", 0.15f);
    tblCorpora.setColumnExpandRatio("tokenCount", 0.25f);
    tblCorpora.addActionHandler((Action.Handler) this);
    tblCorpora.setImmediate(true);
    tblCorpora.addListener(new ValueChangeListener() 
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      { 
        finalThis.controller.corpusSelectionChanged();
      }
    });
    
    setExpandRatio(tblCorpora, 1.0f);

    Button btReload = new Button();
    btReload.addListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        updateCorpusSetList(false);
        Notification.show("Reloaded corpus list", 
          Notification.Type.HUMANIZED_MESSAGE);
      }
    });
    btReload.setIcon(new ThemeResource("../runo/icons/16/reload.png"));
    btReload.setDescription("Reload corpus list");
    btReload.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
    
    selectionLayout.addComponent(btReload);
    selectionLayout.setComponentAlignment(btReload, Alignment.MIDDLE_RIGHT);
    
    tblCorpora.setSortContainerPropertyId("name");
    updateCorpusSetList();
  }
  
  
  
  public void updateCorpusSetList()
  {
    updateCorpusSetList(true);
  }
  
  private void updateCorpusSetList(boolean showLoginMessage)
  {
    if(queryServerForCorpusList() && userConfig != null)
    {
      if(VaadinSession.getCurrent().getAttribute(AnnisCorpus.class) == null)
      {
        if(showLoginMessage)
        {
          if(allCorpora.isEmpty())
          {
            Notification.show("No corpora found. Please login "
                + "(use button at upper right corner) to see more corpora.",
                Notification.Type.HUMANIZED_MESSAGE);
          }
          else
          {
            Notification.show(
              "You can login (use button at upper right corner) to get access to more corpora",
              Notification.Type.TRAY_NOTIFICATION);
          }
        }
      }

      Object oldSelection = cbSelection.getValue();
      cbSelection.removeAllItems();
      cbSelection.addItem(ALL_CORPORA);

      List<CorpusSet> corpusSets = new LinkedList<CorpusSet>();
      if(instanceConfig != null && instanceConfig.getCorpusSets() != null)
      {
        corpusSets.addAll(instanceConfig.getCorpusSets());
      }
      if(userConfig != null && userConfig.getCorpusSets() != null)
      {
        corpusSets.addAll(userConfig.getCorpusSets());
      }
      
      // add the corpus set names in sorted order
      TreeSet<String> corpusSetNames = new TreeSet<String>();
      for (CorpusSet cs : corpusSets)
      {
        corpusSetNames.add(cs.getName());
      }
      for(String s : corpusSetNames)
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
        
        if(instanceConfig != null && instanceConfig.getDefaultCorpusSet() != null)
        {
          cbSelection.select(instanceConfig.getDefaultCorpusSet());
        }
        else
        {
          cbSelection.select(ALL_CORPORA);
        }
      }

      updateCorpusTable();
    } // end if querying the server for corpus list was successful
  }

  private void updateCorpusTable()
  {
    corpusContainer.removeAllItems();
    String selectedCorpusSetName = (String) cbSelection.getValue();
    
    if (selectedCorpusSetName == null || ALL_CORPORA.equals(selectedCorpusSetName))
    {
      // add all corpora
      corpusContainer.addAll(allCorpora);
    }
    else if(userConfig != null)
    {
      CorpusSet selectedCS = null;
      
      // TODO: use map
      List<CorpusSet> corpusSets = new LinkedList<CorpusSet>();
      if(instanceConfig != null && instanceConfig.getCorpusSets() != null)
      {
        corpusSets.addAll(instanceConfig.getCorpusSets());
      }
      if(userConfig != null && userConfig.getCorpusSets() != null)
      {
        corpusSets.addAll(userConfig.getCorpusSets());
      }
      
      for(CorpusSet cs : corpusSets)
      {
        if(cs.getName().equals(selectedCorpusSetName))
        {
          selectedCS = cs;
        }
      }
      if(selectedCS != null)
      {
        LinkedList<AnnisCorpus> shownCorpora = new LinkedList<AnnisCorpus>();
        for(AnnisCorpus c : allCorpora)
        {
          if(selectedCS.getCorpora().contains(c.getName()))
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
   * Queries the web service and sets the {@link #allCorpora} and {@link #userConfig} members.
   * @return True if successful
   */
  private boolean queryServerForCorpusList()
  {
    try
    {
      loadFromRemote();
      
      WebResource rootRes = Helper.getAnnisWebResource();
      allCorpora = rootRes.path("query").path("corpora")
        .get(new GenericType<List<AnnisCorpus>>(){});

      return true;
    }
    catch (ClientHandlerException ex)
    {
      log.error(
        null, ex);
      Notification.show("Service not available: "
        + ex.getLocalizedMessage(),
        Notification.Type.TRAY_NOTIFICATION);
    }
    catch (UniformInterfaceException ex)
    {
      if(ex.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode())
      {
        Notification.show("You are not authorized to get the corpus list.", ex.getMessage(), 
          Notification.Type.WARNING_MESSAGE);
      }
      else
      {
        log.error(
          null, ex);
        Notification.show("Remote exception: "
          + ex.getLocalizedMessage(),
          Notification.Type.TRAY_NOTIFICATION);
      }
    }
    catch (Exception ex)
    {
      log.error(
        null, ex);
      Notification.show("Exception: "
        + ex.getLocalizedMessage(),
        Notification.Type.TRAY_NOTIFICATION);
    }
    return false;
  }
  
  private void loadFromRemote()
  {
    WebResource rootRes = Helper.getAnnisWebResource();
    // get the current corpus configuration
    this.userConfig = rootRes.path("admin").path("userconfig").get(AnnisUserConfig.class);
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
        loadFromRemote();
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
          + ex.getLocalizedMessage(),
          Notification.Type.WARNING_MESSAGE);
      }
    } // end if new item
  }

  @Override
  public Action[] getActions(Object target, Object sender)
  {
    String corpusName = (String) target;
    LinkedList<Action> result = new LinkedList<Action>();

    if(target == null)
    {
      // no action for empty space
      return new Action[0];
    }
    
    if (VaadinSession.getCurrent().getAttribute(AnnisCorpus.class) == null)
    {
      // we can't change anything if we are not logged in so don't even try
      return new Action[0];
    }
    
    if(userConfig != null)
    {
      for (CorpusSet entry : userConfig.getCorpusSets())
      {
        if(entry.getCorpora().contains(corpusName))
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

    return result.toArray(new Action[0]);
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

  public static class CorpusSorter extends DefaultItemSorter
  {

    @Override
    protected int compareProperty(Object propertyId, boolean sortDirection,
      Item item1, Item item2)
    {
      if ("name".equals(propertyId))
      {
        String val1 = (String) item1.getItemProperty(propertyId).getValue();
        String val2 = (String) item2.getItemProperty(propertyId).getValue();

        if (sortDirection)
        {
          return val1.compareToIgnoreCase(val2);
        }
        else
        {
          return val2.compareToIgnoreCase(val1);
        }
      }
      else
      {
        return super.compareProperty(propertyId, sortDirection, item1, item2);
      }
    }
  }

  public void selectCorpora(Set<String> corpora)
  {
    if (tblCorpora != null)
    {
      tblCorpora.setValue(corpora);
    }
  }

  public Set<String> getSelectedCorpora()
  {
    Set<String> result = new HashSet<String>();

    for (String id : corpusContainer.getItemIds())
    {
      if (tblCorpora.isSelected(id))
      {
        result.add(id);
      }
    }

    return result;
  }

  public class InfoGenerator implements Table.ColumnGenerator
  {

    @Override
    public Component generateCell(Table source, Object itemId, Object columnId)
    {
      final AnnisCorpus c = corpusContainer.getItem(itemId).getBean();
      Button l = new Button();
      l.setStyleName(BaseTheme.BUTTON_LINK);
      l.setIcon(INFO_ICON);
      l.setDescription("show metadata and annotations for " + c.getName());

      l.addListener(new Button.ClickListener()
      {

        @Override
        public void buttonClick(ClickEvent event)
        {
          MetaDataPanel meta = new MetaDataPanel(c.getName());
          if (controller != null)
          {
            CorpusBrowserPanel browse = new CorpusBrowserPanel(c, controller);
            HorizontalLayout layout = new HorizontalLayout();
            layout.addComponent(meta);
            layout.addComponent(browse);
            layout.setSizeFull();
            layout.setExpandRatio(meta, 0.5f);
            layout.setExpandRatio(browse, 0.5f);

            Window window = new Window("Corpus information for " + c.getName()
              + " (ID: " + c.getId() + ")", layout);
            window.setWidth(70, UNITS_EM);
            window.setHeight(40, UNITS_EM);
            window.setResizable(false);
            window.setModal(false);

            UI.getCurrent().addWindow(window);
            window.center();
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
}
