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
import annis.gui.converter.DateTimeStringConverter;
import annis.security.User;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.Action;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
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
import org.joda.time.DateTime;

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
    userList.setColumns("name", "edit", "changePassword", "expires", "groups",
      "permissions");
    
    HeaderRow filterRow = userList.appendHeaderRow();
    TextField userFilterField = new TextField();
    userFilterField.setInputPrompt("Filter");
    userFilterField.addTextChangeListener(new FieldEvents.TextChangeListener()
    {

      @Override
      public void textChange(FieldEvents.TextChangeEvent event)
      {
        userContainer.removeContainerFilters("name");
        if(!event.getText().isEmpty())
        {
          userContainer.addContainerFilter(new SimpleStringFilter("name",
            event.getText(), true, false));
        }
      }
    });
    filterRow.getCell("name").setComponent(userFilterField);
    
    CheckBox expiredFilterField = new CheckBox("has expired");
    expiredFilterField.addValueChangeListener(new Property.ValueChangeListener()
    {

      @Override
      public void valueChange(Property.ValueChangeEvent event)
      {
        userContainer.removeContainerFilters("expires");
        if((Boolean) event.getProperty().getValue() == true)
        {
          userContainer.addContainerFilter(new ExpiredUserFilter("expires"));
        }
      }
    });
    filterRow.getCell("expires").setComponent(expiredFilterField);
    
    TextField groupFilterField = new TextField();
    groupFilterField.setInputPrompt("Filter by groups");
    groupFilterField.addTextChangeListener(new FieldEvents.TextChangeListener()
    {

      @Override
      public void textChange(FieldEvents.TextChangeEvent event)
      {
        userContainer.removeContainerFilters("groups");
        if(!event.getText().isEmpty())
        {
          userContainer.addContainerFilter(new GroupManagementPanel.StringPatternInSetFilter("groups",
            event.getText()));
        }
      }
    });
    filterRow.getCell("groups").setComponent(groupFilterField);

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

          EditSingleUser edit = new EditSingleUser(group, groupsContainer,
            permissionsContainer);

          Window w = new Window("Edit user \"" + u.getName() + "\"");
          w.setContent(edit);
          w.setModal(true);
          w.setWidth("500px");
          w.setHeight("400px");
          UI.getCurrent().addWindow(w);
        }
      }));
    editColum.setHeaderCaption("");
    editColum.setExpandRatio(0);

    Grid.Column passwordColumn = userList.getColumn("changePassword");
    passwordColumn.setRenderer(new ButtonRenderer(
      new ClickableRenderer.RendererClickListener()
      {

        @Override
        public void click(ClickableRenderer.RendererClickEvent event)
        {
          UserManagementPanel.this.askForPasswordChange((String) event.
            getItemId());
        }
      }));
    passwordColumn.setHeaderCaption("");
    passwordColumn.setExpandRatio(0);

    userList.getColumn("name").setHeaderCaption("Username");

    Grid.Column groupsColumm = userList.getColumn("groups");
    groupsColumm.setHeaderCaption("Groups");
    groupsColumm.setConverter(new CommaSeperatedStringConverterSet());
    groupsColumm.setExpandRatio(1);

    Grid.Column permissionsColumn = userList.getColumn("permissions");
    permissionsColumn.setHeaderCaption("Additional permissions");
    permissionsColumn.setConverter(new CommaSeperatedStringConverterSet());

    Grid.Column expiresColumn = userList.getColumn("expires");
    expiresColumn.setHeaderCaption("Expiration Date");
    expiresColumn.setConverter(new DateTimeStringConverter());

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
        for (Object id : userList.getSelectedRows())
        {
          selectedUsers.add((String) id);
        }
        userList.getSelectionModel().reset();
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
  public void addAvailablePermissions(Collection<String> permissions)
  {
    for (String p : permissions)
    {
      permissionsContainer.addItem(p);
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
  
  public static class ExpiredUserFilter implements Container.Filter
  {
    
    private final Object propertyId;

    public ExpiredUserFilter(Object propertyId)
    {
      this.propertyId = propertyId;
    }
    
    @Override
    public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException
    {
      Object expirationDateRaw = item.getItemProperty(propertyId).getValue();
      if(expirationDateRaw instanceof DateTime)
      {
        DateTime expirationDate = (DateTime) expirationDateRaw;
        
        return expirationDate.isBeforeNow();
      }
      else if(expirationDateRaw == null)
      {
        // everything without an explicit date does not expire
        return false;
      }
      else
      {
        throw new UnsupportedOperationException();
      }
    }

    @Override
    public boolean appliesToProperty(Object propertyId)
    {
      return this.propertyId.equals(propertyId);
    }
    
  }

}
