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

import annis.gui.admin.view.UserListView;
import annis.security.User;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItemContainer;
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
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
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
public class UserManagementPanel extends Panel
  implements UserListView
{

  private final VerticalLayout layout;

  private final Table userList;

  private final BeanContainer<String, User> userContainer;

  private final List<UserListView.Listener> listeners = new LinkedList<>();

  private final TextField txtUserName;
  
  private final IndexedContainer groupsContainer = new IndexedContainer();
  
  public UserManagementPanel()
  {

    userContainer = new BeanContainer<>(User.class);
    userContainer.setBeanIdProperty("name");

    userList = new Table();
    userList.setEditable(true);
    userList.setSelectable(true);
    userList.setMultiSelect(true);
    userList.addStyleName(ChameleonTheme.TABLE_STRIPED);
    userList.addStyleName("grey-selection");
    userList.setSizeFull();
    userList.setContainerDataSource(userContainer);
    userList.addGeneratedColumn("changepassword",
      new PasswordChangeColumnGenerator());

    userList.
      setVisibleColumns("name", "groups", "permissions", "changepassword");
    userList.
      setColumnHeaders("Username", "Groups (seperate with comma)", "Additional permissions (seperate with comma)", "");

    userList.setTableFieldFactory(new FieldFactory());

    txtUserName = new TextField();
    txtUserName.setInputPrompt("New user name");

    Button btAddNewUser = new Button("Add new user");
    btAddNewUser.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        handleAdd();
      }
    });
    btAddNewUser.addStyleName(ChameleonTheme.BUTTON_DEFAULT);

    Button btDeleteUser = new Button("Delete selected user(s)");
    btDeleteUser.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        // get selected users
        Set<String> selectedUsers = (Set<String>) userList.getValue();
        for (UserListView.Listener l : listeners)
        {
          l.deleteUsers(selectedUsers);
        }
      }
    });

    HorizontalLayout actionLayout = new HorizontalLayout(txtUserName,
      btAddNewUser, btDeleteUser);

    layout = new VerticalLayout(actionLayout, userList);
    layout.setSizeFull();
    layout.setExpandRatio(userList, 1.0f);
    layout.setSpacing(true);
    layout.setMargin(new MarginInfo(true, false, false, false));
    
    layout.setComponentAlignment(actionLayout, Alignment.MIDDLE_CENTER);
    
    setContent(layout);
    setSizeFull();
    
    addActionHandler(new AddUserHandler(txtUserName));

  }

  private void handleAdd()
  {
    for (UserListView.Listener l : listeners)
    {
      l.addNewUser(txtUserName.getValue());
    }
  }

  @Override
  public void addListener(UserListView.Listener listener)
  {
    listeners.add(listener);
  }

  @Override
  public void askForPasswordChange(String userName)
  {
    NewPasswordWindow w = new NewPasswordWindow(userName, listeners);
    UI.getCurrent().addWindow(w);
    w.center();
  }

  @Override
  public void setUserList(Collection<User> users)
  {
    userContainer.removeAllItems();
    userContainer.addAll(users);
  }

  @Override
  public void emptyNewUserNameTextField()
  {
    txtUserName.setValue("");
  }

  @Override
  public void addAvailableGroupNames(Collection<String> groupNames)
  {
    for(String g : groupNames)
    {
      groupsContainer.addItem(g);
    }
  }
  
  
  
  public class AddUserHandler implements Action.Handler
  {

    private final Action enterKeyShortcutAction
      = new ShortcutAction(null, ShortcutAction.KeyCode.ENTER, null);

    private final Object registeredTarget;

    public AddUserHandler(Object registeredTarget)
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
      if (action == enterKeyShortcutAction && target == registeredTarget)
      {
        handleAdd();
      }
    }
  }

  public class PasswordChangeColumnGenerator implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, final Object itemId,
      Object columnId)
    {
      PasswordField txtNewPassword = new PasswordField();
      txtNewPassword.setInputPrompt("New password");
      Button btChangePassword = new Button("Change password");
      btChangePassword.addClickListener(new Button.ClickListener()
      {

        @Override
        public void buttonClick(Button.ClickEvent event)
        {
          askForPasswordChange((String) itemId);
        }
      });
      return btChangePassword;
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
        case "groups":

          PopupTwinColumnSelect groupsSelector = new PopupTwinColumnSelect(groupsContainer);
          groupsSelector.setWidth("100%");
          groupsSelector.addValueChangeListener(new Property.ValueChangeListener()
          {

            @Override
            public void valueChange(Property.ValueChangeEvent event)
            {
              for (UserListView.Listener l : listeners)
              {
                l.userUpdated(userContainer.getItem(itemId).getBean());
              }
            }
          });

          result = groupsSelector;
          
          break;
        case "permissions":
          
          PopupTwinColumnSelect permissionSelector = new PopupTwinColumnSelect(null);
          permissionSelector.setWidth("100%");
          permissionSelector.addValueChangeListener(new Property.ValueChangeListener()
          {

            @Override
            public void valueChange(Property.ValueChangeEvent event)
            {
              for (UserListView.Listener l : listeners)
              {
                l.userUpdated(userContainer.getItem(itemId).getBean());
              }
            }
          });

          result = permissionSelector;
          
          break;
        case "name":
          // explicitly request a read-only label for the name and groups
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
