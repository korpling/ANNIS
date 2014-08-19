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
package annis.gui.admin;

import annis.gui.admin.view.GroupListView;
import annis.security.Group;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class GroupManagementPanel extends Panel
  implements GroupListView
{

  private final List<GroupListView.Listener> listeners = new LinkedList<>();

  private final Table tblGroups = new Table();
  private final TextField txtGroupName;

  private final BeanContainer<String, Group> groupsContainer = new BeanContainer<>(
    Group.class);
  
  private final IndexedContainer corpusNamesContainer = new IndexedContainer();
  public GroupManagementPanel()
  {
    groupsContainer.setBeanIdProperty("name");

    tblGroups.setContainerDataSource(groupsContainer);
    tblGroups.setEditable(true);
    tblGroups.setSelectable(true);
    tblGroups.setMultiSelect(true);
    tblGroups.setSizeFull();
    tblGroups.addStyleName(ChameleonTheme.TABLE_STRIPED);
    tblGroups.addStyleName("grey-selection");

    tblGroups.setTableFieldFactory(new FieldFactory());

    tblGroups.setVisibleColumns("name", "corpora");
    tblGroups.setColumnHeaders("Name", "Allowed corpora (seperate with comma)");

    txtGroupName = new TextField();
    txtGroupName.setInputPrompt("New group name");

    Button btAddNewGroup = new Button("Add new group");
    btAddNewGroup.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        handleAdd();
      }
    });
    btAddNewGroup.addStyleName(ChameleonTheme.BUTTON_DEFAULT);

    Button btDeleteGroup = new Button("Delete selected group(s)");
    btDeleteGroup.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        // get selected users
        Set<String> selectedGroups = (Set<String>) tblGroups.getValue();
        for (GroupListView.Listener l : listeners)
        {
          l.deleteGroups(selectedGroups);
        }
      }
    });

    HorizontalLayout actionLayout = new HorizontalLayout(txtGroupName,
      btAddNewGroup, btDeleteGroup);
    
    VerticalLayout layout = new VerticalLayout(actionLayout, tblGroups);
    layout.setSizeFull();
    layout.setExpandRatio(tblGroups, 1.0f);
    layout.setSpacing(true);
    layout.setMargin(new MarginInfo(true, false, false, false));
    
    layout.setComponentAlignment(actionLayout, Alignment.MIDDLE_CENTER);
    
    setContent(layout);
    setSizeFull();
    
    addActionHandler(new AddGroupHandler(txtGroupName));
  }

  private void handleAdd()
  {
    for (GroupListView.Listener l : listeners)
    {
      l.addNewGroup(txtGroupName.getValue());
    }
  }

  @Override
  public void attach()
  {
    super.attach();
    for (GroupListView.Listener l : listeners)
    {
      l.attached();
    }
  }

  @Override
  public void addListener(GroupListView.Listener listener)
  {
    listeners.add(listener);
  }

  @Override
  public void setGroupList(Collection<Group> groups)
  {
    groupsContainer.removeAllItems();
    groupsContainer.addAll(groups);
  }

  @Override
  public void emptyNewGroupNameTextField()
  {
    txtGroupName.setValue("");
  }

  @Override
  public void setAvailableCorpusNames(Collection<String> corpusNames)
  {
    corpusNamesContainer.removeAllItems();
    for(String n : corpusNames)
    {
      corpusNamesContainer.addItem(n);
    }
  }
  
  

  public class AddGroupHandler implements Action.Handler
  {
    private final Action enterKeyShortcutAction 
      = new ShortcutAction(null, ShortcutAction.KeyCode.ENTER, null);

    private final Object registeredTarget;

    public AddGroupHandler(Object registeredTarget)
    {
      this.registeredTarget = registeredTarget;
    }

    @Override
    public Action[] getActions(Object target, Object sender)
    {
      return new Action[]
      {
        enterKeyShortcutAction
      };
    }
    
    @Override
    public void handleAction(Action action, Object sender, Object target)
    {
      if(action == enterKeyShortcutAction && target == registeredTarget)
      {
        handleAdd();
      }
    }
  }

  public class FieldFactory extends DefaultFieldFactory
  {

    @Override
    public Field<?> createField(Container container, final Object itemId,
      Object propertyId, Component uiContext)
    {
      Field<?> result = null;

      switch ((String) propertyId)
      {
        case "corpora":
          
          PopupTwinColumnSelect selector = new PopupTwinColumnSelect(corpusNamesContainer);
          selector.setWidth("100%");
          selector.addValueChangeListener(new Property.ValueChangeListener()
          {

            @Override
            public void valueChange(Property.ValueChangeEvent event)
            {
              for (GroupListView.Listener l : listeners)
              {
                l.groupUpdated(groupsContainer.getItem(itemId).getBean());
              }
            }
          });

          result = selector;
          break;
        case "name":
          // explicitly request a read-only label for the name
          result = null;
          break;
        default:
          result = super.createField(container, itemId, propertyId, uiContext);
          break;
      }

      return result;
    }
  }
}
