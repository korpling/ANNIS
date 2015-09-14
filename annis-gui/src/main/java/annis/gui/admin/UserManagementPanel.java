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
import annis.gui.converter.CommaSeperatedStringConverterSet;
import annis.security.User;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class UserManagementPanel extends Panel
  implements UserListView
{

  private final VerticalLayout layout;

  private final HorizontalLayout actionLayout;

  private final Grid userList;

  private final BeanContainer<String, User> userContainer;

  private final List<UserListView.Listener> listeners = new LinkedList<>();

  private final TextField txtUserName;

  private final IndexedContainer groupsContainer = new IndexedContainer();

  private final IndexedContainer permissionsContainer = new IndexedContainer();

  private final ProgressBar progress;

  public UserManagementPanel()
  {

    userContainer = new BeanContainer<>(User.class);
    userContainer.setBeanIdProperty("name");

    progress = new ProgressBar();
    progress.setCaption("Loading user list");
    progress.setIndeterminate(true);
    progress.setVisible(false);

    GeneratedPropertyContainer generated = new GeneratedPropertyContainer(
      userContainer);
    generated.addGeneratedProperty("edit", new PropertyValueGenerator<String>()
    {

      @Override
      public String getValue(Item item, Object itemId, Object propertyId)
      {
        return "Edit";
      }

      @Override
      public Class<String> getType()
      {
        return String.class;
      }
    });
    generated.addGeneratedProperty("changePassword",
      new PropertyValueGenerator<String>()
      {

        @Override
        public String getValue(Item item, Object itemId, Object propertyId)
        {
          return "Change password";
        }

        @Override
        public Class<String> getType()
        {
          return String.class;
        }
      });

    userList = new Grid(generated);
    userList.setSizeFull();
    userList.setSelectionMode(Grid.SelectionMode.MULTI);
    userList.setColumns("name", "groups", "permissions", "expires", "edit",
      "changePassword");

    Grid.Column editColum = userList.getColumn("edit");
    editColum.setRenderer(new ButtonRenderer(
      new ClickableRenderer.RendererClickListener()
      {

        @Override
        public void click(ClickableRenderer.RendererClickEvent event)
        {

          User u = userContainer.getItem(event.getItemId()).getBean();

          FieldGroup group = new FieldGroup(userContainer.getItem(event.
              getItemId()));
          group.addCommitHandler(new UserCommitHandler(u.getName()));

          EditSingleUser edit = new EditSingleUser(group, groupsContainer);

          Window w = new Window("Edit user \"" + u.getName() + "\"");
          w.setContent(edit);
          w.setModal(true);
          w.setWidth("400px");
          w.setHeight("300px");
          UI.getCurrent().addWindow(w);
        }
      }));
    editColum.setHeaderCaption("");
    
    Grid.Column passwordColumn = userList.getColumn("changePassword");
    passwordColumn.setRenderer(new ButtonRenderer(
      new ClickableRenderer.RendererClickListener()
      {

        @Override
        public void click(ClickableRenderer.RendererClickEvent event)
        {
          UserManagementPanel.this.askForPasswordChange((String) event.getItemId());
        }
      }));
    passwordColumn.setHeaderCaption("");

    userList.getColumn("name").setHeaderCaption("Username");

    Grid.Column groupsColum = userList.getColumn("groups");
    groupsColum.setHeaderCaption("Groups");
    groupsColum.setConverter(new CommaSeperatedStringConverterSet());

    Grid.Column permissionsColumn = userList.getColumn("permissions");
    permissionsColumn.setHeaderCaption("Additional permissions");
    permissionsColumn.setConverter(new CommaSeperatedStringConverterSet());

    userList.getColumn("expires").setHeaderCaption("Expiration Date");

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
        Set<String> selectedUsers = new TreeSet<>();
        for(Object id : userList.getSelectedRows())
        {
          selectedUsers.add((String) id);
        }
        for (UserListView.Listener l : listeners)
        {
          l.deleteUsers(selectedUsers);
        }
      }
    });

    actionLayout = new HorizontalLayout(txtUserName,
      btAddNewUser, btDeleteUser);

    layout = new VerticalLayout(actionLayout, progress, userList);
    layout.setSizeFull();
    layout.setExpandRatio(userList, 1.0f);
    layout.setExpandRatio(progress, 1.0f);
    layout.setSpacing(true);
    layout.setMargin(new MarginInfo(true, false, false, false));

    layout.setComponentAlignment(actionLayout, Alignment.MIDDLE_CENTER);
    layout.setComponentAlignment(progress, Alignment.TOP_CENTER);

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
    for (String g : groupNames)
    {
      groupsContainer.addItem(g);
    }
  }

  @Override
  public void setLoadingAnimation(boolean show)
  {
    progress.setVisible(show);
    userList.setVisible(!show);
    actionLayout.setEnabled(!show);
  }

  public List<UserListView.Listener> getListeners()
  {
    return listeners;
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

          PopupTwinColumnSelect groupsSelector = new PopupTwinColumnSelect();
          groupsSelector.setSelectableContainer(groupsContainer);
          groupsSelector.setWidth("100%");
          groupsSelector.setCaption("Groups for \"" + itemId + "\"");
          groupsSelector.addValueChangeListener(new UserChangeListener(itemId));

          result = groupsSelector;

          break;
        case "permissions":

          PopupTwinColumnSelect permissionSelector = new PopupTwinColumnSelect();
          permissionSelector.setSelectableContainer(permissionsContainer);
          permissionSelector.setWidth("100%");
          permissionSelector.setCaption("Permissions for \"" + itemId + "\"");
          permissionSelector.addValueChangeListener(new UserChangeListener(
            itemId));

          result = permissionSelector;

          break;
        case "name":
          // explicitly request a read-only label for the name and groups
          result = null;
          break;
        case "expires":
          OptionalDateTimeField dateField = new OptionalDateTimeField("expires");
          dateField.addValueChangeListener(new UserChangeListener(itemId));

          result = dateField;
          break;
        default:
          result = super.createField(container, itemId, propertyId, uiContext);
          break;
      }

      return result;
    }

  }

  private class UserChangeListener implements Property.ValueChangeListener
  {

    private final Object itemId;

    public UserChangeListener(Object itemId)
    {
      this.itemId = itemId;
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event)
    {
      for (UserListView.Listener l : listeners)
      {
        l.userUpdated(userContainer.getItem(itemId).getBean());
      }
    }

  }

  private class UserCommitHandler implements FieldGroup.CommitHandler
  {

    private final String userName;

    public UserCommitHandler(String userName)
    {
      this.userName = userName;
    }

    @Override
    public void preCommit(FieldGroup.CommitEvent event) throws FieldGroup.CommitException
    {

    }

    @Override
    public void postCommit(FieldGroup.CommitEvent event) throws FieldGroup.CommitException
    {
      for (UserListView.Listener l : listeners)
      {
        l.userUpdated(userContainer.getItem(userName).getBean());
      }
    }

  }

}
