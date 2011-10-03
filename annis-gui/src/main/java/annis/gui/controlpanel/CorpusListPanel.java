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
import annis.gui.MainApp;
import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;
import annis.service.AnnisService;
import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.AnnisCorpusSet;
import com.vaadin.Application;
import com.vaadin.Application.UserChangeEvent;
import com.vaadin.Application.UserChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.event.Action;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author thomas
 */
public class CorpusListPanel extends Panel implements UserChangeListener,
  AbstractSelect.NewItemHandler, ValueChangeListener, Action.Handler
{

  private static final ThemeResource INFO_ICON = new ThemeResource("info.gif");
  public static final String ALL_CORPORA = "All";
  public static final String CORPUSSET_PREFIX = "corpusset_";

  public enum ActionType
  {

    Add, Remove
  };
  BeanContainer<Long, AnnisCorpus> corpusContainer;
  private Table tblCorpora;
  private ControlPanel controlPanel;
  private ComboBox cbSelection;
  private Map<String, Map<Long, AnnisCorpus>> corpusSets = new TreeMap<String, Map<Long, AnnisCorpus>>();

  public CorpusListPanel(ControlPanel controlPanel)
  {
    this.controlPanel = controlPanel;

    setSizeFull();

    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setSizeFull();

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
    cbSelection.addListener((ValueChangeListener) this);

    selectionLayout.addComponent(cbSelection);
    selectionLayout.setExpandRatio(cbSelection, 1.0f);
    selectionLayout.setSpacing(true);
    selectionLayout.setComponentAlignment(cbSelection, Alignment.MIDDLE_RIGHT);
    selectionLayout.setComponentAlignment(lblVisible, Alignment.MIDDLE_LEFT);

    layout.addComponent(selectionLayout);

    tblCorpora = new Table();
    addComponent(tblCorpora);

    corpusContainer = new BeanContainer<Long, AnnisCorpus>(AnnisCorpus.class);
    corpusContainer.setBeanIdProperty("id");
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
    tblCorpora.setColumnExpandRatio("name", 0.7f);
    tblCorpora.setColumnExpandRatio("textCount", 0.15f);
    tblCorpora.setColumnExpandRatio("tokenCount", 0.15f);
    tblCorpora.setColumnWidth("info", 18);
    tblCorpora.addActionHandler((Action.Handler) this);

    layout.setExpandRatio(tblCorpora, 1.0f);

  }

  @Override
  public void attach()
  {
    super.attach();

    getApplication().addListener((UserChangeListener) this);

    tblCorpora.setSortContainerPropertyId("name");
    updateCorpusSetList();
  }

  private void updateCorpusSetList()
  {
    corpusSets.clear();

    Map<Long, AnnisCorpus> allCorpora = getCorpusList((AnnisUser) getApplication().getUser());
    corpusSets.put(ALL_CORPORA, allCorpora);

    AnnisUser user = (AnnisUser) getApplication().getUser();
    if(user != null)
    {
      for(String p : user.stringPropertyNames())
      {
        if(p.startsWith(CORPUSSET_PREFIX))
        {
          String setName = p.substring(CORPUSSET_PREFIX.length());
          Map<Long, AnnisCorpus> corpora = new TreeMap<Long, AnnisCorpus>();

          String corpusString = user.getProperty(p);
          if(!ALL_CORPORA.equals(setName) && corpusString != null)
          {
            String[] splitted = corpusString.split(",");
            for(String s : splitted)
            {
              try
              {
                Long val = Long.parseLong(s);
                AnnisCorpus c = allCorpora.get(val);
                if(c != null)
                {
                  corpora.put(c.getId(), c);
                }
              }
              catch(NumberFormatException ex)
              {
                Logger.getLogger(CorpusListPanel.class.getName()).log(Level.WARNING, "invalid number in corpus set " + setName, ex);
              }
            }
            corpusSets.put(setName, corpora);
          }
        }
      }
    } // end if user not null

    Object oldSelection = cbSelection.getValue();
    cbSelection.removeAllItems();
    for(String n : corpusSets.keySet())
    {
      cbSelection.addItem(n);
    }
    if(oldSelection != null && cbSelection.containsId(oldSelection))
    {
      cbSelection.select(oldSelection);
    }
    else
    {
      cbSelection.select(ALL_CORPORA);
    }

    updateCorpusList();

  }

  private void updateCorpusList()
  {
    corpusContainer.removeAllItems();
    String selectedCorpusSet = (String) cbSelection.getValue();
    if(selectedCorpusSet == null)
    {
      selectedCorpusSet = ALL_CORPORA;
    }
    if(corpusSets.containsKey(selectedCorpusSet))
    {
      corpusContainer.addAll(corpusSets.get(selectedCorpusSet).values());
    }
    tblCorpora.sort();
  }

  private Map<Long, AnnisCorpus> getCorpusList(AnnisUser user)
  {
    Map<Long, AnnisCorpus> result = new TreeMap<Long, AnnisCorpus>();
    try
    {
      AnnisService service = Helper.getService(getApplication(), getWindow());
      if(service != null)
      {
        AnnisCorpusSet corpora = service.getCorpusSet();
        for(AnnisCorpus c : corpora)
        {
          if(user == null || user.getCorpusIdList().contains(c.getId()))
          {
            result.put(c.getId(), c);
          }
        }
      }
    }
    catch(RemoteException ex)
    {
      Logger.getLogger(CorpusListPanel.class.getName()).log(Level.SEVERE,
        null, ex);
      getWindow().showNotification("Remote exception: " + ex.getLocalizedMessage(),
        Notification.TYPE_WARNING_MESSAGE);
    }
    return result;
  }

  @Override
  public void applicationUserChanged(UserChangeEvent event)
  {
    updateCorpusSetList();
  }

  @Override
  public void addNewItem(String newItemCaption)
  {
    if(!cbSelection.containsId(newItemCaption))
    {
      cbSelection.addItem(newItemCaption);
      cbSelection.setValue(newItemCaption);

      corpusSets.put(newItemCaption, new TreeMap<Long, AnnisCorpus>());
      updateCorpusList();

      // add the new item to the user configuration
      Application app = getApplication();
      if(app instanceof MainApp)
      {
        AnnisSecurityManager sm = ((MainApp) app).getSecurityManager();
        AnnisUser user = (AnnisUser) app.getUser();
        if(sm != null
          && !AnnisSecurityManager.FALLBACK_USER.equals(user.getUserName()))
        {
          user.put(CORPUSSET_PREFIX + newItemCaption, "");
          try
          {
            sm.storeUserProperties(user);
          }
          catch(Exception ex)
          {
            Logger.getLogger(CorpusListPanel.class.getName()).log(Level.SEVERE,
              "could not store new corpus set", ex);
            getWindow().showNotification("Could not store new corpus set: " + ex.getLocalizedMessage(),
              Notification.TYPE_ERROR_MESSAGE);
          }

        }
      }
    }
  }

  @Override
  public void valueChange(ValueChangeEvent event)
  {
    updateCorpusList();
  }

  @Override
  public Action[] getActions(Object target, Object sender)
  {
    Long corpusId = (Long) target;
    LinkedList<Action> result = new LinkedList<Action>();

    AnnisUser user = (AnnisUser) getApplication().getUser();
    if(user == null || AnnisSecurityManager.FALLBACK_USER.equals(user.getUserName()))
    {
      return new Action[0];
    }

    for(Map.Entry<String, Map<Long, AnnisCorpus>> entry : corpusSets.entrySet())
    {
      if(entry.getValue() != null && !ALL_CORPORA.equals(entry.getKey())
        && corpusId != null)
      {
        if(entry.getValue().containsKey(corpusId))
        {
          // add possibility to remove
          result.add(new AddRemoveAction(ActionType.Remove, entry.getKey(), corpusId,
            "Remove from " + entry.getKey()));
        }
        else
        {
          // add possibility to add
          result.add(new AddRemoveAction(ActionType.Add, entry.getKey(), corpusId,
            "Add to " + entry.getKey()));
        }
      }
    }

    return result.toArray(new Action[0]);
  }

  @Override
  public void handleAction(Action action, Object sender, Object target)
  {
    AddRemoveAction a = (AddRemoveAction) action;

    Map<Long, AnnisCorpus> set = corpusSets.get(a.getCorpusSet());
    Map<Long, AnnisCorpus> allCorpora = corpusSets.get(ALL_CORPORA);

    if(a.type == ActionType.Remove)
    {
      set.remove(a.getCorpusId());
      if(set.isEmpty())
      {
        // remove the set itself when it gets empty
        corpusSets.remove(a.getCorpusSet());
        cbSelection.removeItem(a.getCorpusSet());
        cbSelection.select(ALL_CORPORA);
      }
    }
    else if(a.type == ActionType.Add)
    {
      set.put(a.getCorpusId(), allCorpora.get(a.getCorpusId()));
    }

    // save to file
    Application app = getApplication();
    if(app instanceof MainApp)
    {
      AnnisSecurityManager sm = ((MainApp) app).getSecurityManager();
      AnnisUser user = (AnnisUser) app.getUser();

      LinkedList<String> keys = new LinkedList<String>(user.stringPropertyNames());

      for(String key : keys)
      {
        if(key.startsWith(CORPUSSET_PREFIX))
        {
          user.remove(key);
        }
      }

      for(Map.Entry<String, Map<Long, AnnisCorpus>> entry : corpusSets.entrySet())
      {
        if(!ALL_CORPORA.equals(entry.getKey()))
        {
          String key = CORPUSSET_PREFIX + entry.getKey();
          String value = StringUtils.join(entry.getValue().keySet(), ",");

          user.setProperty(key, value);
        }
      }

      try
      {
        sm.storeUserProperties(user);
      }
      catch(Exception ex)
      {
        Logger.getLogger(CorpusListPanel.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    // update view
    updateCorpusList();

  }

  public class CorpusSorter extends DefaultItemSorter
  {

    @Override
    protected int compareProperty(Object propertyId, boolean sortDirection, Item item1, Item item2)
    {
      if("name".equals(propertyId))
      {
        String val1 = (String) item1.getItemProperty(propertyId).getValue();
        String val2 = (String) item2.getItemProperty(propertyId).getValue();

        if(sortDirection)
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

  protected void selectCorpora(Map<Long, AnnisCorpus> corpora)
  {
    if(tblCorpora != null)
    {
      tblCorpora.setValue(corpora.keySet());
    }
  }

  protected Map<Long, AnnisCorpus> getSelectedCorpora()
  {
    HashMap<Long, AnnisCorpus> result = new HashMap<Long, AnnisCorpus>();

    for(Long id : corpusContainer.getItemIds())
    {
      if(tblCorpora.isSelected(id))
      {
        AnnisCorpus c = (AnnisCorpus) corpusContainer.getItem(id).getBean();
        result.put(id, c);
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
      l.setDescription(c.getName());

      l.addListener(new Button.ClickListener()
      {

        @Override
        public void buttonClick(ClickEvent event)
        {
          MetaDataPanel meta = new MetaDataPanel(c.getId());
          if(controlPanel != null)
          {
            CorpusBrowserPanel browse = new CorpusBrowserPanel(c, controlPanel);
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
            window.setModal(true);

            getWindow().addWindow(window);
            window.center();

          }
        }
      });

      return l;
    }
  }

  public class AddRemoveAction extends Action
  {

    private ActionType type;
    private String corpusSet;
    private long corpusId;

    public AddRemoveAction(ActionType type, String corpusSet, long corpusId, String caption)
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

    public long getCorpusId()
    {
      return corpusId;
    }

    public String getCorpusSet()
    {
      return corpusSet;
    }
  }
}
