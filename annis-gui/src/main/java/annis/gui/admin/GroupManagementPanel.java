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

import annis.gui.admin.view.GroupManagementView;
import annis.gui.admin.view.UserManagementView;
import annis.security.Group;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.vaadin.tokenfield.TokenField;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class GroupManagementPanel extends Panel
  implements GroupManagementView
{

  private final List<GroupManagementView.Listener> listeners = new LinkedList<>();

  private final Table tblGroups = new Table();

  private final BeanContainer<String, Group> groupsContainer = new BeanContainer<>(
    Group.class);

  public GroupManagementPanel()
  {
    groupsContainer.setBeanIdProperty("name");

    tblGroups.setContainerDataSource(groupsContainer);
    tblGroups.setEditable(true);
    tblGroups.setSelectable(true);
    tblGroups.setMultiSelect(true);
    tblGroups.setSizeFull();
    
    tblGroups.setTableFieldFactory(new FieldFactory());

    tblGroups.setVisibleColumns("name", "corpora");
    tblGroups.setColumnHeaders("Name", "Allowed corpora");
    
    final TextField txtUserName = new TextField();
    txtUserName.setInputPrompt("New group name");

    Button btAddNewGroup = new Button("Add new group");
    btAddNewGroup.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        for (GroupManagementView.Listener l : listeners)
        {
          l.addNewGroup(txtUserName.getValue());
        }
      }
    });

    Button btDeleteGroup = new Button("Delete selected group(s)");
    btDeleteGroup.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        // get selected users
        Set<String> selectedGroups = (Set<String>) tblGroups.getValue();
        for (GroupManagementView.Listener l : listeners)
        {
          l.deleteGroups(selectedGroups);
        }
      }
    });
    
    HorizontalLayout actionLayout = new HorizontalLayout(txtUserName,
      btAddNewGroup, btDeleteGroup);

    VerticalLayout layout = new VerticalLayout(tblGroups, actionLayout);
    layout.setSizeFull();
    setContent(layout);
  }

  @Override
  public void attach()
  {
    super.attach();
    for (GroupManagementView.Listener l : listeners)
    {
      l.attached();
    }
  }

  @Override
  public void addListener(GroupManagementView.Listener listener)
  {
    listeners.add(listener);
  }

  @Override
  public void setGroupList(Collection<Group> groups)
  {
    groupsContainer.removeAllItems();
    groupsContainer.addAll(groups);
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
          TokenField tokenField = new TokenField();
          tokenField.setPropertyDataSource(container.
            getContainerProperty(itemId,
              propertyId));

          tokenField.addValueChangeListener(new Property.ValueChangeListener()
          {

            @Override
            public void valueChange(Property.ValueChangeEvent event)
            {
              for (GroupManagementView.Listener l : listeners)
              {
                l.groupUpdated(groupsContainer.getItem(itemId).getBean());
              }
            }
          });
          result = tokenField;
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
